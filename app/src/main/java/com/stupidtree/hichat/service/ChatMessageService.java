package com.stupidtree.hichat.service;

import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.ApiResponse;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;

import java.util.List;

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
     * @return 获取结果
     */
    @GET("/message/get")
    LiveData<ApiResponse<List<ChatMessage>>> getChatMessages(@Header("token") String token, @Query("conversationId") String conversationId);




}


