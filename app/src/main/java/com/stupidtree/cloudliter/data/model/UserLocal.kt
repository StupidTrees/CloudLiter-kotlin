package com.stupidtree.cloudliter.data.model

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.utils.JsonUtils
import java.io.Serializable

/**
 * 缓存在本地的此用户Model
 * 暂未和服务器返回数据格式匹配，需要适配函数
 */
class UserLocal : Serializable {
    //定义性别的枚举类型
    enum class GENDER {
        MALE, FEMALE
    }

    enum class TYPEPERMISSION {
        PRIVATE, PUBLIC, PROTECTED
    }

    var username //用户名
            : String? = null
    var id //用户id
            : String? = null
    var nickname //用户昵称
            : String? = null
    var signature //用户签名
            : String? = null
    var token //保存用户登陆状态的token（重要）
            : String? = null
        get
    var gender //用户性别
            : GENDER? = null
    var avatar //用户头像链接
            : String? = null

    var type // 用户类型
            : Int? = null
    var subType // 无障碍二级分类
            : String? = null
    var typePermission
            : TYPEPERMISSION = TYPEPERMISSION.PRIVATE


    val isValid: Boolean
        get() {
            Log.e("valid", (token != null && id != null).toString())
            return token != null && id != null
        }

    fun setGender(gender: String?) {
        this.gender = if (gender == "MALE") GENDER.MALE else GENDER.FEMALE
    }
    fun setTypePermission(typePermission: String?) {
        typePermission?.let { this.typePermission = TYPEPERMISSION.valueOf(it) }
    }
    override fun toString(): String {
        return Gson().toJson(this)
    }


    companion object {
        /**
         * 从服务器返回的JsonObject中解析出一个UserLocal对象
         * @param responseData 来自服务器
         * @return 返回
         */
        @JvmStatic
        fun getFromResponseData(responseData: JsonObject?): UserLocal {
            val userLocal = UserLocal()
            val info = responseData?.let { JsonUtils.getObjectData(it, "info") }
            val token = responseData?.let { JsonUtils.getStringData(it, "token") }
            if (info != null) {
                val id = JsonUtils.getStringData(info, "id")
                val username = JsonUtils.getStringData(info, "username")
                val nickname = JsonUtils.getStringData(info, "nickname")
                val signature = JsonUtils.getStringData(info, "signature")
                val gender = JsonUtils.getStringData(info, "gender")
                val avatar = JsonUtils.getStringData(info, "avatar")
                val type = JsonUtils.getIntegerData(info, "type")
                val subType = JsonUtils.getStringData(info, "subType")
                val typePermission = JsonUtils.getStringData(info, "typePermission")
                userLocal.username = username
                userLocal.nickname = nickname
                userLocal.signature = signature
                userLocal.setGender(gender)
                userLocal.avatar = avatar
                userLocal.token = token
                userLocal.id = id
                userLocal.type = type
                userLocal.subType = subType
                typePermission?.let { userLocal.typePermission = TYPEPERMISSION.valueOf(it) }
                return userLocal
            }
            return userLocal
        }
    }
}