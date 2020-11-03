package com.stupidtree.cloudliter.ui.welcome.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.UserRepository
import com.stupidtree.cloudliter.data.repository.UserRepository.Companion.getInstance
import com.stupidtree.cloudliter.ui.welcome.signup.SignUpTrigger.Companion.getRequestState
import com.stupidtree.cloudliter.utils.TextUtils

/**
 * 层次：ViewModel
 * 登录界面的ViewModel
 */
class SignUpViewModel(application: Application?) : AndroidViewModel(application!!) {
    /**
     * 数据区
     */
    //数据本体：登录表单
    private val signUpFormState = MutableLiveData<SignUpFormState>()

    //Trigger：控制登录的进行
    private val signUpController = MutableLiveData<SignUpTrigger>()

    /**
     * 仓库区
     */
    //用户仓库
    private val userRepository: UserRepository?
    private val localUserRepository: LocalUserRepository
    val loginFormState: LiveData<SignUpFormState>
        get() = signUpFormState

    //通知用户仓库进行注册，并从中获取返回结果的liveData
    val signUpResult: LiveData<SignUpResult?>
        get() = Transformations.switchMap<SignUpTrigger, SignUpResult?>(signUpController) { input ->
            if (input.isActioning) {
                //通知用户仓库进行注册，并从中获取返回结果的liveData
                return@switchMap userRepository!!.signUp(input.username, input.password, input.getGender(), input.nickname)
            }
            MutableLiveData(null)
        }

    /**
     * 进行注册请求
     * @param username 用户名
     * @param password 密码
     * @param gender 性别
     * @param nickname 昵称
     */
    fun signUp(username: String?, password: String?,
               gender: GENDER?, nickname: String?
    ) {
        signUpController.value = getRequestState(
                username, password, gender, nickname)
    }

    /**
     * 当注册信息表变更时，通知viewModel变更数据
     * @param username 用户名
     * @param password 密码
     * @param passwordConfirm 确认密码
     * @param nickname 昵称
     */
    fun signUpDataChanged(username: String?, password: String?, passwordConfirm: String?, nickname: String?) {
        //检查输入合法性，若合法则更新登录表单
        if (!TextUtils.isUsernameValid(username)) {
            signUpFormState.setValue(
                    SignUpFormState(R.string.invalid_username, null, null, null))
        } else if (!TextUtils.isPasswordValid(password)) {
            signUpFormState.setValue(
                    SignUpFormState(null, R.string.invalid_password, null, null))
        } else if (password != passwordConfirm) {
            signUpFormState.setValue(
                    SignUpFormState(null, null, R.string.inconsistent_password, null))
        } else if (TextUtils.isEmpty(nickname)) {
            signUpFormState.setValue(
                    SignUpFormState(null, null, null, R.string.empty_nickname))
        } else {
            signUpFormState.setValue(SignUpFormState(true))
        }
    }

    init {
        userRepository = getInstance(application!!)
        localUserRepository = LocalUserRepository.getInstance(application)
    }
}