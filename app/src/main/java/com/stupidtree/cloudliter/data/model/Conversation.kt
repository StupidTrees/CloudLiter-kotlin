package com.stupidtree.cloudliter.data.model

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stupidtree.cloudliter.utils.TextUtils
import java.io.Serializable
import java.sql.Timestamp
import java.util.*

/**
 * 对话实体类
 */
@Entity(tableName = "conversation")
class Conversation : Serializable {
    @PrimaryKey
    lateinit var id: String
    var historyId: String? = null
    var lastMessage: String? = null
    var friendId: String? = null
    var groupId: String? = null
    var friendNickname: String? = null
    var friendAvatar: String? = null
    var friendRemark: String? = null
    var relationId: String? = null
    var createdAt: Timestamp? = null
    var updatedAt: Timestamp? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Conversation
        return id == that.id &&
                historyId == that.historyId &&
                lastMessage == that.lastMessage && friendId == that.friendId &&
                groupId == that.groupId &&
                friendRemark == that.friendRemark &&
                friendAvatar == that.friendAvatar &&
                relationId == that.relationId &&
                createdAt == that.createdAt &&
                updatedAt == that.updatedAt
    }

    override fun hashCode(): Int {
        return Objects.hash(id, historyId, lastMessage, friendId, groupId, friendNickname, friendAvatar, relationId, createdAt, updatedAt)
    }

    override fun toString(): String {
        return "Conversation{" +
                "id='" + id + '\'' +
                ", historyId='" + historyId + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", friendId='" + friendId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", friendNickname='" + friendNickname + '\'' +
                ", friendAvatar='" + friendAvatar + '\'' +
                ", friendRemark='" + friendRemark + '\'' +
                ", relationId='" + relationId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}'
    }

    companion object {
        @JvmStatic
        fun fromNewMessage(message: ChatMessage): Conversation {
            val conversation = Conversation()
            conversation.friendAvatar = message.getFriendAvatar()
            conversation.friendId = message.getFromId()
            conversation.friendRemark = message.getFriendRemark()
            conversation.id = message.getConversationId()
            conversation.relationId = message.getRelationId()
            return conversation
        }

        @JvmStatic
        fun fromUserRelationAndProfile(friendProfile: UserProfile, userRelation: UserRelation, userLocal: UserLocal): Conversation {
            val conversation = Conversation()
            conversation.friendAvatar = friendProfile.getAvatar()
            conversation.friendId = friendProfile.getId()
            conversation.friendRemark = userRelation.getRemark()
            conversation.friendNickname = friendProfile.getNickname()
            conversation.id = TextUtils.getP2PIdOrdered(friendProfile.getId(), userLocal.getId())
            conversation.relationId = userRelation.getFriendId()
            Log.e("conver_FPF", friendProfile.toString())
            Log.e("conver_UR", userRelation.toString())
            return conversation
        }
    }
}