package com.stupidtree.cloudliter.ui.chat

import java.io.Serializable
import java.sql.Timestamp

class MessageReadNotification : Serializable {
    enum class TYPE {
        ALL, ONE
    }

    var type: TYPE
    var id: String? = null
    var conversationId: String
    var userId: String
    var fromTime: Timestamp? = null

    constructor(userId: String, conversationId: String, fromTime: Timestamp?) {
        type = TYPE.ALL
        this.userId = userId
        this.conversationId = conversationId
        this.fromTime = fromTime
    }

    constructor(userId: String, conversationId: String, id: String?) {
        type = TYPE.ONE
        this.userId = userId
        this.conversationId = conversationId
        this.id = id
    }

    override fun toString(): String {
        return "MessageReadNotification{" +
                "type=" + type +
                ", id=" + id +
                ", userId='" + userId + '\'' +
                '}'
    }
}