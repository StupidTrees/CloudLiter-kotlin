package com.stupidtree.cloudliter.ui.face.permission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.repository.ImageRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.StringTrigger
import com.stupidtree.component.data.Trigger

class FaceWhiteListViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 仓库区
     */
    private val imageRepository = ImageRepository.getInstance(application)
    val localUserRepository = LocalUserRepository.getInstance(application)


    /**
     * 数据区
     */
    private val refreshController = MutableLiveData<Trigger>()
    private var deleteController = MutableLiveData<StringTrigger>()
    private val addController = MutableLiveData<List<String>>()
    val whiteListLiveData = Transformations.switchMap(refreshController) {
        val user = localUserRepository.getLoggedInUser()
        if (user.isValid) {
            return@switchMap imageRepository.getWhiteList(user.token!!)
        }else{
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }
    val addWhitelistResult= Transformations.switchMap(addController) {
        val user = localUserRepository.getLoggedInUser()
        if (user.isValid) {
            return@switchMap imageRepository.addToWhiteList(user.token!!,it)
        }else{
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }
    /**
     * 词云删除结果
     */
    var deleteResult = Transformations.switchMap(deleteController){
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap imageRepository.deleteFromWhiteList(userLocal.token!!,it.data)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    /**
     * 进行刷新
     */
    fun startRefresh() {
        refreshController.value = Trigger.actioning
    }

    /**
     * 进行删除
     */
    fun deleteFaceWhiteList(friendId:String){
        deleteController.value = StringTrigger.getActioning(friendId)
    }


    /**
     * 添加白名单
     */
    fun addWhitelist(list:List<String>){
        addController.value = list
    }

}