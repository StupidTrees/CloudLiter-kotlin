package com.stupidtree.cloudliter.data.model

import androidx.annotation.StringRes
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER

/**
 * 显示在用户资料页的用户资料Model
 * 和服务器返回数据匹配，无需适配函数
 */
class UserProfile {
    enum class COLOR {
        RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, PURPLE
    }

    var id //用户id
            : String? = null
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
    var color //颜色
            : COLOR? = null

    @get:StringRes
    val colorName: Int
        get() = when (color) {
            COLOR.RED -> R.string.red
            COLOR.ORANGE -> R.string.orange
            COLOR.YELLOW -> R.string.yellow
            COLOR.GREEN -> R.string.green
            COLOR.CYAN -> R.string.cyan
            COLOR.PURPLE -> R.string.purple
            else -> R.string.blue
        }

    override fun toString(): String {
        return "UserProfile{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gender=" + gender +
                ", signature='" + signature + '\'' +
                ", avatar='" + avatar + '\'' +
                ", color=" + color +
                '}'
    }
}