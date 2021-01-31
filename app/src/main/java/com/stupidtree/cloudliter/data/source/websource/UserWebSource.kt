package com.stupidtree.cloudliter.data.source.websource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.UserLocal.Companion.getFromResponseData
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.data.model.UserSearched
import com.stupidtree.cloudliter.data.source.websource.service.LiveDataCallAdapter
import com.stupidtree.cloudliter.data.source.websource.service.UserService
import com.stupidtree.cloudliter.data.source.websource.service.codes.SUCCESS
import com.stupidtree.cloudliter.data.source.websource.service.codes.TOKEN_INVALID
import com.stupidtree.cloudliter.data.source.websource.service.codes.USER_ALREADY_EXISTS
import com.stupidtree.cloudliter.data.source.websource.service.codes.WORD_CLOUD_PRIVATE
import com.stupidtree.cloudliter.data.source.websource.service.codes.WRONG_PASSWORD
import com.stupidtree.cloudliter.data.source.websource.service.codes.WRONG_USERNAME
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.welcome.login.LoginResult
import com.stupidtree.cloudliter.ui.welcome.signup.SignUpResult
import com.stupidtree.cloudliter.utils.JsonUtils
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

/**
 * 层次：DataSource
 * 用户的数据源
 * 类型：网络数据
 * 数据：异步读，异步写
 */
