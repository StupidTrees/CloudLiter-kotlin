package com.stupidtree.cloudliter.ui.conversation.normal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.ConversationRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.StringTrigger
import java.util.*

class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 数据区
     */

    private val conversationIdLiveData = MutableLiveData<String>()

    var conversationLiveData: LiveData<DataState<Conversation?>> = Transformations.switchMap(conversationIdLiveData) { input ->
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap repository.queryConversation(userLocal.token!!, input)
        } else {
            return@switchMap MutableLiveData(DataState<Conversation?>(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    //数据本体：聊天词云
    var wordCloudLiveData: LiveData<DataState<HashMap<String, Float?>?>> = Transformations.switchMap(conversationIdLiveData) { input ->
        val user = localUserRepository.getLoggedInUser()
        if (user.isValid) {
            return@switchMap repository.getUserWordCloud(user.token, input)
        } else {
            return@switchMap MutableLiveData(DataState<HashMap<String, Float?>?>(DataState.STATE.NOT_LOGGED_IN))
        }
    }


/**
 * 仓库区
 */
private val repository: ConversationRepository = getInstance(application)
private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)

fun startRefresh(conversationId: String) {
    conversationIdLiveData.value = conversationId
}

}