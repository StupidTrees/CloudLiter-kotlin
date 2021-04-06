package com.stupidtree.cloudliter.ui.groupchat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.repository.FriendsRepository
import com.stupidtree.cloudliter.data.repository.GroupChatRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState

class CreateGroupChatViewModel(application: Application) : AndroidViewModel(application) {


    private val groupChatRepository = GroupChatRepository.getInstance(application)

    //仓库2：本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)

    //仓库1：好友仓库
    private val friendsRepository = FriendsRepository.getInstance(application)


    /**
     * 数据区
     */
    //数据本体:联系人列表
    var listData: MediatorLiveData<DataState<List<UserRelation>?>> = friendsRepository.friendsLiveData


    private val createGroupController = MutableLiveData<Pair<List<String>,String>>()
    val createGroupChatResult = Transformations.switchMap(createGroupController) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap groupChatRepository.createGroupChat(userLocal.token!!,it.second,it.first)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }

    }


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

    fun startCreateGroup(list:List<String>,name:String){
        createGroupController.value = Pair(list,name)
    }
}