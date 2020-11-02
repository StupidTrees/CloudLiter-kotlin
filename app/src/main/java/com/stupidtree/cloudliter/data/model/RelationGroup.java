package com.stupidtree.cloudliter.data.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * 数据实体：好友分组
 */
public class RelationGroup {
    /**
     * 和服务器返回的结构一致，无需转换
     */
    String id;
    String userId;
    String groupName;
    Timestamp createdAt;
    Timestamp updatedAt;

    public RelationGroup(String userId, String groupName){
        this.groupName = groupName;
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationGroup that = (RelationGroup) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(groupName, that.groupName);
    }

    @Override
    public String toString() {
        return "RelationGroup{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, groupName, createdAt, updatedAt);
    }
}
