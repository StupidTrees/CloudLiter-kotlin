package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.cloudliter.data.model.ImageEntity
import com.stupidtree.cloudliter.ui.face.FaceEntity
import com.stupidtree.cloudliter.ui.face.permission.FaceWhiteListEntity
import com.stupidtree.cloudliter.ui.gallery.faces.FriendFaceEntity
import com.stupidtree.cloudliter.ui.gallery.scene.SceneEntity
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
     * 获取包含某好友
     */
    @GET("/image/by_friend")
    fun getImagesOfFriend(@Header("token") token: String,@Query("friendId")friendId:String,@Query("pageSize")pageSize:Int,@Query("pageNum")pageNum:Int):LiveData<ApiResponse<List<String>>>

    /**
     * 获取某类别的图片
     */
    @GET("/image/classes")
    fun getAllClasses(@Header("token") token: String):LiveData<ApiResponse<List<SceneEntity>>>

    /**
     * 获取所有好友人脸
     */
    @GET("/image/friend_faces")
    fun getFriendFaces(@Header("token") token: String):LiveData<ApiResponse<List<FriendFaceEntity>>>


    /**
     * 获取白名单
     */
    @GET("/image/whitelist")
    fun getFaceWhiteList(@Header("token") token: String):LiveData<ApiResponse<List<FaceWhiteListEntity>>>


    /**
     * 获取白名单
     */
    @FormUrlEncoded
    @POST("/image/add_whitelist")
    fun addWhiteList(@Header("token") token: String,@Field("whitelist")userIds:List<String>):LiveData<ApiResponse<Any>>

    /**
     * 获取白名单
     */
    @FormUrlEncoded
    @POST("/image/remove_whitelist")
    fun removeFromWhiteList(@Header("token") token: String,@Field("friendId")friendId: String):LiveData<ApiResponse<Any>>

}