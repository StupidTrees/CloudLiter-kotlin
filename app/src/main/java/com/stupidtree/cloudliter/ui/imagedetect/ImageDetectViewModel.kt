package com.stupidtree.cloudliter.ui.imagedetect

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.repository.DetectionRepository

class ImageDetectViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 倉庫區
     */
    private val detectionRepository = DetectionRepository.getInstance(application)


    var imageUrl = MutableLiveData<String>()
    var imageLiveData = MutableLiveData<Bitmap>()
    var chatMessageLiveData = MutableLiveData<ChatMessage?>()
    var detectionResult = Transformations.switchMap(imageLiveData){
        return@switchMap detectionRepository.detectImage(it)
    }
}