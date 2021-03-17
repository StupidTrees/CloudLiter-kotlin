package com.stupidtree.cloudliter.data.source.websource

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import java.util.*

/**
 * 层次：DataSource
 * 本地用户的数据源
 * 类型：SharedPreference
 * 数据：同步读取，异步写入
 */
class UserPreferenceSource(private val context: Context) {
    private var sharedPreferences: SharedPreferences? = null
    private val preference: SharedPreferences?
        get() {
            if (sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences(SP_NAME_LOCAL_USER, Context.MODE_PRIVATE)
            }
            return sharedPreferences
        }

    fun saveLocalUser(user: UserLocal) {
        val i = Intent(SocketIOClientService.ACTION_ONLINE)
        i.putExtra("userId", user.id)
        context.sendBroadcast(i)
        Log.e("save_local_user", user.toString())
        preference!!.edit()
                .putString("id", user.id)
                .putString("username", user.username)
                .putString("nickname", user.nickname)
                .putString("gender", user.gender.toString())
                .putString("signature", user.signature) //获取签名
                .putString("token", user.token)
                .putString("avatar", user.avatar)
                .apply()
    }

    fun saveAvatar(newAvatar: String?) {
        preference!!.edit()
                .putString("avatar", newAvatar)
                .apply()
        changeMyAvatarGlideSignature()
    }

    fun saveNickname(nickname: String?) {
        preference!!.edit()
                .putString("nickname", nickname)
                .apply()
    }

    fun saveGender(gender: String?) {
        preference!!.edit()
                .putString("gender", gender)
                .apply()
    }

    fun saveSignature(signature: String?) {
        preference!!.edit()
                .putString("signature", signature)
                .apply()
    }
    fun saveAccessibility(accessibility: String?) {
        preference!!.edit()
                .putString("accessibility",accessibility)
                .apply()
    }
    fun saveType(type: Int?) {
        preference!!.edit()
                .putInt("type", type!!)
                .apply()
    }
    fun saveSubType(subType: String?) {
        preference!!.edit()
                .putString("subType", subType)
                .apply()
    }
    fun saveTypePermission(typePermission: String?) {
        preference!!.edit()
                .putString("typePermission", typePermission)
                .apply()
    }
    fun clearLocalUser() {
        val preferences = context.getSharedPreferences(SP_NAME_LOCAL_USER, Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
        //        preferences.edit().putString("username",null)
//                .putString("nickname",null).putString("token",null).apply();
    }

    // Log.e("get_local_user", String.valueOf(result));
    val localUser: UserLocal
        get() {
            val preferences = preference
            val result = UserLocal()
            result.id = preferences!!.getString("id", null)
            result.username = preferences.getString("username", null)
            result.nickname = preferences.getString("nickname", null)
            result.signature = preferences.getString("signature", null)
            result.token = preferences.getString("token", null)
            result.setGender(preferences.getString("gender", "MALE"))
            result.avatar = preferences.getString("avatar", null)
            // Log.e("get_local_user", String.valueOf(result));
            return result
        }

    private fun changeMyAvatarGlideSignature() {
        preference!!.edit().putString("my_avatar", UUID.randomUUID().toString()).apply()
    }

    val myAvatarGlideSignature: String
        get() {
            var signature = preference!!.getString("my_avatar", null)
            if (signature == null) {
                signature = UUID.randomUUID().toString()
                preference!!.edit().putString("my_avatar", signature).apply()
            }
            return signature
        }

    companion object {
        private const val SP_NAME_LOCAL_USER = "local_user_profile"

        @SuppressLint("StaticFieldLeak")
        private var instance: UserPreferenceSource? = null
        @JvmStatic
        fun getInstance(context: Context): UserPreferenceSource? {
            if (instance == null) {
                instance = UserPreferenceSource(context.applicationContext)
            }
            return instance
        }
    }

}