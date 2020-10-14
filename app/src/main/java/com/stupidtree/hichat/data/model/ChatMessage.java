package com.stupidtree.hichat.data.model;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class ChatMessage implements Serializable {
    /**
     * 和服务器数据实体一致的属性
     */
    String id;
    String fromId;
    String toId;
    String content;
    String friendRemark;
    String friendAvatar;
    String conversationId;
    String relationId;
    boolean read;
    boolean sensitive;
    Timestamp createdAt;
    Timestamp updatedAt;

    /**
     * 服务器上不保存的属性
     */
    //是否正在发送
    boolean progressing;
    String uuid;

    public ChatMessage(String fromId, String toId, String content) {
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        createdAt = new Timestamp(System.currentTimeMillis());
        uuid = UUID.randomUUID().toString();
        progressing = true;
    }

    public static ChatMessage getTimeStampHolderInstance(Timestamp timestamp){
        ChatMessage cm = new ChatMessage(null,null,null);
        cm.id = "TIME";
        cm.createdAt = timestamp;
        return cm;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getCreatedTime(){
        return createdAt;
    }
    @NotNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getFriendRemark() {
        return friendRemark;
    }

    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getRelationId() {
        return relationId;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setProgressing(boolean progressing) {
        this.progressing = progressing;
    }

    public boolean isProgressing() {
        return progressing;
    }


    public boolean isRead() {
        return read;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage message = (ChatMessage) o;
        return Objects.equals(id, message.id) &&
                Objects.equals(fromId, message.fromId) &&
                Objects.equals(toId, message.toId) &&
                Objects.equals(conversationId, message.conversationId) &&
                Objects.equals(relationId, message.relationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromId, toId, conversationId, relationId);
    }
}
