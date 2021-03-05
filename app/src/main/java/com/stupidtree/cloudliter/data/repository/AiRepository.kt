package com.stupidtree.cloudliter.data.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier
import com.stupidtree.cloudliter.data.source.ai.yolo.YOLOSource
import com.stupidtree.cloudliter.data.source.websource.AiWebSource
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.imagedetect.BitmapRequestBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception

class AiRepository(application: Application) {
    //数据源1：网络类型数据，消息记录的网络数据源
    var aiWebSource: AiWebSource = AiWebSource.instance!!
    val yoloSource: YOLOSource = YOLOSource.getInstance(application)
    val imageDao = AppDatabase.getDatabase(application).imageDao()

    /**
     * 进行图片分类（上传图片文件形式）
     *
     * @param token    令牌
     * @param bitmap 图片bitmap
     * @return 返回结果
     */
    fun imageClassifyDirect(token: String, bitmap: Bitmap): LiveData<DataState<JsonObject>> {
        val body = MultipartBody.Part.createFormData("upload", "hello.jpg", BitmapRequestBody(bitmap))
        return aiWebSource.imageClassifyDirect(token, body)
    }

    /**
     * 进行图片分类
     *
     * @param token    令牌
     * @param imageId 图片id
     * @return 返回结果
     */
    fun imageClassify(token: String, imageId: String): LiveData<DataState<JsonObject>> {
        val res = MediatorLiveData<DataState<JsonObject>>()
        val cache = Transformations.switchMap(imageDao.getSceneBtId(imageId)){
            return@switchMap try{
                MutableLiveData(JsonParser().parse(it).asJsonObject)
            }catch(e:Exception){
                MutableLiveData()
            }
        }
        res.addSource(cache){
            res.value = DataState(it)
        }
        res.addSource(aiWebSource.imageClassify(token, imageId)){
            if(it.state==DataState.STATE.SUCCESS){
                Thread{
                    it.data?.let{jo->
                        imageDao.updateSceneSync(imageId,jo.toString())
                    }
                }.start()
            }
        }
        return res
    }


    /**
     * 上传人脸图片
     *
     * @param token    令牌
     * @param filePath 文件
     * @return 返回结果
     */
    fun uploadFaceImage(context: Context, token: String, filePath: String): LiveData<DataState<String?>> {
        val result = MediatorLiveData<DataState<String?>>()
        //读取图片文件
        val f = File(filePath)
        Luban.with(context)
                .setTargetDir(context.getExternalFilesDir("image")!!.absolutePath)
                .load(f)
                .setCompressListener(object : OnCompressListener {
                    //设置回调
                    override fun onStart() {
                        Log.e("luban", "开始压缩")
                    }

                    override fun onSuccess(file: File) {
                        Log.e("luban", "压缩成功")
                         val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                        //构造一个图片格式的POST表单
                        val body = MultipartBody.Part.createFormData("upload", file.name, requestFile)
                        val sendResult = aiWebSource.uploadFaceImage(token, body)
                        result.addSource(sendResult) { v ->
                            result.value = v
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e("luban", "压缩失败")
                    }
                }).launch() //启动压缩
        return result
    }


    fun detectImage(bitmap: Bitmap): LiveData<DataState<List<Classifier.Recognition>>> {
        return yoloSource.detectImage(bitmap)
    }

    companion object {
        //单例模式
        @JvmStatic
        @Volatile
        private var instance: AiRepository? = null
        fun getInstance(application: Application): AiRepository {
            if (instance == null) {
                instance = AiRepository(application)
            }
            return instance!!
        }
    }
}