package com.stupidtree.cloudliter.ui.imagedetect

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.repository.AiRepository
import com.stupidtree.cloudliter.data.repository.DetectionRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.StringTrigger

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
    var aiImageClassify = MutableLiveData<String>()
//    var aiImageClassifyResult = Transformations.switchMap(imageLiveData){
//        return@switchMap aiRepository.ActionSendAiImage(getApplication(), localUserRepository.getLoggedInUser().token!!, )
//    }

    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)
    private val aiRepository: AiRepository = AiRepository.getInstance(application)
    private val imageSendController = MutableLiveData<StringTrigger>()

    fun getAiImageClassifyResult(): LiveData<DataState<String?>> {
        return Transformations.switchMap(imageSendController) { input: StringTrigger ->
            if (input.isActioning) {
                val userLocal = localUserRepository.getLoggedInUser()
                if (userLocal.isValid) {
                    while (false) {
                        break
                    }
                    Log.e("LiveData::", imageLiveData.value.toString())
                    return@switchMap aiRepository.ActionSendAiImage(getApplication(), userLocal.token!!, input.data, imageLiveData.value)
                } else {
                    return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                }
            }
            MutableLiveData(DataState(DataState.STATE.NOTHING))
        }
    }

    fun sendImageMessage(path: String?) {
        imageSendController.value = path?.let { StringTrigger.getActioning(it) }
    }
}