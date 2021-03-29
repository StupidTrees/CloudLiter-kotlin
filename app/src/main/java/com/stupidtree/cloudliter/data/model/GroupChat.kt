package com.stupidtree.cloudliter.data.model

class GroupChat {

    var id: String = ""
    var name: String? = ""
    var master: String? = ""
    var avatar: String? = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupChat

        if (id != other.id) return false
        if (name != other.name) return false
        if (master != other.master) return false
        if (avatar != other.avatar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (master?.hashCode() ?: 0)
        result = 31 * result + (avatar?.hashCode() ?: 0)
        return result
    }


}