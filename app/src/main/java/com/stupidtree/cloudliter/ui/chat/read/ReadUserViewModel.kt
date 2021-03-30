package com.stupidtree.cloudliter.ui.chat.read

import android.app.Application
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState

/**
 * 层次：ViewModel
 * 联系人页面Fragment所绑定的ViewModel
 */
class ReadUserViewModel(application: Application) : AndroidViewModel(application) {//switchMap的作用是

    /**
     * 仓库区
     */

    private val conversationRepository = ConversationRepository.getInstance(application)

    //仓库2：本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)


    /**
     * 数据区
     */
    //数据本体:联系人列表
    private val refreshController = MutableLiveData<Triple<String,String,Boolean>>()
    var listData: LiveData<DataState<List<ReadUser>>> = Transformations.switchMap(refreshController) {
        val userLocal = localUserRepository.getLoggedInUser()
        return@switchMap if (userLocal.isValid) {
            conversationRepository.getReadUsers(userLocal.token!!, it.first,it.second,it.third)
        } else {
            MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }


    /**
     * 开始刷新列表数据
     */
    fun startFetchData(messageId: String,conversationId:String,read:Boolean) {
        refreshController.value = Triple(messageId,conversationId,read)
    }

}