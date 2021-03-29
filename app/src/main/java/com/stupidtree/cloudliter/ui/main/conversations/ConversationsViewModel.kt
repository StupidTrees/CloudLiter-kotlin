package com.stupidtree.cloudliter.ui.main.conversations

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.ConversationRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.utils.MTransformations
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.Trigger
import java.util.*

class ConversationsViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */
    //对话仓库
    private val conversationRepository: ConversationRepository = getInstance(application)

    //本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)


    /**
     * 数据区
     */
    private val refreshController = MutableLiveData<Trigger>()
    //数据本体：列表数据
    var listData: LiveData<DataState<MutableList<Conversation>?>> = Transformations.switchMap(refreshController){
        val userLocal = localUserRepository.getLoggedInUser()
        return@switchMap if (userLocal.isValid) {
            conversationRepository.getConversations(userLocal.token!!)
        } else {
            MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    //数据本体：未读消息
    var unreadMessageState: LiveData<DataState<HashMap<String, Int>>> = Transformations.map(conversationRepository.unreadMessageState) { input ->
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
    private val unreadMessages = HashMap<String, Int>()


    fun startRefresh() {
        refreshController.value = Trigger.actioning
    }

    /**
     * 绑定服务
     *
     * @param context Activity对象
     */
    fun bindService(context: Context?) {
        conversationRepository.bindService(context!!)
    }

    fun unbindService(context: Context?) {
        conversationRepository.unbindService(context!!)
    }

    /**
     * 获取某个对话的未读数量
     *
     * @return 未读消息数目
     */
    fun getUnreadNumber(conversation: Conversation): Int {
        val res = unreadMessages[conversation.id]
        return res ?: 0
    }

    fun callOnline(context: Context) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            conversationRepository.actionCallOnline(context, userLocal)
        }
    }

}