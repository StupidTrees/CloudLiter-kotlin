package com.stupidtree.cloudliter.data.repository

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.source.dao.ConversationDao
import com.stupidtree.cloudliter.data.source.websource.ConversationWebSource
import com.stupidtree.cloudliter.data.source.websource.SocketWebSource
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.cloudliter.data.model.AccessibilityInfo
import com.stupidtree.cloudliter.ui.chat.read.ReadUser
import com.stupidtree.component.data.DataState
import java.util.*

/**
 * 层次：Repository
 * 对话仓库
 */
class ConversationRepository(context: Context) {
    //数据源1：网络类型数据，对话的网络数据源
    private var conversationWebSource: ConversationWebSource = ConversationWebSource.instance!!

    //数据源2：长连接数据源
    var socketWebSource: SocketWebSource = SocketWebSource()

    //数据源3：对话的本地存储
    private var conversationDao: ConversationDao = AppDatabase.getDatabase(context).conversationDao()

    val unreadMessageState: LiveData<DataState<HashMap<String, Int>>>
        get() = SocketWebSource.unreadMessageState

    fun bindService(context: Context) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(SocketIOClientService.RECEIVE_RECEIVE_MESSAGE)
        intentFilter.addAction(SocketIOClientService.RECEIVE_FRIEND_STATE_CHANGED)
        intentFilter.addAction(SocketIOClientService.RECEIVE_UNREAD_MESSAGE)
        context.registerReceiver(socketWebSource, intentFilter)
    }

    fun unbindService(context: Context) {
        try {
            context.unregisterReceiver(socketWebSource)
        } catch (e: Exception) {
        }
    }


    fun getConversations(token: String): LiveData<DataState<MutableList<Conversation>?>> {
        val res = MediatorLiveData<DataState<MutableList<Conversation>?>>()
        val local = conversationDao.getConversations()
        res.addSource(local) { conversations ->
            res.value = DataState(conversations).setFromCache(true)
            //只进行一次网络拉取
            val listWebData = conversationWebSource.getConversations(token)
            res.addSource(listWebData) { listDataState ->
                if (listDataState.state === DataState.STATE.SUCCESS) {
                    res.removeSource(local)
                    res.value = listDataState.setFromCache(false).setRetryState(DataState.STATE.SUCCESS)
                    //找到本地上多余的，删除
                    val redundant = mutableListOf<Conversation>()
                    conversations.let {
                        for (o in it) {
                            var contains = false
                            for (d in listDataState.data!!) {
                                if (d.id == o.id) {
                                    contains = true
                                    break
                                }
                            }
                            if (!contains) {
                                redundant.add(o)
                            }
                        }
                    }
                    Thread {
                        conversationDao.deleteConversations(redundant)
                        conversationDao.saveConversations(listDataState.data as MutableList)
                    }.start()
                } else if (listDataState.state === DataState.STATE.FETCH_FAILED) {
                    res.value = DataState(conversations).setFromCache(false).setRetryState(DataState.STATE.FETCH_FAILED)
                }
            }
        }
        return res
    }

    /**
     * 动作：通知上线
     *
     * @param context   上下文
     * @param userLocal 本地用户
     */
    fun actionCallOnline(context: Context, userLocal: UserLocal) {
        socketWebSource.callOnline(context, userLocal)
    }


    /**
     * 获取对话对象
     */
    fun queryConversation(token: String, conversationId: String): LiveData<DataState<Conversation?>> {
        val result = MediatorLiveData<DataState<Conversation?>>()
        val local = conversationDao.getConversationAt(conversationId)
        result.addSource(local) {
            result.value = DataState(it).setFromCache(true)
        }
        result.addSource(conversationWebSource.queryConversation(token, conversationId)) {
            if (it.state == DataState.STATE.SUCCESS) {
                result.removeSource(local)
                Thread {
                    it.data?.let { it1 -> conversationDao.saveConversation(it1) }
                }.start()
                result.value = it.setFromCache(false)
            }
        }
        return result
    }

    /**
     * 获取聊天词云
     *
     * @param token 用户令牌
     * @return 词频表
     */
    fun getUserWordCloud(token: String?, conversationId: String): LiveData<DataState<HashMap<String, Float?>?>> {
        return conversationWebSource.getWordCloud(token, conversationId)
    }

    fun getReadUsers(token: String, messageId: String, conversationId: String, read: Boolean): LiveData<DataState<List<ReadUser>>> {
        return conversationWebSource.getReadUsers(token, messageId, conversationId, read)
    }

    fun getAccessibilityInfo(token: String, conversationId: String, type: Conversation.TYPE): LiveData<DataState<AccessibilityInfo>> {
        return conversationWebSource.getAccessibilityInfo(token, conversationId, type)
    }


    companion object {
        //单例模式
        @Volatile
        private var instance: ConversationRepository? = null

        @JvmStatic
        fun getInstance(application: Application): ConversationRepository {
            if (instance == null) {
                instance = ConversationRepository(application.applicationContext)
            }
            return instance!!
        }
    }

}