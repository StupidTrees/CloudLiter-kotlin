package com.stupidtree.hichat.service;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.ChatMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * 层次：Service
 * 消息记录网络服务
 */
public interface ChatMessageService {


    /**
     * 获取消息
     * @param token 令牌
     * @param conversationId 对话id
     * @param pageSize 分页大小
     * @param fromId 从哪条记录往前
     * @return 获取结果
     */
    @GET("/message/get")
    LiveData<ApiResponse<List<ChatMessage>>> getChatMessages(@Header("token") String token, @Query("conversationId") String conversationId, @Query("fromId") String fromId, @Query("pageSize") int pageSize);

    @GET("/message/get")
    Call<ApiResponse<List<ChatMessage>>> getChatMessagesCall(@Header("token") String token, @Query("conversationId") String conversationId, @Query("fromId") String fromId, @Query("pageSize") int pageSize);

    /**
     * 拉取最新消息
     * @param token 令牌
     * @param conversationId 对话id
     * @param afterId 查询该id之后的消息
     * @param pageSize 分页大小
     * @return 获取结果
     */
    @GET("/message/pull_latest")
    LiveData<ApiResponse<List<ChatMessage>>> pullLatestChatMessages(@Header("token") String token, @Query("conversationId") String conversationId,@Query("afterId") String afterId);



}


