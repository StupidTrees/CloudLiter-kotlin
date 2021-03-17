package com.stupidtree.cloudliter.ui.wordcloud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.ProfileRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.StringTrigger
import com.stupidtree.cloudliter.ui.base.Trigger

class WordCloudViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 仓库区
     */
    private val profileRepository = ProfileRepository.getInstance(application)
    val localUserRepository = LocalUserRepository.getInstance(application)


    /**
     * 数据区
     */
    private val wordCloudRefreshController = MutableLiveData<Trigger>()
    private var deleteController = MutableLiveData<StringTrigger>()
    val wordCloudLiveData = Transformations.switchMap(wordCloudRefreshController) {
        val user = localUserRepository.getLoggedInUser()
        if (user.isValid) {
            return@switchMap profileRepository.getUserWordCloud(user.token!!, user.id!!)
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
            return@switchMap profileRepository.deleteWordCloud(userLocal.token!!,userLocal.id!!,it.data)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    /**
     * 进行刷新
     */
    fun startRefresh() {
        wordCloudRefreshController.value = Trigger.actioning
    }

    /**
     * 进行词云删除
     */
    fun deleteWordCloud(word:String){
        deleteController.value = StringTrigger.getActioning(word)
    }



}