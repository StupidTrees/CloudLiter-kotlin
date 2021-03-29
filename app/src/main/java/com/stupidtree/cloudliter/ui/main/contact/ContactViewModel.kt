package com.stupidtree.cloudliter.ui.main.contact

import android.app.Application
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.repository.GroupChatRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.RelationRepository
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.Trigger
import java.util.*

/**
 * 层次：ViewModel
 * 联系人页面Fragment所绑定的ViewModel
 */
class ContactViewModel(application: Application) : AndroidViewModel(application) {//switchMap的作用是
    /**
     * 仓库区
     */
    //仓库2：关系仓库
    private val relationRepository: RelationRepository = RelationRepository.getInstance(application)

    private val groupChatRepository = GroupChatRepository.getInstance(application)

    //仓库2：本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)


    /**
     * 未读好友事件数
     */

    private val unReadController = MutableLiveData<Trigger>()
    //数据本体：未读好友事件数
    var unReadLiveData: LiveData<DataState<Int?>> = Transformations.switchMap(unReadController) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap relationRepository.countUnread(userLocal.token!!)
        } else {
            return@switchMap MutableLiveData(DataState<Int?>(DataState.STATE.NOT_LOGGED_IN))
        }
    }


    private val createGroupController = MutableLiveData<List<String>>()
    val createGroupChatResult = Transformations.switchMap(createGroupController) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap groupChatRepository.createGroupChat(userLocal.token!!,"新建群聊",it)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }

    }


    /**
     * 开始获取未读事件数目
     */
    fun startFetchUnread() {
        unReadController.value = Trigger.actioning
    }

    fun startCreateGroup(list:List<String>){
        createGroupController.value = list
    }
}