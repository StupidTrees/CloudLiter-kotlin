package com.stupidtree.cloudliter.ui.face

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.repository.AiRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.StringTrigger

class MyFaceViewModel(application: Application) : AndroidViewModel(application) {
    val localUserRepository = LocalUserRepository.getInstance(application)
    private val aiRepository = AiRepository.getInstance(application)

    var uploadFaceController = MutableLiveData<StringTrigger>()

    //状态数据：更改头像的结果
    var uploadFaceResult: LiveData<DataState<String?>> = Transformations.switchMap(uploadFaceController) { input: StringTrigger ->
        if (input.isActioning) {
            //要先判断本地用户当前是否登录
            val userLocal = localUserRepository.getLoggedInUser()
            if (userLocal.isValid) {
                return@switchMap aiRepository.uploadFaceImage(application, userLocal.token!!, input.data)
            } else {
                return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
            }
        } else {
            return@switchMap MutableLiveData<DataState<String?>>()
        }
    }

    fun startUploadFace(path:String){
        uploadFaceController.value = StringTrigger.getActioning(path)
    }
}