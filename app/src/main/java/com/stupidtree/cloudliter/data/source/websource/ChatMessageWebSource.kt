package com.stupidtree.cloudliter.data.source.websource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.source.websource.service.ChatMessageService
import com.stupidtree.cloudliter.data.source.websource.service.LiveDataCallAdapter
import com.stupidtree.cloudliter.data.source.websource.service.codes
import com.stupidtree.cloudliter.ui.base.DataState
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 层次：DataSource
 * 消息记录的数据源
 * 类型：网络数据
 * 数据：异步读，异步写
 */
class ChatMessageWebSource : BaseWebSource<ChatMessageService>(Retrofit.Builder()
        .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://hita.store:3000").build()) {
    /**
     * 获取某对话下的所有消息记录
     *
     * @param token 令牌
     * @param id    对话id
     * @return 获取结果
     */
    fun getMessages(token: String, id: String, fromId: String?, pageSize: Int): LiveData<DataState<List<ChatMessage>?>> {
        // Log.e("getMes",token+"-"+fromId+"-"+pageSize);
        return Transformations.map(this.service.getChatMessages(token,id,fromId,pageSize)) { input:ApiResponse<List<ChatMessage>?>?
            ->
            if (null == input) {
                return@map DataState<List<ChatMessage>?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> {
                    return@map DataState(input.data)
                }
                codes.TOKEN_INVALID -> return@map DataState<List<ChatMessage>?>(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState<List<ChatMessage>?>(DataState.STATE.FETCH_FAILED)
            }
        }

    }


    /**
     * 拉取某消息之后的所有消息（包括该消息）
     *
     * @param token   令牌
     * @param id      对话id
     * @param afterId 查询该id之后的消息
     * @return 获取结果
     */
    fun getMessagesAfter(token: String, id: String, afterId: String?, includeBound: Boolean): LiveData<DataState<List<ChatMessage>?>>? {
        return Transformations.map(service.getMessagesAfter(token,id,afterId,includeBound)){
            input: ApiResponse<List<ChatMessage>?>? ->
            if (null == input) {
                return@map DataState<List<ChatMessage>?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data)
                codes.TOKEN_INVALID -> return@map DataState<List<ChatMessage>?>(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState<List<ChatMessage>?>(DataState.STATE.FETCH_FAILED)
            }
        }

    }

    /**
     * 发送图片
     *
     * @param token 令牌
     * @param toId  朋友id
     * @param file  文件
     * @return 返回结果
     */
    fun sendImageMessage(token: String, toId: String, file: MultipartBody.Part, uuid: String?): LiveData<DataState<ChatMessage?>> {
        return Transformations.map(service.sendImageMessage(token, toId, file, uuid)) { input: ApiResponse<ChatMessage?>? ->
            Log.e("发送图片结果", input.toString())
            if (input == null) {
                return@map DataState<ChatMessage?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data)
                codes.TOKEN_INVALID -> return@map DataState<ChatMessage?>(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState<ChatMessage?>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 发送语音
     *
     * @param token 令牌
     * @param toId  朋友id
     * @param file  文件
     * @return 返回结果
     */
    fun sendVoiceMessage(token: String, toId: String, file: MultipartBody.Part, uuid: String?,seconds:Int): LiveData<DataState<ChatMessage?>> {
        return Transformations.map(service.sendVoiceMessage(token, toId, file, uuid,seconds)) { input: ApiResponse<ChatMessage?>? ->
            Log.e("发送语音结果", input.toString())
            if (input == null) {
                return@map DataState<ChatMessage?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data)
                codes.TOKEN_INVALID -> return@map DataState<ChatMessage?>(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState<ChatMessage?>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }


    override fun getServiceClass(): Class<ChatMessageService> {
        return ChatMessageService::class.java
    }

    companion object {
        //单例模式
        @Volatile
        var instance: ChatMessageWebSource? = null
            get() {
                if (field == null) {
                    field = ChatMessageWebSource()
                }
                return field
            }
            private set
    }
}