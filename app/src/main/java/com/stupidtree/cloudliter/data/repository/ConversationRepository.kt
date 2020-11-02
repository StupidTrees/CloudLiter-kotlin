package com.stupidtree.cloudliter.data.repository

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.source.ConversationDao
import com.stupidtree.cloudliter.data.source.ConversationWebSource
import com.stupidtree.cloudliter.data.source.SocketWebSource
import com.stupidtree.cloudliter.socket.SocketIOClientService
import com.stupidtree.cloudliter.ui.base.DataState
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 层次：Repository
 * 对话仓库
 */
class ConversationRepository(context: Context) {
    //数据源1：网络类型数据，对话的网络数据源
    var conversationWebSource: ConversationWebSource

    //数据源2：长连接数据源
    var socketWebSource: SocketWebSource

    //数据源3：对话的本地存储
    var conversationDao: ConversationDao
    var listLiveData = MediatorLiveData<DataState<List<Conversation>?>>()
    var listLocalData: LiveData<MutableList<Conversation>>? = null
    var listWebData: LiveData<DataState<List<Conversation>>>? = null
    fun bindService(context: Context) {
        val IF = IntentFilter()
        IF.addAction(SocketIOClientService.ACTION_RECEIVE_MESSAGE)
        IF.addAction(SocketIOClientService.ACTION_FRIEND_STATE_CHANGED)
        context.registerReceiver(socketWebSource, IF)
        socketWebSource.bindService("conversation", context)
    }

    fun unbindService(context: Context) {
        socketWebSource.unbindService(context)
        context.unregisterReceiver(socketWebSource)
    }

    /**
     * 动作：获取对话列表
     *
     * @param token 令牌
     */
    fun ActionGetConversations(token: String) {
        listLocalData?.let { listLiveData.removeSource(it) }
        listLocalData = conversationDao.getConversations()
        val tried = AtomicBoolean(false)
        listLiveData.addSource(listLocalData!!) { conversations ->
            listLiveData.value = DataState<List<Conversation>?>(conversations).setRetry(tried.get())
            //只进行一次网络拉取
            if (!tried.get()) {
                tried.set(true)
                listWebData?.let { listLiveData.removeSource(it) }
                listWebData = conversationWebSource.getConversations(token)
                listLiveData.addSource(listWebData!!) { listDataState ->
                    if (listDataState.state === DataState.STATE.SUCCESS) {
                        Thread(Runnable {
                            conversationDao.clearTable()
                            conversationDao.saveConversations(listDataState.data as MutableList)
                        }).start()
                    } else if (listDataState.state === DataState.STATE.FETCH_FAILED) {
                        listLiveData.setValue(DataState(conversations, DataState.STATE.FETCH_FAILED))
                    }
                }
            }
        }
    }

    /**
     * 动作：通知上线
     *
     * @param context   上下文
     * @param userLocal 本地用户
     */
    fun ActionCallOnline(context: Context, userLocal: UserLocal) {
        socketWebSource.callOnline(context, userLocal)
    }

    val unreadMessageState: LiveData<DataState<HashMap<String, Int>>>
        get() = socketWebSource.unreadMessageState

    fun queryConversation(token: String, userId: String?, friendId: String): LiveData<DataState<Conversation?>> {
        return conversationWebSource.queryConversation(token, userId, friendId)
    }

    /**
     * 获取聊天词云
     *
     * @param token 用户令牌
     * @return 词频表
     */
    fun getUserWordCloud(token: String?, userId: String, friendId: String): LiveData<DataState<HashMap<String, Float>>> {
        return conversationWebSource.getWordCloud(token, userId, friendId)
    }

    companion object {
        //单例模式
        @Volatile
        private var instance: ConversationRepository? = null
        @JvmStatic
        fun getInstance(application: Application): ConversationRepository? {
            if (instance == null) {
                instance = ConversationRepository(application.applicationContext)
            }
            return instance
        }
    }

    init {
        conversationWebSource = ConversationWebSource.getInstance()
        socketWebSource = SocketWebSource()
        conversationDao = AppDatabase.getDatabase(context).conversationDao()
    }
}