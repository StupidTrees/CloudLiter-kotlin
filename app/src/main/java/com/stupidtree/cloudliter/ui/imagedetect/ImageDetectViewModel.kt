package com.stupidtree.cloudliter.ui.imagedetect

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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


    var imageIdLiveData = MutableLiveData<Pair<String, Boolean>>()
    var imageLiveData = MutableLiveData<Bitmap>()
    var detectionResult: LiveData<DataState<List<DetectResult>>> = Transformations.switchMap(imageLiveData) {
        return@switchMap Transformations.switchMap(aiRepository.detectImage(it)) { d ->
            val r = mutableListOf<DetectResult>()
            d.data?.let { list ->
                for (x in list) {
                    r.add(DetectResult(x))
                }
            }
            return@switchMap MutableLiveData(DataState(r))
        }
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


    val faceRecognitionResult = Transformations.switchMap(detectionResult) { list ->
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            imageIdLiveData.value?.let { id ->
                return@switchMap list.data?.let { aiRepository.imageFaceRecognition(userLocal.token!!, id.first, it) }
            } ?: run {
                return@switchMap MutableLiveData(DataState(DataState.STATE.FETCH_FAILED))
            }
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }

    }
}