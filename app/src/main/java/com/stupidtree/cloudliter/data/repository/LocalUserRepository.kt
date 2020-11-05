package com.stupidtree.cloudliter.data.repository

import android.app.Application
import android.content.Context
import android.content.Intent
import com.stupidtree.cloudliter.HiApplication
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.AppDatabase.Companion.getDatabase
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.source.UserPreferenceSource
import com.stupidtree.cloudliter.service.socket.SocketIOClientService

/**
 * 层次：Repository
 * ”我的“页面的Repository，同时也是全局的本地用户仓库
 */
class LocalUserRepository(application: Application) {

    //数据源：SharedPreference性质的本地状态数据源
    var mePreferenceSource: UserPreferenceSource = UserPreferenceSource(application)

    //将已登录用户缓存在内存里
    private var loggedInUser: UserLocal? = null
    private val appDatabase: AppDatabase = getDatabase(application)

    /**
     * 登出
     */
    fun logout(context: Context) {
        if (loggedInUser != null) {
            val i = Intent(SocketIOClientService.ACTION_OFFLINE)
            i.putExtra("userId", loggedInUser!!.id)
            context.sendBroadcast(i)
        }
        loggedInUser = null
        mePreferenceSource.clearLocalUser()
        //本地缓存清空
        Thread {
            appDatabase.chatMessageDao().clearTable()
            appDatabase.conversationDao().clearTable()
        }.start()
    }

    /**
     * 更改该本地缓存的头像地址
     * @param newAvatar 头像地址
     */
    fun ChangeLocalAvatar(newAvatar: String?) {
        mePreferenceSource.saveAvatar(newAvatar)
        loggedInUser = mePreferenceSource.localUser
        // getThis().getSharedPreferences("Glide", Context.MODE_PRIVATE).edit().
    }

    /**
     * 更改本地缓存的昵称
     * @param nickname 新昵称
     */
    fun ChangeLocalNickname(nickname: String?) {
        mePreferenceSource.saveNickname(nickname)
        loggedInUser = mePreferenceSource.localUser
    }

    /**
     * 更改本地缓存的用户性别
     * @param gender 性别/MALE/FEMALE
     */
    fun ChangeLocalGender(gender: String?) {
        mePreferenceSource.saveGender(gender)
        loggedInUser = mePreferenceSource.localUser
    }

    /**
     * 更改本地缓存的签名
     * @param signature 新签名
     */
    fun ChangeLocalSignature(signature: String?) {
        mePreferenceSource.saveSignature(signature)
    }

    /**
     * 直接获取本地已登陆的用户对象
     * 同步获取
     * @return 本地用户对象
     */
    fun getLoggedInUser(): UserLocal {
        //Log.e("get_local_user", String.valueOf(loggedInUser));
        //if(loggedInUser==null){
        loggedInUser = mePreferenceSource.localUser
        // }
        return loggedInUser!!
    }

    /**
     * 用户是否登录
     * @return 是否登陆
     */
    val isUserLoggedIn: Boolean
        get() = loggedInUser != null && loggedInUser!!.isValid

    companion object {
        //也是单例模式
        @Volatile
        private var instance: LocalUserRepository? = null
        fun getInstance(application: Application):LocalUserRepository {
            if (null == instance) {
                instance = LocalUserRepository(application)
            }
            return instance!!
        }
    }

}