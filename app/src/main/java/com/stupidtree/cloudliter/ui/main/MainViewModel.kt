package com.stupidtree.cloudliter.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.repository.LocalUserRepository

class MainViewModel(application: Application?) : AndroidViewModel(application!!) {
    /**
     * 仓库区
     */
    //本地用户仓库
    var localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application!!)
    val localUser: UserLocal
        get() = localUserRepository.getLoggedInUser()

}