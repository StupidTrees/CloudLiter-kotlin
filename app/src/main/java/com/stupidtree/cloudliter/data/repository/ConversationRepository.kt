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
import com.stupidtree.cloudliter.ui.base.DataState
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
        //socketWebSource.bindService("conversation", context)
    }

    fun unbindService(context: Context) {
       // socketWebSource.unbindService(context)
        context.unregisterReceiver(socketWebSource)
    }


    var listLiveData = MediatorLiveData<DataState<MutableList<Conversation>?>>()
    private var listLocalData: LiveData<MutableList<Conversation>>? = null
    private var listWebData: LiveData<DataState<MutableList<Conversation>?>>? = null
    /**
     * 动作：获取对话列表
     * @param token 令牌
     */
    fun actionGetConversations(token: String) {
        listLocalData?.let { listLiveData.removeSource(it) }
        listLocalData = conversationDao.getConversations()
        listLiveData.addSource(listLocalData!!) { conversations ->
            listLiveData.value = DataState(conversations).setRetry(false)
            //只进行一次网络拉取
            listWebData?.let { listLiveData.removeSource(it) }
            listWebData = conversationWebSource.getConversations(token)
            listLiveData.addSource(listWebData!!) { listDataState ->
                if (listDataState.state === DataState.STATE.SUCCESS) {
                    listLocalData?.let { listLiveData.removeSource(it) }
                    listLiveData.value = listDataState.setRetry(true).setRetryState(DataState.STATE.SUCCESS)
                    //找到本地上多余的，删除
                    val redundant = mutableListOf<Conversation>()
                    conversations.let {
                        for(o in it){
                            var contains = false
                            for(d in listDataState.data!!){
                                if(d.id==o.id){
                                    contains = true
                                    break
                                }
                            }
                            if(!contains){
                                redundant.add(o)
                            }
                        }
                    }
                    Thread {
                        conversationDao.deleteConversations(redundant)
                        conversationDao.saveConversations(listDataState.data as MutableList)
                    }.start()
                } else if (listDataState.state === DataState.STATE.FETCH_FAILED) {
                    listLiveData.value = DataState(conversations).setRetry(true).setRetryState(DataState.STATE.FETCH_FAILED)
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
    fun actionCallOnline(context: Context, userLocal: UserLocal) {
        socketWebSource.callOnline(context, userLocal)
    }


    /**
     * 获取对话对象
     */
    fun queryConversation(token: String, userId: String, friendId: String): LiveData<DataState<Conversation?>> {
        val result = MediatorLiveData<DataState<Conversation?>>()
        result.addSource(conversationDao.getConversationAt(friendId)) {
            result.value = DataState(it)
        }
        result.addSource(conversationWebSource.queryConversation(token, userId, friendId)) {
            if (it.state == DataState.STATE.SUCCESS) {
                Thread {
                    it.data?.let { it1 -> conversationDao.saveConversation(it1) }
                }.start()
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
    fun getUserWordCloud(token: String?, userId: String, friendId: String): LiveData<DataState<HashMap<String, Float?>?>> {
        return conversationWebSource.getWordCloud(token, userId, friendId)
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