package com.stupidtree.cloudliter.ui.conversation

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.ConversationRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.StringTrigger
import java.util.*

class ConversationViewModel(application: Application?) : AndroidViewModel(application!!) {
    /**
     * 数据区
     */
    var conversationLiveData: LiveData<DataState<Conversation?>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(conversationTrigger){ input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap repository!!.queryConversation(userLocal.token!!, userLocal.id, input.data!!)
                        } else {
                            return@switchMap MutableLiveData(DataState<Conversation?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }else{
                        return@switchMap MutableLiveData(DataState<Conversation?>(DataState.STATE.NOTHING))
                    }
                }
            }
            return field
        }
        private set

    //数据本体：聊天词云
    var wordCloudLiveData: LiveData<DataState<HashMap<String, Float>>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(conversationTrigger) { input: StringTrigger ->
                    val user = localUserRepository.getLoggedInUser()
                    if (input.isActioning) {
                        if (user.isValid) {
                            return@switchMap repository!!.getUserWordCloud(user.token, user.id!!, input.data!!)
                        } else {
                            return@switchMap MutableLiveData(DataState<HashMap<String, Float>>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData(DataState<HashMap<String, Float>>(DataState.STATE.NOTHING))
                }
            }
            return field
        }
        private set
    private val conversationTrigger = MutableLiveData<StringTrigger>()

    /**
     * 仓库区
     */
    private val repository: ConversationRepository?
    private val localUserRepository: LocalUserRepository

    private val conversationId: String?
        get() {
            if (conversationLiveData!!.value != null) {
                val c = conversationLiveData!!.value!!.data
                if (c != null) {
                    return c.id
                }
            }
            return null
        }

    fun startRefresh(friendId: String?) {
        conversationTrigger.value = StringTrigger.getActioning(friendId)
    }

    init {
        repository = getInstance(application!!)
        localUserRepository = LocalUserRepository.getInstance(application)
    }
}