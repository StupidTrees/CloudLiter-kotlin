package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.model.ApiResponse
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
    fun imageClassify(@Header("token") token: String,@Field("messageId") id:String): LiveData<ApiResponse<JsonObject>>


}