package com.stupidtree.cloudliter.data.source.websource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.source.websource.service.AiService
import com.stupidtree.cloudliter.data.source.websource.service.LiveDataCallAdapter
import com.stupidtree.cloudliter.data.source.websource.service.codes
import com.stupidtree.cloudliter.ui.base.DataState
import okhttp3.MultipartBody
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
        return Transformations.map(service.imageClassifyDirect(token, upload)) { input->
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
    fun imageClassify(token: String, messageId:String): LiveData<DataState<JsonObject>> {
        return Transformations.map(service.imageClassify(token, messageId)) { input->
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