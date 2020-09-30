package com.stupidtree.hichat.service;

import androidx.lifecycle.LiveData;

import com.google.gson.JsonElement;
import com.stupidtree.hichat.data.ApiResponse;
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
     * 请求建立好友关系
     * @param token 登录状态的token
     * @param friendId 对方的id
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("relation/make_friends")
    LiveData<ApiResponse<Boolean>> makeFriends(@Header("token")String token,@Field("friend")String friendId);




}


