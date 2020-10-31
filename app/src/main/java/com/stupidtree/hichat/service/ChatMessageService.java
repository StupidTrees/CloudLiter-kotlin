package com.stupidtree.hichat.service;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.ChatMessage;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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
     * @return 获取结果
     */
    @GET("/message/get_message_after")
    LiveData<ApiResponse<List<ChatMessage>>> getMessagesAfter(@Header("token") String token, @Query("conversationId") String conversationId,@Query("afterId") String afterId,@Query("includeBound") boolean includeBond);



    /**
     * 发送图片
     * @param token 令牌
     * @param toId 朋友id
     * @param file 文件
     * @return 返回结果
     */
    @Multipart
    @POST("/message/send_image")
    LiveData<ApiResponse<ChatMessage>> sendImageMessage(@Header("token") String token,@Query("toId") String toId,@Part MultipartBody.Part file,@Query("uuid") String uuid);

}


