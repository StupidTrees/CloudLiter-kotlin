package com.stupidtree.cloudliter.service

import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.RelationEvent
import com.stupidtree.cloudliter.data.model.UserRelation
import retrofit2.http.*

/**
 * 层次：Service
 * 用户关系网络服务
 */
interface RelationService {
    /**
     * 获取某用户的所有好友
     * @param token 登陆状态token
     * @param id 用户id（非必须，缺失的话服务端将从token中解析出用户id）
     * @return 好友列表
     */
    @GET("/relation/friends")
    fun getFriends(@Header("token") token: String): LiveData<ApiResponse<List<UserRelation>?>?>
    fun isFriends(@Header("token") token: String, @Query("id1") id1: String, @Query("id2") id2: String): LiveData<ApiResponse<Boolean?>?>

    /**
     * 获得我和好友的关系对象
     * @param token 登录状态的token
     * @param friendId 好友的id
     * @return 判断结果
     */
    @GET("relation/query")
    fun queryRelation(@Header("token") token: String, @Query("friendId") friendId: String): LiveData<ApiResponse<UserRelation?>?>

    /**
     * 请求建立好友关系
     * @param token 登录状态的token
     * @param friendId 对方的id
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("relation/make_friends")
    fun makeFriends(@Header("token") token: String, @Field("friend") friendId: String): LiveData<ApiResponse<Boolean?>?>

    /**
     * 更换备注
     * @param remark 新备注
     * @param friend_id 朋友id
     * @param token 登录状态的token（表征了用户身份）
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/relation/friend_remark")
    fun changeRemark(@Field("id2") friend_id: String, @Field("remark") remark: String, @Header("token") token: String): LiveData<ApiResponse<Any?>?>

    /**
     * 发送好友申请
     * @param token 令牌
     * @param friendId 朋友id
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/relation/event/request")
    fun sendFriendRequest(@Header("token") token: String, @Field("friendId") friendId: String): LiveData<ApiResponse<Any?>?>

    /**
     * 获得所有和我有关的好友请求
     * @param token 令牌
     * @return 请求结果
     */
    @GET("relation/event/query_mine")
    fun queryMine(@Header("token") token: String): LiveData<ApiResponse<List<RelationEvent>?>?>

    /**
     * 响应好友申请
     * @param token 令牌
     * @param eventId 事件id
     * @param action 操作
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/relation/event/response")
    fun responseFriendRequest(@Header("token") token: String, @Field("eventId") eventId: String, @Field("action") action: String): LiveData<ApiResponse<Any?>?>

    /**
     * 删除好友
     * @param token 令牌
     * @param friendId 好友id
     * @return 删除结果
     */
    @FormUrlEncoded
    @POST("/relation/event/delete_friend")
    fun deleteFriend(@Header("token") token: String, @Field("friendId") friendId: String): LiveData<ApiResponse<Any?>?>

    /**
     * 获得未读好友事件数量
     * @param token 令牌
     * @return 删除结果
     */
    @GET("/relation/event/count_unread")
    fun countUnread(@Header("token") token: String): LiveData<ApiResponse<Int?>?>

    /**
     * 标记已读所有好友消息
     * @param token 令牌
     * @return 操作结果
     */
    @POST("/relation/event/mark_read")
    fun markRead(@Header("token") token: String): LiveData<ApiResponse<Any?>?>
}