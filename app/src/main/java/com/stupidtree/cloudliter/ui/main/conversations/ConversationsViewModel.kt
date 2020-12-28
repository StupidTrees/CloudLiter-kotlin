package com.stupidtree.cloudliter.ui.main.conversations

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.ConversationRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import java.util.*

class ConversationsViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */
    //对话仓库
    private val conversationRepository: ConversationRepository? = getInstance(application)

    //本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)


    /**
     * 数据区
     */
    //数据本体：列表数据
    var listData: MediatorLiveData<DataState<MutableList<Conversation>?>>? =conversationRepository!!.listLiveData

    //数据本体：未读消息
    var unreadMessageState: LiveData<DataState<HashMap<String, Int>>>? = null
        get() {
            if (field == null) {
                field = Transformations.map(conversationRepository!!.unreadMessageState) { input: DataState<HashMap<String, Int>> ->
                    when {
                        input.listAction === DataState.LIST_ACTION.APPEND -> {
                            for (key in input.data!!.keys) {
                                val oldValue = unreadMessages[key]
                                if (oldValue == null) {
                                    unreadMessages[key] = 1
                                } else {
                                    unreadMessages[key] = oldValue + 1
                                }
                            }
                        }
                        input.listAction === DataState.LIST_ACTION.DELETE -> {
                            for (key in input.data!!.keys) {
                                val oldValue = unreadMessages[key]
                                val deleteValue = input.data!![key]
                                if (oldValue != null) {
                                    when {
                                        oldValue <= 1 -> {
                                            unreadMessages.remove(key)
                                        }
                                        deleteValue != null -> {
                                            unreadMessages[key] = oldValue - deleteValue
                                        }
                                        else -> {
                                            unreadMessages[key] = oldValue - 1
                                        }
                                    }
                                }
                            }
                        }
                        input.listAction === DataState.LIST_ACTION.REPLACE_ALL -> {
                            unreadMessages.clear()
                            unreadMessages.putAll(input.data!!)
                        }
                    }
                    input
                }
            }
            return field
        }
        private set
    private val unreadMessages = HashMap<String, Int>()


    fun startRefresh() {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            conversationRepository!!.actionGetConversations(userLocal.token!!)
        } else {
            listData!!.setValue(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    /**
     * 绑定服务
     *
     * @param context Activity对象
     */
    fun bindService(context: Context?) {
        conversationRepository!!.bindService(context!!)
    }

    fun unbindService(context: Context?) {
        conversationRepository!!.unbindService(context!!)
    }

    /**
     * 获取某个对话的未读数量
     *
     * @return 未读消息数目
     */
    fun getUnreadNumber(conversation: Conversation): Int {
        val res = unreadMessages[conversation.id]
        //        for(ChatMessage cm:unreadMessages){
//            if(Objects.equals(cm.getConversationId(),conversation.getId())){
//                res++;
//            }
//        }
        return res ?: 0
    }

    fun callOnline(context: Context) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            conversationRepository!!.actionCallOnline(context, userLocal)
        }
    }

}