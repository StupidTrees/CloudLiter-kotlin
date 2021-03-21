package com.stupidtree.cloudliter.ui.gallery.faces

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.repository.ImageRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.Trigger

class FriendFacesViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 仓库区
     */
    private val imageRepository = ImageRepository.getInstance(application)
    private val localUserRepository = LocalUserRepository.getInstance(application)

    /**
     * LiveData区
     */

    private val refreshController = MutableLiveData<Trigger>()
    val imagesLiveData = Transformations.switchMap(refreshController) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap imageRepository.getFriendFaces(userLocal.token!!)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }


    /**
     * 方法
     */
    fun startRefresh(){
        refreshController.value = Trigger.actioning
    }

}