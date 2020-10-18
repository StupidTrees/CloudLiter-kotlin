package com.stupidtree.hichat.data.model;

import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.stupidtree.hichat.utils.TextUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 对话实体类
 */
@Entity(tableName = "conversation")
public class Conversation implements Serializable {
    @NonNull
    @PrimaryKey
    String id;
    String historyId;
    String lastMessage;
    @NonNull
    String friendId;
    String groupId;
    String friendNickname;
    String friendAvatar;
    String friendRemark;
    String relationId;
    Timestamp createdAt;
    Timestamp updatedAt;

    public static Conversation fromNewMessage(@NonNull ChatMessage message) {
        Conversation conversation = new Conversation();
        conversation.setFriendAvatar(message.getFriendAvatar());
        conversation.setFriendId(message.getFromId());
        conversation.setFriendRemark(message.getFriendRemark());
        conversation.setId(message.getConversationId());
        conversation.setRelationId(message.getRelationId());
        return conversation;
    }
    public static Conversation fromUserRelationAndProfile(@NonNull UserProfile friendProfile,@NonNull UserRelation userRelation, @NonNull UserLocal userLocal) {
        Conversation conversation = new Conversation();
        conversation.setFriendAvatar(friendProfile.getAvatar());
        conversation.setFriendId(friendProfile.getId());
        conversation.setFriendRemark(userRelation.getRemark());
        conversation.setFriendNickname(friendProfile.getNickname());
        conversation.setId(TextUtils.getP2PIdOrdered(friendProfile.getId(),userLocal.getId()));
        conversation.setRelationId(userRelation.getId());
        Log.e("conver_FPF", String.valueOf(friendProfile));
        Log.e("conver_UR", String.valueOf(userRelation));
        return conversation;
    }


    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setFriendRemark(String friendRemark) {
        this.friendRemark = friendRemark;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @NonNull
    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(@NonNull String friendId) {
        this.friendId = friendId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getFriendRemark() {
        return friendRemark;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public String getFriendNickname() {
        return friendNickname;
    }

    public void setFriendNickname(String friendNickname) {
        this.friendNickname = friendNickname;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setFriendAvatar(String friendAvatar) {
        this.friendAvatar = friendAvatar;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return id.equals(that.id) &&
                Objects.equals(historyId, that.historyId) &&
                Objects.equals(lastMessage, that.lastMessage) &&
                friendId.equals(that.friendId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(friendRemark, that.friendRemark) &&
                Objects.equals(friendAvatar, that.friendAvatar) &&
                Objects.equals(relationId, that.relationId) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, historyId, lastMessage, friendId, groupId, friendNickname, friendAvatar, relationId, createdAt, updatedAt);
    }

    @Override
    public String toString() {
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
                '}';
    }
}
