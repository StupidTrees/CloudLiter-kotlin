package com.stupidtree.cloudliter.ui.main.contact.group

import android.app.Application
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.repository.FriendsRepository
import com.stupidtree.cloudliter.data.repository.GroupRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.Trigger

/**
 * 层次：ViewModel
 * 联系人页面Fragment所绑定的ViewModel
 */
class ContactGroupViewModel(application: Application) : AndroidViewModel(application) {



    /**
     * 仓库区
     */
    //仓库1：好友仓库
    private val friendsRepository: FriendsRepository = FriendsRepository.getInstance(application)

    //仓库2：本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)

    //仓库3：好友分组仓库
    private val groupRepository: GroupRepository = GroupRepository.instance!!
    /**
     * 数据区
     */
    //数据本体:联系人列表
    var listData: MediatorLiveData<DataState<List<UserRelation>?>> = friendsRepository.friendsLiveData


    /**
     * 开始刷新列表数据
     */
    fun startFetchData() {
        val userLocal = localUserRepository.getLoggedInUser()
        if(userLocal.isValid){
            friendsRepository.actionGetFriends(userLocal.token!!)
        }else{
            listData.value = DataState(DataState.STATE.NOT_LOGGED_IN)
        }
    }


}