class UserWebSource : BaseWebSource<UserService>(Retrofit.Builder()
        .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://hita.store:3000").build()) {
    override fun getServiceClass(): Class<UserService> {
        return UserService::class.java
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    fun login(username: String?, password: String?): LiveData<LoginResult> {
        return Transformations.map(service.login(username, password)) { input: ApiResponse<JsonObject?>? ->
            val loginResult = LoginResult()
            if (null == input) {
                loginResult[LoginResult.STATES.ERROR] = R.string.login_failed
            } else {
                when (input.code) {
                    SUCCESS -> {
                        Log.e("RESPONSE", "登录成功")
                        val token = input.data?.let {
                            JsonUtils.getStringData(
                                    it, "token")
                        }
                        if (null == token) {
                            Log.e("RESPONSE", "没有找到token")
                            loginResult[LoginResult.STATES.ERROR] = R.string.login_failed
                        } else {
                            loginResult[LoginResult.STATES.SUCCESS] = R.string.login_success
                            loginResult.token = token
                            loginResult.userLocal = getFromResponseData(input.data)
                        }
                    }
                    WRONG_USERNAME -> {
                        Log.e("RESPONSE", "用户名错误")
                        loginResult[LoginResult.STATES.WRONG_USERNAME] = R.string.login_failed_wrong_username
                    }
                    WRONG_PASSWORD -> {
                        Log.e("RESPONSE", "密码错误")
                        loginResult[LoginResult.STATES.WRONG_PASSWORD] = R.string.login_failed_wrong_password
                    }
                    else -> loginResult[LoginResult.STATES.ERROR] = R.string.login_failed
                }
            }
            loginResult
        }
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param gender 性别 MALE/FEMALE
     * @param nickname 昵称
     * @return 注册结果
     */
    fun signUp(username: String?, password: String?, gender: String?, nickname: String?): LiveData<SignUpResult?> {
        return Transformations.map(service.signUp(username, password, gender, nickname)
        ) { input: ApiResponse<JsonObject?>? ->
            val signUpResult = SignUpResult()
            if (input != null) {
                when (input.code) {
                    SUCCESS -> {
                        Log.e("RESPONSE", "注册成功")
                        val token = input.data?.let {
                            JsonUtils.getStringData(
                                    it, "token")
                        }
                        if (null == token) {
                            Log.e("RESPONSE", "没有找到token")
                            signUpResult[SignUpResult.STATES.ERROR] = R.string.sign_up_failed
                        } else {
                            signUpResult[SignUpResult.STATES.SUCCESS] = R.string.sign_up_success
                        }
                        signUpResult.userLocal = getFromResponseData(input.data)
                    }
                    USER_ALREADY_EXISTS -> signUpResult[SignUpResult.STATES.USER_EXISTS] = R.string.user_already_exists
                    else -> signUpResult[SignUpResult.STATES.ERROR] = R.string.sign_up_failed
                }
            } else {
                signUpResult[SignUpResult.STATES.ERROR] = R.string.sign_up_failed
            }
            signUpResult
        }
    }

    /**
     * 搜索用户
     * @param text 检索语句
     * @param token 令牌
     * @return 搜索结果
     */
    fun searchUser(text: String?, token: String?): LiveData<DataState<List<UserSearched>?>> {
        return Transformations.map(service.searchUser(text, token)) { input->
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState<List<UserSearched>?>(input.data!!)
                    TOKEN_INVALID -> return@map DataState<List<UserSearched>?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<List<UserSearched>?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    /**
     * 按照词云搜索用户
     * @param text 检索语句
     * @param token 令牌
     * @return 搜索结果
     */
    fun searchUserByWordCloud(text: String?, token: String?): LiveData<DataState<List<UserSearched>?>> {
        return Transformations.map(service.searchUserByWordCloud(text, token)) { input->
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState<List<UserSearched>?>(input.data!!)
                    TOKEN_INVALID -> return@map DataState<List<UserSearched>?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<List<UserSearched>?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    /**
     * 获取用户资料
     * @param id 用户id
     * @param token 用户令牌
     * @return 资料
     */
    fun getUserProfile(id: String?, token: String): LiveData<DataState<UserProfile?>> {
        return Transformations.map<ApiResponse<UserProfile?>, DataState<UserProfile?>>(service.getUserProfile(id, token)) { input: ApiResponse<UserProfile?>? ->
            Log.e("profile", input.toString())
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState(input.data)
                    TOKEN_INVALID -> return@map DataState<UserProfile?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<UserProfile?>(DataState.STATE.FETCH_FAILED)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    /**
     * 换昵称
     * @param token 令牌
     * @param nickname 昵称
     * @return 操作结果
     */
    fun changeNickname(token: String, nickname: String): LiveData<DataState<String?>> {
        return Transformations.map<ApiResponse<Any?>, DataState<String?>>(service.changeNickname(nickname, token)) { input: ApiResponse<Any?>? ->
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState<String?>(DataState.STATE.SUCCESS)
                    TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    /**
     * 更换性别
     * @param token 令牌
     * @param gender 性别 MALE/FEMALE
     * @return 操作结果
     */
    fun changeGender(token: String, gender: String): LiveData<DataState<String?>> {
        return Transformations.map<ApiResponse<Any?>, DataState<String?>>(service.changeGender(gender, token)) { input: ApiResponse<Any?>? ->
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState<String?>(DataState.STATE.SUCCESS)
                    TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    /**
     * 更换颜色
     * @param token 令牌
     * @param accessibility 用户类型
     * @return 操作结果
     */
    fun changeAccessibility(token: String, accessibility: String): LiveData<DataState<String?>> {
        return Transformations.map<ApiResponse<Any?>, DataState<String?>>(service.changeAccessibility(accessibility, token)) { input: ApiResponse<Any?>? ->
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState<String?>(DataState.STATE.SUCCESS)
                    TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    /**
     * 更换无障碍状态
     * @param token 令牌
     * @param type 用户类型
     * @param subType 无障碍二级分类
     * @param typePermission 无障碍隐私类型
     * @return 操作结果
     */
    fun changeType(token: String, type: Int, subType: String?, typePermission: String?): LiveData<DataState<String?>> {
        return Transformations.map<ApiResponse<Any?>, DataState<String?>>(service.changeType(type, subType, typePermission, token)) { input: ApiResponse<Any?>? ->
            if (input != null) {
                when (input.code) {
                    SUCCESS -> return@map DataState<String?>(DataState.STATE.SUCCESS)
                    TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    /**
     * 更换签名
     * @param token 令牌
     * @param signature 签名
     * @return 操作结果
     */
    fun changeSignature(token: String, signature: String): LiveData<DataState<String?>> {
        return Transformations.map<ApiResponse<Any?>, DataState<String?>>(service.changeSignature(signature, token)) { input: ApiResponse<Any?>? ->
            Log.e("changeSignature: ", input.toString())
            if (input != null) {
                when (input.code) {
                    SUCCESS -> {
                        println("SUCCEED")
                        return@map DataState<String?>(DataState.STATE.SUCCESS)
                    }
                    TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }
    /**
     * 设置词云可见性
     * @param token 令牌
     * @return 操作结果
     */
    fun setWordCloudAccessibility(token: String, private:Boolean): LiveData<DataState<String?>> {
        return Transformations.map<ApiResponse<Any?>, DataState<String?>>(service.setWordCloudAccessibility(private, token)) { input: ApiResponse<Any?>? ->
            Log.e("changeWCAccessibility: ", input.toString())
            if (input != null) {
                when (input.code) {
                    SUCCESS -> {
                        println("SUCCEED")
                        return@map DataState<String?>(DataState.STATE.SUCCESS)
                    }
                    TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }
    /**
     * 更换头像
     * @param token 令牌
     * @param file 图片请求包
     * @return 返回
     */
    fun changeAvatar(token: String, file: MultipartBody.Part): LiveData<DataState<String?>> {
        return Transformations.map(service.uploadAvatar(file, token)) { input: ApiResponse<JsonObject?>? ->
            if (input == null) {
                return@map DataState<String?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                SUCCESS -> {
                    val file1 = input.data?.let { JsonUtils.getStringData(it, "file") }
                    if (file1 != null) {
                        return@map DataState<String?>(file1)
                    } else {
                        return@map DataState<String?>(DataState.STATE.FETCH_FAILED)
                    }
                }
                TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 获取用户词云
     * @param token 用户令牌
     * @return 词频表
     */
    fun getUserWordCloud(token: String?, userId: String): LiveData<DataState<HashMap<String, Float?>?>> {
        return Transformations.map(service.getWordCloud(token, userId)) { input  ->
            if (input != null) {
                when (input.code) {
                    WORD_CLOUD_PRIVATE->return@map DataState<HashMap<String, Float?>?>(DataState.STATE.SPECIAL)
                    SUCCESS -> return@map DataState<HashMap<String, Float?>?>(input.data!!)
                    TOKEN_INVALID -> return@map DataState<HashMap<String, Float?>?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<HashMap<String, Float?>?>(DataState.STATE.FETCH_FAILED)
                }
            }
            DataState(DataState.STATE.FETCH_FAILED)
        }
    }

    companion object {
        //单例模式
        @Volatile
        var instance: UserWebSource? = null
            get() {
                if (field == null) {
                    field = UserWebSource()
                }
                return field
            }
            private set
    }
}