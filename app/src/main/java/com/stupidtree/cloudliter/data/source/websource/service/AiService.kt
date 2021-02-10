package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.model.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AiService {

    /**
     * 发送图片进行图片分类
     * @param token 令牌
     * @param file 文件
     * @return 操作结果
     */
    @Multipart
    @POST("/ai/image/classify")
    fun sendAiImage(@Header("token") token: String, @Part upload: MultipartBody.Part): LiveData<ApiResponse<String?>?>
}