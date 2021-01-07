package com.stupidtree.cloudliter.data.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER

/**
 * 显示在用户资料页的用户资料Model
 * 和服务器返回数据匹配，无需适配函数
 */
@Entity(tableName = "profile")
class UserProfile {

    @PrimaryKey
    var id //用户id
            : String = ""
    var username //用户名
            : String? = null
    var nickname //昵称
            : String? = null
    var gender //性别
            : GENDER? = null
    var signature //签名
            : String? = null
    var avatar //头像
            : String? = null
    var accessibility //颜色
            : UserLocal.ACCESSIBILITY = UserLocal.ACCESSIBILITY.NO

    var wordCloudPrivate:Boolean = false//词云私密性


    fun getAccessibilityName():Int{
        return when (accessibility) {
            UserLocal.ACCESSIBILITY.NO -> {
                R.string.accessibility_off
            }
            UserLocal.ACCESSIBILITY.YES_PUBLIC -> {
                R.string.accessibility_on_public
            }
            else -> {
                R.string.accessibility_on_private
            }
        }
    }
}