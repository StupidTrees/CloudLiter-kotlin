package com.stupidtree.cloudliter.ui.main.contact.popup

import android.app.Application
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.repository.FriendsRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState

/**
 * 层次：ViewModel
 * 联系人页面Fragment所绑定的ViewModel
 */
class PickFriendViewModel(application: Application) : AndroidViewModel(application) {//switchMap的作用是

    /**
     * 仓库区
     */
    //仓库1：好友仓库
    private val friendsRepository = FriendsRepository.getInstance(application)

    //仓库2：本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)


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