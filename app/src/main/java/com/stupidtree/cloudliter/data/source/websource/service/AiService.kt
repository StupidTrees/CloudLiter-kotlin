package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.ChatMessage
import okhttp3.MultipartBody
import retrofit2.http.*

interface AiService {

    /**
     * 发送图片进行图片分类
     * @param token 令牌
     * @param file 文件
     * @return 操作结果
     */
    @Multipart
    @POST("/ai/image/classify_dir")
    fun imageClassifyDirect(@Header("token") token: String, @Part upload: MultipartBody.Part): LiveData<ApiResponse<JsonObject>>

    /**
     * 对某个聊天消息的图片进行图片分类
     * @param token 令牌
     * @param id 消息id
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/ai/image/classify")
    fun imageClassify(@Header("token") token: String,@Field("imageId") id:String): LiveData<ApiResponse<JsonObject>>


    /**
     * 上传人脸图片
     * @param token 令牌
     * @param file 文件
     * @return 返回结果
     */
    @Multipart
    @POST("/ai/face/upload")
    fun uploadFaceImage(@Header("token") token: String,@Part file: MultipartBody.Part): LiveData<ApiResponse<String?>?>

    /**
     * 上传语音文件（.m4a）进行转文字
     * @param token 令牌
     * @param file 文件
     * @return 操作结果
     */
    @Multipart
    @POST("/ai/voice/ttsdir")
    fun voiceTTSDirect(@Header("token") token: String, @Part upload: MultipartBody.Part): LiveData<ApiResponse<String?>?>


}