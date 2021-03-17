package com.stupidtree.cloudliter.data.source.websource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.model.FaceResult
import com.stupidtree.cloudliter.data.source.ai.detect.ObjectDetectSource.Companion.IMAGE_SIZE
import com.stupidtree.cloudliter.data.source.websource.service.AiService
import com.stupidtree.cloudliter.data.source.websource.service.LiveDataCallAdapter
import com.stupidtree.cloudliter.data.source.websource.service.codes
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.imagedetect.DetectResult
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AiWebSource : BaseWebSource<AiService>(Retrofit.Builder()
        .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://hita.store:3000").build()) {


    /**
     * 发送图片进行图片分类
     * @param token 令牌
     * @param file 文件
     * @return 操作结果
     */
    fun imageClassifyDirect(token: String, upload: MultipartBody.Part): LiveData<DataState<JsonObject>> {
        return Transformations.map(service.imageClassifyDirect(token, upload)) { input ->
            //Log.e("resp", input.toString())
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data!!)
                codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }


    /**
     * 对某个聊天图片进行图片分类
     * @param token 令牌
     * @param messageId 消息id
     * @return 操作结果
     */
    fun imageClassify(token: String, messageId: String): LiveData<DataState<String>> {
        return Transformations.map(service.imageClassify(token, messageId)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data!!)
                codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 上传语音音频文件
     * @param token 令牌
     * @param file 文件
     * @return 操作结果
     */
    fun voiceTTSDirect(token: String, file: MultipartBody.Part): LiveData<DataState<String?>> {
        return Transformations.map(service.voiceTTSDirect(token, file)) { input->
            // TODO
            // 后端未完成，没得测试
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(DataState.STATE.SUCCESS)
                codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }


    /**
     * 对某个文件的指定位置进行人脸检测
     * @param token 令牌
     * @return 操作结果
     */
    fun imageFaceRecognition(token: String, imageId: String, rectList: List<DetectResult>): LiveData<DataState<List<FaceResult>>> {
        val ja = JSONArray()
        val xRel = 1f / IMAGE_SIZE
        val yRel = 1f / IMAGE_SIZE
        for (rect in rectList) {
            val jo = JSONObject()
            jo.put("id", rect.id)
            jo.put("x", xRel*rect.rect.left)
            jo.put("y", yRel*rect.rect.top)
            jo.put("width", xRel* rect.rect.width())
            jo.put("height", yRel*rect.rect.width())
            ja.put(jo)
        }
        return Transformations.map(service.imageFaceRecognition(token, imageId, ja)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map input.data?.let { DataState(it) }?: DataState(DataState.STATE.FETCH_FAILED)
                codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }


    /**
     * 上传人脸文件
     *
     * @param token 令牌
     * @param file  文件
     * @return 返回结果
     */
    fun uploadFaceImage(token: String, file: MultipartBody.Part): LiveData<DataState<String?>> {
        return Transformations.map(service.uploadFaceImage(token, file)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(DataState.STATE.SUCCESS)
                codes.IMAGE_NO_FACE->return@map DataState(DataState.STATE.SPECIAL)
                codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    override fun getServiceClass(): Class<AiService> {
        return AiService::class.java
    }

    companion object {
        //单例模式
        @Volatile
        var instance: AiWebSource? = null
            get() {
                if (field == null) {
                    field = AiWebSource()
                }
                return field
            }
            private set
    }

}