package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.Conversation
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.*

/**
 * 层次：Service
 * 对话网络服务
 */
interface ConversationService {
    /**
     * 获取某用户的所有对话
     * @param token 登陆状态token
     * @return 对话列表
     */
    @GET("/conversation/get")
    fun getConversations(@Header("token") token: String?): LiveData<ApiResponse<MutableList<Conversation>?>?>

    /**
     * 查询两用户的某一对话
     * @param token 登陆状态token
     * @param userId 用户id（非必须，缺失的话服务端将从token中解析出用户id）
     * @return 对话列表
     */
    @GET("/conversation/query")
    fun queryConversation(@Header("token") token: String?, @Query("userId") userId: String?, @Query("friendId") friendId: String?): LiveData<ApiResponse<Conversation?>?>

    /**
     * 获取对话词云
     * @param token 登录状态的token
     * @return 搜索结果
     */
    @GET("/conversation/word_cloud")
    fun getWordCloud(@Header("token") token: String?, @Query("userId") userId: String?, @Query("friendId") friendId: String): LiveData<ApiResponse<HashMap<String, Float?>?>?>
}