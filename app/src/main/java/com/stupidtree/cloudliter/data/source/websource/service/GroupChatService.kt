package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.model.GroupChat
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.ui.conversation.group.GroupMemberEntity
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * 层次：Service
 * 群聊网络服务
 */
interface GroupChatService {

    @FormUrlEncoded
    @POST("/group_chat/create")
    fun createGroup(@Header("token") token: String, @Field("name") name: String, @Field("list") list: List<String>): LiveData<ApiResponse<String?>?>

    /**
     * 获取所有成员
     * @param token 令牌
     */
    @GET("/group_chat/members")
    fun getAllMembers(@Header("token") token: String, @Query("groupId") groupId: String): LiveData<ApiResponse<List<GroupMemberEntity>>?>

    @FormUrlEncoded
    @POST("/group_chat/rename")
    fun renameGroup(@Header("token") token: String, @Field("name") name: String, @Field("groupId") groupId: String): LiveData<ApiResponse<String?>?>

    @FormUrlEncoded
    @POST("/group_chat/quit")
    fun quitGroup(@Header("token") token: String, @Field("groupId") groupId: String): LiveData<ApiResponse<String?>?>

    @FormUrlEncoded
    @POST("/group_chat/destroy")
    fun destroyGroup(@Header("token") token: String, @Field("groupId") groupId: String): LiveData<ApiResponse<String?>?>

    /**
     * 获取所有成员
     * @param token 令牌
     */
    @GET("/group_chat/get")
    fun getGroupEntity(@Header("token") token: String, @Query("groupId") groupId: String): LiveData<ApiResponse<GroupChat>?>


    /**
     * 上传头像
     * @param file 头像体
     * @param token 登录状态的token
     * @return 返回操作结果和文件名
     */
    @Multipart
    @POST("/group_chat/upload_avatar")
    fun uploadAvatar(@Part file: MultipartBody.Part?, @Header("token") token: String?, @Query("groupId") groupId: String): LiveData<ApiResponse<String?>?>

}