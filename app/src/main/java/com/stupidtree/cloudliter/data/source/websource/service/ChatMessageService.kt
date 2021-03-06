package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.ChatMessage
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

/**
 * 层次：Service
 * 消息记录网络服务
 */
interface ChatMessageService {
    /**
     * 获取消息
     * @param token 令牌
     * @param conversationId 对话id
     * @param pageSize 分页大小
     * @param fromId 从哪条记录往前
     * @return 获取结果
     */
    @GET("/message/get")
    fun getChatMessages(@Header("token") token: String, @Query("conversationId") conversationId: String, @Query("fromId") fromId: String?, @Query("pageSize") pageSize: Int): LiveData<ApiResponse<List<ChatMessage>?>?>

    @GET("/message/get")
    fun getChatMessagesCall(@Header("token") token: String, @Query("conversationId") conversationId: String, @Query("fromId") fromId: String?, @Query("pageSize") pageSize: Int): Call<ApiResponse<List<ChatMessage>?>?>

    /**
     * 拉取最新消息
     * @param token 令牌
     * @param conversationId 对话id
     * @param afterId 查询该id之后的消息
     * @return 获取结果
     */
    @GET("/message/get_message_after")
    fun getMessagesAfter(@Header("token") token: String, @Query("conversationId") conversationId: String, @Query("afterId") afterId: String?, @Query("includeBound") includeBond: Boolean): LiveData<ApiResponse<List<ChatMessage>?>?>

    /**
     * 发送消息
     * @param token 令牌
     * @param fromId
     * @param toId
     *
     * @return 返回结果
     */
    @FormUrlEncoded
    @POST("/message/send_text")
    fun sendTextMessage(@Header("token") token: String, @Field("fromId") fromId: String, @Field("toId") toId:String, @Field("content") content:String,@Field("uuid") uuid: String): LiveData<ApiResponse<ChatMessage?>?>


    /**
     * 发送图片
     * @param token 令牌
     * @param toId 朋友id
     * @param file 文件
     * @return 返回结果
     */
    @Multipart
    @POST("/message/send_image")
    fun sendImageMessage(@Header("token") token: String, @Query("toId") toId: String, @Part file: MultipartBody.Part, @Query("uuid") uuid: String?): LiveData<ApiResponse<ChatMessage?>?>

    @Multipart
    @POST("/message/send_voice")
    fun sendVoiceMessage(@Header("token") token: String, @Query("toId") toId: String, @Part file: MultipartBody.Part, @Query("uuid") uuid: String?,@Query("seconds")seconds:Int): LiveData<ApiResponse<ChatMessage?>?>


    /**
     * 消息语音识别
     * @param token 令牌
     * @param messageId 消息id
     * @return 获取结果
     */
    @FormUrlEncoded
    @POST("/ai/voice/tts")
    fun startTTS(@Header("token") token: String, @Field("id") messageId:String): LiveData<ApiResponse<ChatMessage>>

}


