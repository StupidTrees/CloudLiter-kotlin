package com.stupidtree.cloudliter.ui.face.permission

class FaceWhiteListEntity{
    var userId:String?=""
    var userName:String?=""
    var userAvatar:String?=""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceWhiteListEntity

        if (userId != other.userId) return false
        if (userName != other.userName) return false
        if (userAvatar != other.userAvatar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId?.hashCode() ?: 0
        result = 31 * result + (userName?.hashCode() ?: 0)
        result = 31 * result + (userAvatar?.hashCode() ?: 0)
        return result
    }


}