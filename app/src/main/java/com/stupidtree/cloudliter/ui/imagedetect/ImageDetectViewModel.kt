package com.stupidtree.cloudliter.ui.imagedetect

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.repository.AiRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState

class ImageDetectViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 倉庫區
     */
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)
    private val aiRepository: AiRepository = AiRepository.getInstance(application)

    var imageUrl = MutableLiveData<String>()
    var imageLiveData = MutableLiveData<Bitmap>()
    var chatMessageLiveData = MutableLiveData<ChatMessage?>()
    var detectionResult = Transformations.switchMap(imageLiveData) {
        return@switchMap aiRepository.detectImage(it)
    }
    var imageClassifyResult = Transformations.switchMap(imageLiveData) { input ->
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap aiRepository.imageClassify(userLocal.token!!, input)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

}