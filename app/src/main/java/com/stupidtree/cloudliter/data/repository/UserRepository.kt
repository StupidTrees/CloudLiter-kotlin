package com.stupidtree.cloudliter.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.UserSearched
import com.stupidtree.cloudliter.data.source.UserPreferenceSource
import com.stupidtree.cloudliter.data.source.UserWebSource
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.welcome.login.LoginResult
import com.stupidtree.cloudliter.ui.welcome.signup.SignUpResult

/**
 * 层次：Repository层
 * 用户操作的Repository
 */
class UserRepository private constructor(context: Context) {
    //数据源1:网络类型数据，用户网络数据源
    private val userWebSource: UserWebSource = UserWebSource.instance!!

    //数据源2：SharedPreference类型数据，本地用户数据源
    private val userPreferenceSource: UserPreferenceSource? = UserPreferenceSource.getInstance(context.applicationContext)

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    fun login(username: String, password: String): LiveData<LoginResult?> {
        return Transformations.map(userWebSource.login(username, password)) { input: LoginResult ->
            if (input.state === LoginResult.STATES.SUCCESS) {
                userPreferenceSource!!.saveLocalUser(input.userLocal!!)
            }
            input
        }
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param gender 性别
     * @param nickname 昵称
     * @return 注册结果
     */
    fun signUp(username: String?, password: String?,
               gender: String?, nickname: String?): LiveData<SignUpResult?> {
        return Transformations.map(userWebSource.signUp(username, password, gender, nickname)) { input: SignUpResult?->
            if (input != null) {
                if (input.state === SignUpResult.STATES.SUCCESS) {
                    userPreferenceSource!!.saveLocalUser(input.userLocal!!)
                }
            }
            input
        }
    }

    /**
     * 搜索用户
     * @param text 搜索语句
     * @param token 令牌
     * @return 搜索结果列表
     */
    fun searchUser(text: String, token: String): LiveData<DataState<List<UserSearched>?>> {
        return userWebSource.searchUser(text, token)
    }

    companion object {
        //单例模式
        @Volatile
        private var instance: UserRepository? = null

        // public方法：获取单例
        @JvmStatic
        fun getInstance(context: Context): UserRepository? {
            if (instance == null) {
                instance = UserRepository(context)
            }
            return instance
        }
    }

}