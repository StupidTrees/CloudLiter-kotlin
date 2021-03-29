package com.stupidtree.cloudliter.ui.conversation.group

class GroupMemberEntity {

    var userId: String = ""
    var userAvatar: String? = ""
    var groupNickname: String? = ""
    var userNickname: String? = ""


    fun getName(): String? {
        return if (groupNickname.isNullOrEmpty()) {
            userNickname
        } else {
            groupNickname
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupMemberEntity

        if (userId != other.userId) return false
        if (userAvatar != other.userAvatar) return false
        if (groupNickname != other.groupNickname) return false
        if (userNickname != other.userNickname) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + (userAvatar?.hashCode() ?: 0)
        result = 31 * result + (groupNickname?.hashCode() ?: 0)
        result = 31 * result + (userNickname?.hashCode() ?: 0)
        return result
    }

}