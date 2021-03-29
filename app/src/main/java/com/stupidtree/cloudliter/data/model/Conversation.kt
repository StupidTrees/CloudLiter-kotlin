package com.stupidtree.cloudliter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.sql.Timestamp

/**
 * 对话实体类
 */
@Entity(tableName = "conversation")
class Conversation : Serializable {
    enum class TYPE { GROUP, FRIEND }

    @PrimaryKey
    lateinit var id: String
    var lastMessage: String? = null
    var groupId: String? = null
    var type: TYPE = TYPE.FRIEND
    var friendId: String? = null
    var name: String? = null
    var avatar: String? = null
    var createdAt: Timestamp? = null
    var updatedAt: Timestamp? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conversation

        if (id != other.id) return false
        if (lastMessage != other.lastMessage) return false
        if (name != other.name) return false
        if (avatar != other.avatar) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (lastMessage?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (avatar?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }

}