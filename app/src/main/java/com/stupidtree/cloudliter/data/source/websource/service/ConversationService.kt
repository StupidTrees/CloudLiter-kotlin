package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.model.AccessibilityInfo
import com.stupidtree.cloudliter.ui.chat.read.ReadUser
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
     * @return 对话列表
     */
    @GET("/conversation/query")
    fun queryConversation(@Header("token") token: String?, @Query("conversationId") conversationId: String?): LiveData<ApiResponse<Conversation?>?>

    /**
     * 获取对话词云
     * @param token 登录状态的token
     * @return 搜索结果
     */
    @GET("/conversation/word_cloud")
    fun getWordCloud(@Header("token") token: String?, @Query("conversationId") conversationId: String): LiveData<ApiResponse<HashMap<String, Float?>?>?>


    @GET("/message/read_user")
    fun getReadUser(@Header("token") token: String, @Query("messageId") messageId: String, @Query("conversationId") conversationId: String,
                    @Query("read") read: Boolean): LiveData<ApiResponse<List<ReadUser>>>


    @GET("/conversation/accessibility_info")
    fun getAccessibilityInfo(@Header("token") token: String, @Query("conversationId") conversationId: String,
                    @Query("type") type: Conversation.TYPE): LiveData<ApiResponse<AccessibilityInfo>>

}