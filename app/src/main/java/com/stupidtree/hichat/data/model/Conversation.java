package com.stupidtree.hichat.data.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 对话实体类
 */
public class Conversation implements Serializable {
    @NonNull
    String id;
    String historyId;
    String lastMessage;
    @NonNull
    String friendId;
    String groupId;
    String friendNickname;
    String friendAvatar;
    String relationId;
    Timestamp createdAt;
    Timestamp updatedAt;

    public static Conversation getFromProfileAndMe(@NonNull UserProfile friend,UserLocal userLocal){
        Conversation conversation = new Conversation();
        if(userLocal.getId()!=null&&friend.getId()!=null){
            conversation.setFriendAvatar(friend.getAvatar());
            conversation.setFriendId(friend.getId());
            conversation.setFriendNickname(friend.getNickname());
            int fi = Integer.parseInt(friend.getId());
            int mi = Integer.parseInt(userLocal.getId());
            conversation.setId(Math.min(fi,mi)+"-"+Math.max(fi,mi));

        }
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
                Objects.equals(friendNickname, that.friendNickname) &&
                Objects.equals(friendAvatar, that.friendAvatar) &&
                Objects.equals(relationId, that.relationId) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, historyId, lastMessage, friendId, groupId, friendNickname, friendAvatar, relationId, createdAt, updatedAt);
    }
}
