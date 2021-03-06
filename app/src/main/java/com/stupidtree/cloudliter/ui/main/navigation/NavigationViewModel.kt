package com.stupidtree.cloudliter.ui.main.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.Trigger

class NavigationViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */

    //本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)

    /**
     * 数据区
     */
    private val refreshController = MutableLiveData<Trigger>()
    val localUserLiveData: LiveData<UserLocal> = Transformations.switchMap(refreshController) {
        return@switchMap MutableLiveData(localUserRepository.getLoggedInUser())
    }

    /**
     * 方法区
     */
    fun startRefresh(){
        refreshController.value = Trigger.actioning
    }

}