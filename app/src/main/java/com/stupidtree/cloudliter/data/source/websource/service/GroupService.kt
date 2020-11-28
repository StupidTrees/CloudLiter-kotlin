package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.RelationGroup
import retrofit2.http.*

/**
 * 层次：Service
 * 好友分组网络服务
 */
interface GroupService {
    /**
     * 查询我的所有好友分组
     * @param token 令牌
     * @return 对话列表
     */
    @GET("/group/get")
    fun queryMyGroups(@Header("token") token: String?): LiveData<ApiResponse<List<RelationGroup>?>?>

    /**
     * 为好友分配分组
     * @param token 令牌
     * @param friendId 朋友id
     * @param groupId 分组Id
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("group/assign")
    fun assignGroup(@Header("token") token: String?, @Field("friendId") friendId: String?, @Field("groupId") groupId: String?): LiveData<ApiResponse<Any?>?>

    /**
     * 添加好友分组
     * @param token 令牌
     * @return 对话列表
     */
    @FormUrlEncoded
    @POST("/group/add")
    fun addMyGroups(@Header("token") token: String?, @Field("groupName") groupName: String?): LiveData<ApiResponse<String?>?>

    /**
     * 删除好友分组
     * @param token 令牌
     */
    @FormUrlEncoded
    @POST("/group/delete")
    fun deleteMyGroups(@Header("token") token: String?, @Field("groupId") groupName: String?): LiveData<ApiResponse<String?>?>

    /**
     * 重命名好友分组
     * @param token 令牌
     */
    @FormUrlEncoded
    @POST("/group/rename")
    fun renameGroup(@Header("token") token: String, @Field("groupId") id: String,@Field("name") name:String): LiveData<ApiResponse<String?>?>

}