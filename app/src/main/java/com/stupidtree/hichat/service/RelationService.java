package com.stupidtree.hichat.service;

import androidx.lifecycle.LiveData;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stupidtree.hichat.data.ApiResponse;
import com.stupidtree.hichat.data.model.RelationEvent;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 层次：Service
 * 用户关系网络服务
 */
public interface RelationService {

    /**
     * 获取某用户的所有好友
     * @param token 登陆状态token
     * @param id 用户id（非必须，缺失的话服务端将从token中解析出用户id）
     * @return 好友列表
     */
    @GET("/relation/friends")
    LiveData<ApiResponse<JsonElement>> getFriends(@Header("token") String token, @Query("id") String id);


    /**
     * 判断某两个用户是否是好友
     * @param token 登录状态的token
     * @param id1 主人用户id（非必须）
     * @param id2 对方用户的id（必须）
     * @return 判断结果
     */
    @GET("relation/is_friend")
    LiveData<ApiResponse<Boolean>> isFriends(@Header("token") String token, @Query("id1") String id1, @Query("id2") String id2);


    /**
     * 获得我和好友的关系对象
     * @param token 登录状态的token
     * @param friendId 好友的id
     * @return 判断结果
     */
    @GET("relation/query")
    LiveData<ApiResponse<JsonObject>> queryRelation(@Header("token") String token, @Query("friendId") String friendId);



    /**
     * 请求建立好友关系
     * @param token 登录状态的token
     * @param friendId 对方的id
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("relation/make_friends")
    LiveData<ApiResponse<Boolean>> makeFriends(@Header("token")String token,@Field("friend")String friendId);



    /**
     * 更换备注
     * @param remark 新备注
     * @param friend_id 朋友id
     * @param token 登录状态的token（表征了用户身份）
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/relation/friend_remark")
    LiveData<ApiResponse<Object>> changeRemark(@Field("id2")String friend_id,@Field("remark")String remark,@Header("token")String token);


    /**
     * 发送好友申请
     * @param token 令牌
     * @param friendId 朋友id
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/relation/event/request")
    LiveData<ApiResponse<Object>> sendFriendRequest(@Header("token") String token, @Field("friendId") String friendId);


    /**
     * 获得所有和我有关的好友请求
     * @param token 令牌
     * @return 请求结果
     */
    @GET("relation/event/query_mine")
    LiveData<ApiResponse<List<RelationEvent>>> queryMine(@Header("token") String token);


    /**
     * 响应好友申请
     * @param token 令牌
     * @param eventId 事件id
     * @param action 操作
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/relation/event/response")
    LiveData<ApiResponse<Object>> responseFriendRequest(@Header("token")String token, @Field("eventId") String eventId,@Field("action") String action);


    /**
     * 删除好友
     * @param token 令牌
     * @param friendId 好友id
     * @return 删除结果
     */
    @FormUrlEncoded
    @POST("/relation/event/delete_friend")
    LiveData<ApiResponse<Object>> deleteFriend(@Header("token")String token, @Field("friendId") String friendId);


    /**
     * 获得未读好友事件数量
     * @param token 令牌
     * @return 删除结果
     */
    @GET("/relation/event/count_unread")
    LiveData<ApiResponse<Integer>> countUnread(@Header("token")String token);



    /**
     * 标记已读所有好友消息
     * @param token 令牌
     * @return 操作结果
     */
    @POST("/relation/event/mark_read")
    LiveData<ApiResponse<Object>> markRead(@Header("token") String token);
}


