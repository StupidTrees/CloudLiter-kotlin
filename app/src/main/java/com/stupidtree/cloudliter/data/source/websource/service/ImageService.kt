package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.ImageEntity
import com.stupidtree.cloudliter.ui.wordcloud.FaceEntity
import retrofit2.http.*

interface ImageService {

    /**
     * 获得图片对象
     * @param token 令牌
     * @param imageId 图片id
     * @return 操作结果
     */
    @GET("/image/get_entity")
    fun getImageEntity(@Header("token") token: String, @Query("imageId")imageId:String): LiveData<ApiResponse<ImageEntity>>

    /**
     * 获得所有人脸
     */
    @GET("/image/get_faces")
    fun getFaces(@Header("token") token:String):LiveData<ApiResponse<List<FaceEntity>>>

    /**
     * 删除人脸
     */
    @FormUrlEncoded
    @POST("/image/delete_face")
    fun deleteFace(@Header("token") token:String,@Field("faceId")faceId:String):LiveData<ApiResponse<String?>>
}