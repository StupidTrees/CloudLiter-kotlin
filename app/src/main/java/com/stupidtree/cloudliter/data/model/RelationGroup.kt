package com.stupidtree.cloudliter.data.model

import java.sql.Timestamp
import java.util.*

/**
 * 数据实体：好友分组
 */
class RelationGroup(var userId: String, var groupName: String) {
    /**
     * 和服务器返回的结构一致，无需转换
     */
    var id: String? = null
    var createdAt: Timestamp? = null
    var updatedAt: Timestamp? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as RelationGroup
        return id == that.id &&
                userId == that.userId &&
                groupName == that.groupName
    }

    override fun toString(): String {
        return "RelationGroup{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}'
    }

    override fun hashCode(): Int {
        return Objects.hash(id, userId, groupName, createdAt, updatedAt)
    }

}