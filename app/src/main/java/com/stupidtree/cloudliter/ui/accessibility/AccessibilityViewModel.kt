package com.stupidtree.cloudliter.ui.accessibility

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.ProfileRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.Trigger
import com.stupidtree.cloudliter.ui.myprofile.TypeTrigger

class AccessibilityViewModel(application: Application) : AndroidViewModel(application) {

    private val profileRepository = ProfileRepository.getInstance(application)
    private val localUserRepository = LocalUserRepository.getInstance(application)

    /**
     * 数据区
     */
    private val refreshController = MutableLiveData<Trigger>()
    val myProfileLiveData = Transformations.switchMap(refreshController) {
        val user = localUserRepository.getLoggedInUser()
        if (user.isValid) {
            return@switchMap profileRepository.getUserProfile(user.id!!, user.token!!)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }
    var changeTypeController = MutableLiveData<TypeTrigger>()

    //状态数据：变更用户类型的结果
    var changeTypeResult: LiveData<DataState<String?>> = Transformations.switchMap(changeTypeController)
    { input: TypeTrigger ->
        if (input.isActioning) {
            val userLocal = localUserRepository.getLoggedInUser()
            if (userLocal.isValid) {
                return@switchMap profileRepository.changeUserType(userLocal.token!!, input.type, input.subType, input.typePermission)
            } else {
                return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
            }
        }
        MutableLiveData()
    }


    /**
     * 方法区
     */

    fun startRefresh() {
        refreshController.value = Trigger.actioning
    }

    /**
     * 发起更换用户类型及隐私类型请求
     * @param type 用户类型
     * @param subType 用户类型详细分类
     * @param typePermission 无障碍隐私类型
     */
    fun startChangeType(type: Int, subType: String?, typePermission: UserLocal.TYPEPERMISSION) {
        changeTypeController.value = TypeTrigger.getActioning(type, subType, typePermission.name)
    }
}