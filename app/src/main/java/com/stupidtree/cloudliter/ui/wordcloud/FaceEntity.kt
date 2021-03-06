package com.stupidtree.cloudliter.ui.wordcloud

class FaceEntity:Comparable<FaceEntity>{
    var id: String? = ""
    var userId: String? = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceEntity

        if (id != other.id) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (userId?.hashCode() ?: 0)
        return result
    }

    override fun compareTo(other: FaceEntity): Int {
        return id?.compareTo(other.id?:"")?:0
    }


}