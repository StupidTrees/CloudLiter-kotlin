package com.stupidtree.cloudliter.ui.conversation.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.ConversationRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.GroupChatRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.StringTrigger
import com.stupidtree.component.data.Trigger
import java.util.*

class ConversationGroupViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */
    private val repository: ConversationRepository = getInstance(application)
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)
    private val groupChatRepository = GroupChatRepository.getInstance(application)


    /**
     * 数据区
     */

    private val conversationIdLiveData = MutableLiveData<String>()
    private val groupIdLiveData = MutableLiveData<String>()
    private val renameGroupController = MutableLiveData<String>()

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

    //群成员列表
    var groupMembersLiveData = Transformations.switchMap(groupIdLiveData) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap groupChatRepository.getAllGroupMembers(userLocal.token!!, it)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    //群资料
    var groupInfoLiveData = Transformations.switchMap(groupIdLiveData) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap groupChatRepository.getGroupInfo(userLocal.token!!, it)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    var renameGroupResult = Transformations.switchMap(renameGroupController) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap groupChatRepository.renameGroup(userLocal.token!!, it, groupIdLiveData.value
                    ?: "")
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }


    //Trigger：控制更改头像请求的发送，其中携带了新头像文件的路径字符串
    var changeAvatarController = MutableLiveData<StringTrigger>()

    //状态数据：更改头像的结果
    var changeAvatarResult: LiveData<DataState<String?>> = Transformations.switchMap(changeAvatarController) { input: StringTrigger ->
        //要先判断本地用户当前是否登录
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            //通知用户资料仓库，开始更换头像
            return@switchMap groupIdLiveData.value?.let { groupChatRepository.changeAvatar(userLocal.token!!, it,input.data) }
        } else {
            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
        }
    }


    private val quitGroupTrigger = MutableLiveData<Trigger>()
    private val destroyGroupTrigger = MutableLiveData<Trigger>()

    var destroyGroupResult = Transformations.switchMap(destroyGroupTrigger) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap groupChatRepository.destroyGroup(userLocal.token!!,  groupIdLiveData.value
                    ?: "")
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }


    var quitGroupResult = Transformations.switchMap(quitGroupTrigger) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap groupChatRepository.quitGroup(userLocal.token!!,  groupIdLiveData.value
                    ?: "")
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    /**
     * 我是群主吗？
     */
    fun isMeTheMaster():Boolean{
        return groupInfoLiveData.value?.data?.master == localUserRepository.getLoggedInUser().id
    }


    fun startRefresh(conversationId: String, groupId: String) {
        conversationIdLiveData.value = conversationId
        groupIdLiveData.value = groupId
    }



    fun startRenameGroup(name: String) {
        renameGroupController.value = name
    }

    /**
     * 发起更换头像请求
     * @param path 新头像的路径
     */
    fun startChangeAvatar(path: String) {
        changeAvatarController.value = StringTrigger.getActioning(path)
    }

    fun startQuitGroup(){
        quitGroupTrigger.value = Trigger.actioning

    }

    fun startDestroyGroup(){
        destroyGroupTrigger.value = Trigger.actioning
    }
}