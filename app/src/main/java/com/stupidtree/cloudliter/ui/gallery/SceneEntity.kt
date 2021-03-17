package com.stupidtree.cloudliter.ui.gallery

class SceneEntity {

    var key:String = "" //场景的key
    var representId:String = "" //代表图片的id
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SceneEntity

        if (key != other.key) return false
        if (representId != other.representId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + representId.hashCode()
        return result
    }


}