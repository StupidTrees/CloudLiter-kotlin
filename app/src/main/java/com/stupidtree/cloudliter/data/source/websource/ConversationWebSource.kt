package com.stupidtree.cloudliter.data.source.websource

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.source.websource.service.ConversationService
import com.stupidtree.cloudliter.data.source.websource.service.codes.SUCCESS
import com.stupidtree.cloudliter.data.source.websource.service.codes.TOKEN_INVALID
import com.stupidtree.cloudliter.ui.chat.read.ReadUser
import com.stupidtree.component.data.DataState
import com.stupidtree.component.web.BaseWebSource
import com.stupidtree.component.web.LiveDataCallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

/**
 * 层次：DataSource
 * 对话的数据源
 * 类型：网络数据
 * 数据：异步读，异步写
 */
class ConversationWebSource : BaseWebSource<ConversationService>(Retrofit.Builder()
        .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://hita.store:3000").build()) {
    override fun getServiceClass(): Class<ConversationService> {
        return ConversationService::class.java
    }

    /**
     * 获取某用户所有的对话
     *
     * @param token 令牌
     * @return 获取结果
     */
    fun getConversations(token: String): LiveData<DataState<MutableList<Conversation>?>> {
        return Transformations.map(service.getConversations(token)) { input ->
            if (null == input) {
                return@map DataState<MutableList<Conversation>?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                SUCCESS -> return@map DataState(input.data)
                TOKEN_INVALID -> return@map DataState<MutableList<Conversation>?>(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState<MutableList<Conversation>?>(DataState.STATE.FETCH_FAILED)
            }
        }
    }

    /**
     * 查询两用户的对话对象
     *
     * @param token    令牌
     * @return 查询结果
     */
    fun queryConversation(token: String,  conversationId: String): LiveData<DataState<Conversation?>> {
        return Transformations.map(service.queryConversation(token, conversationId), Function<ApiResponse<Conversation?>?, DataState<Conversation?>> { input ->
            if (null == input) {
                return@Function DataState<Conversation?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                SUCCESS -> DataState(input.data)
                TOKEN_INVALID -> DataState(DataState.STATE.TOKEN_INVALID)
                else -> DataState<Conversation?>(DataState.STATE.FETCH_FAILED)
            }
        })
    }

    /**
     * 获取对话词云
     * @param token 用户令牌
     * @return 词频表
     */
    fun getWordCloud(token: String?, conversationId:String): LiveData<DataState<HashMap<String, Float?>?>> {
        return Transformations.map<ApiResponse<HashMap<String, Float?>?>?, DataState<HashMap<String, Float?>?>>(service.getWordCloud(token, conversationId)) { input: ApiResponse<HashMap<String, Float?>?>? ->
            // Log.e("getWordCloud", String.valueOf(input));
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState(input.data)
                    TOKEN_INVALID -> return@map DataState<HashMap<String, Float?>?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<HashMap<String, Float?>?>(DataState.STATE.FETCH_FAILED)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    fun getReadUsers(token: String, messageId: String,conversationId: String,read:Boolean): LiveData<DataState<List<ReadUser>>> {
        return Transformations.map(service.getReadUser(token, messageId,conversationId,read)) { input ->
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState(input.data?: listOf())
                    TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState(DataState.STATE.FETCH_FAILED)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    companion object {
        //单例模式
        @Volatile
        var instance: ConversationWebSource? = null
            get() {
                if (field == null) {
                    field = ConversationWebSource()
                }
                return field
            }
            private set
    }
}