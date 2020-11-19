package com.stupidtree.cloudliter.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.JsonElement
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import com.stupidtree.cloudliter.utils.JsonUtils
import java.util.*

/**
 * 显示在联系人列表的数据Model
 * 暂未和服务器返回数据格式匹配，需要适配函数
 */
@Entity(tableName = "relation")
class UserRelation {
    //姓名
    var friendNickname: String? = null

    //头像链接
    var friendAvatar: String? = null

    //分组id
    var groupId: String? = null

    //分组名称
    var groupName: String? = null

    //性别
    var friendGender: GENDER? = null

    @PrimaryKey
    //联系人的用户id
    var friendId: String = ""

    //联系人用户的备注
    var remark: String? = null


    /**
     * 仅在本地使用的属性
     */
    @Ignore
    var isLabel = false //是否显示为分组标签

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UserRelation
        return friendNickname == that.friendNickname &&
                friendId == that.friendId&&remark==that.remark
    }

    override fun hashCode(): Int {
        return Objects.hash(friendNickname, friendId)
    }

    override fun toString(): String {
        return "UserRelation{" +
                "name='" + friendNickname + '\'' +
                ", avatar='" + friendAvatar + '\'' +
                ", group='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", gender=" + friendGender +
                ", id='" + friendId + '\'' +
                ", remark='" + remark + '\'' +
                ", label=" + isLabel +
                '}'
    }

}