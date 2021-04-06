package com.stupidtree.accessibility.ai.segmentation

class Token {

    var name:String? = ""
    var tag:String? = ""
    var index:Int = 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Token

        if (name != other.name) return false
        if (tag != other.tag) return false
        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + index
        return result
    }


}