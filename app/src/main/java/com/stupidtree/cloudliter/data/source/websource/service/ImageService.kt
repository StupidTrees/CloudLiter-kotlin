package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.cloudliter.data.model.ImageEntity
import com.stupidtree.cloudliter.ui.face.FaceEntity
import com.stupidtree.cloudliter.ui.gallery.SceneEntity
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

    /**
     * 获取某类别的图片
     */
    @GET("/image/by_class")
    fun getImagesOfClass(@Header("token") token: String,@Query("classKey")classKey:String,@Query("pageSize")pageSize:Int,@Query("pageNum")pageNum:Int):LiveData<ApiResponse<List<String>>>

    /**
     * 获取某类别的图片
     */
    @GET("/image/classes")
    fun getAllClasses(@Header("token") token: String):LiveData<ApiResponse<List<SceneEntity>>>

}