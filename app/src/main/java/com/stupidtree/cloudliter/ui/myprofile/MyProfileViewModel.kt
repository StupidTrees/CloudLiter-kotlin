package com.stupidtree.cloudliter.ui.myprofile

import android.app.Application
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.ProfileRepository
import com.stupidtree.cloudliter.data.repository.ProfileRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.ProfileRepository.Companion.instance
import com.stupidtree.component.data.BooleanTrigger
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.StringTrigger
import java.util.*

/**
 * 层次：ViewModel
 * 和”我的资料“Activity绑定的ViewModel
 */
class MyProfileViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */
    //仓库1：用户资料仓库
    private val profileRepository: ProfileRepository = getInstance(application)

    //仓库2：本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)



    /**
     * 数据区
     */

    //Trigger：控制↑的刷新
    var profileController = MutableLiveData<StringTrigger>()
    //数据本体：我的用户资料
    var userProfileLiveData: LiveData<DataState<UserProfile?>> = Transformations.switchMap(profileController) { input ->
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            //从用户资料仓库总取出数据
            return@switchMap profileRepository.getUserProfile(userLocal.id!!, userLocal.token!!)
        } else {
            return@switchMap MutableLiveData(DataState<UserProfile?>(DataState.STATE.NOT_LOGGED_IN))
        }
    }



    //状态数据：更改头像的结果
    var changeAvatarResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                //controller改变时。。。巴拉巴拉
                changeAvatarResult = Transformations.switchMap(changeAvatarController) { input: StringTrigger ->
                    if (input.isActioning) {
                        //要先判断本地用户当前是否登录
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            //通知用户资料仓库，开始更换头像
                            return@switchMap profileRepository.changeAvatar(userLocal.token!!, input.data)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    } else {
                        return@switchMap MutableLiveData<DataState<String?>>()
                    }
                }
            }
            return field!!
        }

    //Trigger：控制更改头像请求的发送，其中携带了新头像文件的路径字符串
    var changeAvatarController = MutableLiveData<StringTrigger>()

    //状态数据：更改昵称的结果
    var changeNicknameResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                //也是一样的
                changeNicknameResult = Transformations.switchMap(changeNicknameController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap profileRepository.changeNickname(userLocal.token!!, input.data)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    return@switchMap MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制更改昵称请求的发送，其中携带了新昵称字符串
    var changeNicknameController = MutableLiveData<StringTrigger>()


    //状态数据：更改性别的结果
    var changeGenderResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                changeGenderResult = Transformations.switchMap(changeGenderController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap profileRepository.changeGender(userLocal.token!!, input.data)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制更改性别请求的发送，其中携带了新性别字符串
    var changeGenderController = MutableLiveData<StringTrigger>()

    //状态数据：更改颜色结果
    var changeAccessibilityResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                changeAccessibilityResult = Transformations.switchMap(changeAccessibilityController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap profileRepository.changeUserAccessibility(userLocal.token!!, input.data)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制更改颜色请求的发送，其中携带了新颜色的字符串
    var changeAccessibilityController = MutableLiveData<StringTrigger>()

    //状态数据：更改签名的结果
    var changeSignatureResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                //也是一样的
                changeSignatureResult = Transformations.switchMap(changeSignatureController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap profileRepository.changeSignature(userLocal.token!!, input.data)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制更改签名请求的发送，其中携带了新昵称字符串
    private var changeSignatureController = MutableLiveData<StringTrigger>()

    //状态数据：变更用户类型的结果
    var changeTypeResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                changeTypeResult = Transformations.switchMap(changeTypeController) { input: TypeTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap profileRepository.changeUserType(userLocal.token!!, input.type, input.subType, input.typePermission)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制更改用户类型请求的发送，其中携带了用户类型、用户详细类型、隐私类型
    var changeTypeController = MutableLiveData<TypeTrigger>()


    //状态数据：更改签名的结果
    var setWCAccessibilityResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                //也是一样的
                setWCAccessibilityResult = Transformations.switchMap(changeWCAccessibilityController) { input ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap profileRepository.setWordCloudAccessibility(userLocal.token!!, input.data)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData()
                }
            }
            return field!!
        }

    //Trigger：控制↑
    var changeWCAccessibilityController = MutableLiveData<BooleanTrigger>()


    /**
     * 发起更换头像请求
     * @param path 新头像的路径
     */
    fun startChangeAvatar(path: String) {
        changeAvatarController.value = StringTrigger.getActioning(path)
    }

    /**
     * 发起更换昵称请求
     * @param nickname 新昵称字符串
     */
    fun startChangeNickname(nickname: String) {
        changeNicknameController.value = StringTrigger.getActioning(nickname)
    }

    /**
     * 发起更换性别请求
     * @param gender 新性别
     */
    fun startChangeGender(gender: GENDER) {
        val genderStr = if (gender === GENDER.MALE) "MALE" else "FEMALE"
        changeGenderController.value = StringTrigger.getActioning(genderStr)
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

    /**
     * 发起更换签名请求
     * @param signature 新签名字符串
     */
    fun startChangeSignature(signature: String) {
        changeSignatureController.value = StringTrigger.getActioning(signature)
    }

    /**
     * 发起更改词云可见性请求
     */
    fun startChangeWCAccessibility(private: Boolean) {
        changeWCAccessibilityController.value = BooleanTrigger.getActioning(private)
    }

    /**
     * 开始页面刷新（即用户profile的获取）
     */
    fun startRefresh() {
        val userLocal = localUserRepository.getLoggedInUser()
        userLocal.id?.let {
            profileController.value = StringTrigger.getActioning(it)
        }

    }

}