package com.stupidtree.cloudliter.ui.main.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository

class NavigationViewModel(application: Application): AndroidViewModel(application) {
    /**
     * 仓库区
     */

    //本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)
}