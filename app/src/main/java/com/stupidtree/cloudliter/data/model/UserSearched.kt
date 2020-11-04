package com.stupidtree.cloudliter.data.model

import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import java.util.*

/**
 * 显示在搜索页面的搜索结果Model
 * 和服务器返回数据匹配，无需适配函数
 */
class UserSearched {
    var username //用户名
            : String? = null
    var nickname //昵称
            : String? = null
    var avatar //头像
            : String? = null
    var gender //性别
            : GENDER? = null
    var id //id
            : String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as UserSearched
        return username == that.username &&
                id == that.id
    }

    override fun hashCode(): Int {
        return Objects.hash(username, id)
    }

    override fun toString(): String {
        return "UserSearched{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", gender=" + gender +
                ", id=" + id +
                '}'
    }
}