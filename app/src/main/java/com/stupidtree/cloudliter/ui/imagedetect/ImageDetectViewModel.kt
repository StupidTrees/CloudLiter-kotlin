package com.stupidtree.cloudliter.ui.imagedetect

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.repository.AiRepository
import com.stupidtree.cloudliter.data.repository.ImageRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState

class ImageDetectViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 倉庫區
     */
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)
    private val aiRepository: AiRepository = AiRepository.getInstance(application)
    private val imageRepository = ImageRepository.getInstance(application)


    var imageIdLiveData = MutableLiveData<Pair<String,Boolean>>()
    var imageLiveData = MutableLiveData<Bitmap>()
    var detectionResult = Transformations.switchMap(imageLiveData) {
        return@switchMap aiRepository.detectImage(it)
    }

    val imageEntityLiveData = Transformations.switchMap(imageIdLiveData) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap imageRepository.getImageInfo(userLocal.token!!, it.first)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

    var imageClassifyResult = Transformations.switchMap(imageIdLiveData) { input ->
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap aiRepository.imageClassify(userLocal.token!!, input.first)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }
}