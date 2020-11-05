package com.stupidtree.cloudliter.data.model

import com.google.gson.JsonElement
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import com.stupidtree.cloudliter.utils.JsonUtils
import java.util.*

/**
 * 显示在联系人列表的数据Model
 * 暂未和服务器返回数据格式匹配，需要适配函数
 */
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

    //联系人的用户id
    var friendId: String? = null

    //联系人用户的备注
    var remark: String? = null

    /**
     * 仅在本地使用的属性
     */
    var isLabel = false //是否显示为分组标签

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UserRelation
        return friendNickname == that.friendNickname &&
                friendId == that.friendId
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

    companion object {
        fun getLabelInstance(groupId: String?, groupName: String?): UserRelation {
            val res = UserRelation()
            res.isLabel = true
            res.friendNickname = groupName
            res.groupId = groupId
            return res
        }

        /**
         * 从网络请求返回的JsonElement中解析出一个FriendContact对象
         * @param je JsonElement对象
         * @return FriendContact对象
         */
        fun getInstanceFromJsonObject(je: JsonElement): UserRelation? {
            return try {
                val jo = je.asJsonObject
                val userInfo = jo["user"].asJsonObject
                val res = UserRelation()
                //res.group = jo.get("group").getAsInt();
                res.friendNickname = userInfo["friendNickname"].asString
                res.friendId = userInfo["friendId"].asString
                res.friendAvatar = JsonUtils.getStringData(userInfo, "friendAvatar")
                res.groupId = JsonUtils.getStringData(jo, "groupId")
                res.groupName = JsonUtils.getStringData(jo, "groupName")
                res.remark = JsonUtils.getStringData(jo, "remark")
                val gender = userInfo["gender"].asString
                res.friendGender = if (gender == "MALE") GENDER.MALE else GENDER.FEMALE
                res
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}