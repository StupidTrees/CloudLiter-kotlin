package com.stupidtree.cloudliter.data.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier
import com.stupidtree.cloudliter.data.source.ai.yolo.YOLOSource
import com.stupidtree.cloudliter.data.source.websource.AiWebSource
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.imagedetect.BitmapRequestBody
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import java.io.ByteArrayOutputStream

class AiRepository(application: Application) {
    //数据源1：网络类型数据，消息记录的网络数据源
    var aiWebSource: AiWebSource = AiWebSource.instance!!
    val yoloSource: YOLOSource = YOLOSource.getInstance(application)

    
    /**
     * 进行图片分类
     *
     * @param token    令牌
     * @param bitmap 图片bitmap
     * @return 返回结果
     */
    fun imageClassify(token: String, bitmap: Bitmap): LiveData<DataState<JsonObject>> {
        val body = MultipartBody.Part.createFormData("upload","hello.jpg",BitmapRequestBody(bitmap))
        return aiWebSource.imageClassify(token,body)
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