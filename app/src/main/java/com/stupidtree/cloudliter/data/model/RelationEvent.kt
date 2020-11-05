package com.stupidtree.cloudliter.data.model

import java.sql.Timestamp
import java.util.*

class RelationEvent {
    enum class STATE {
        REQUESTING, ACCEPTED, REJECTED, DELETE
    }

    enum class ACTION {
        ACCEPT, REJECT
    }

    var id: String? = null
    var userId: String? = null
    var friendId: String? = null
    var otherId: String? = null
    var otherAvatar: String? = null
    var otherNickname: String? = null
    var state: STATE? = null
    var isUnread = false
    var createdAt: Timestamp? = null
    var updatedAt: Timestamp? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RelationEvent
        return id == that.id &&
                userId == that.userId &&
                friendId == that.friendId &&
                otherAvatar == that.otherAvatar &&
                otherNickname == that.otherNickname &&
                isUnread == that.isUnread && state == that.state
    }

    override fun hashCode(): Int {
        return Objects.hash(id, userId, friendId, otherAvatar, otherNickname, state)
    }
}