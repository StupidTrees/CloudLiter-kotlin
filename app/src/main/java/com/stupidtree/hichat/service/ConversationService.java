package com.stupidtree.hichat.service;

import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.Conversation;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * 层次：Service
 * 对话网络服务
 */
public interface ConversationService {

    /**
     * 获取某用户的所有对话
     * @param token 登陆状态token
     * @param userId 用户id（非必须，缺失的话服务端将从token中解析出用户id）
     * @return 对话列表
     */
    @GET("/conversation/get")
    LiveData<ApiResponse<List<Conversation>>> getConversations(@Header("token") String token, @Query("id") String userId);

    /**
     * 查询两用户的某一对话
     * @param token 登陆状态token
     * @param userId 用户id（非必须，缺失的话服务端将从token中解析出用户id）
     * @return 对话列表
     */
    @GET("/conversation/query")
    LiveData<ApiResponse<Conversation>> queryConversation(@Header("token") String token, @Query("userId") String userId,@Query("friendId") String friendId);




}


