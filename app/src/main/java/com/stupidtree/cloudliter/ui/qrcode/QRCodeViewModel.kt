package com.stupidtree.cloudliter.ui.qrcode

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.Trigger
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils
import java.util.*

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {
    var localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)


    var imageLiveData: LiveData<DataState<Bitmap?>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(imageRefreshController) {
                    val userLocal = localUserRepository.getLoggedInUser()
                    if (userLocal.isValid) {
                        return@switchMap MutableLiveData(DataState(
                                    ImageUtils.createQRCodeBitmap(
                                           TextUtils.encodeUserBusinessCard(userLocal),
                                            800, 800,
                                            null, "H", "0", Color.BLACK, Color.WHITE)
                            ))

                    }
                    return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
                }
            }
            return field!!
        }


    fun getLoggedInUser():UserLocal{
        return localUserRepository.getLoggedInUser()
    }

    var imageRefreshController: MutableLiveData<Trigger> = MutableLiveData()

    fun startRefresh() {
        imageRefreshController.value = Trigger.actioning
    }
}