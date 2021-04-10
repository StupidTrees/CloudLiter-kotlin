package com.stupidtree.cloudliter.ui.imagedetect

import android.graphics.RectF
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier

class DetectResult(res: Classifier.Recognition) {
    var name: String? = ""
    var id: String = ""
    var confidence = 0f
    var friendId: String? = null
    var isFriend = false
    var friendName: String = ""
    var expression:String? = null
    var rect: RectF = RectF(0f, 0f, 0f, 0f)

    init {
        name = res.title
        id = res.id
        confidence = res.confidence
        rect = res.location
    }

    fun setFriendInfo(id:String?,name:String?){
        if (name != null) {
            isFriend = true
            friendId = id
            friendName = name
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectResult

        if (name != other.name) return false
        if (id != other.id) return false
        if (confidence != other.confidence) return false
        if (friendId != other.friendId) return false
        if (isFriend != other.isFriend) return false
        if (friendName != other.friendName) return false
        if (expression != other.expression) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + id.hashCode()
        result = 31 * result + confidence.hashCode()
        result = 31 * result + (friendId?.hashCode() ?: 0)
        result = 31 * result + isFriend.hashCode()
        result = 31 * result + friendName.hashCode()
        result = 31 * result + (expression?.hashCode() ?: 0)
        return result
    }


}