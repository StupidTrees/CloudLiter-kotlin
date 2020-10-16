package com.stupidtree.hichat.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;


@Entity(tableName = "message")
public class ChatMessage implements Serializable {
    /**
     * 和服务器数据实体一致的属性
     */
    @PrimaryKey
    @NotNull
    public Long id;
    public String fromId;
    public String toId;
    public String content;
    @Ignore
    public String friendRemark;
    @Ignore
    public String friendAvatar;
    public String conversationId;
    public String relationId;
    public boolean read;
    public boolean sensitive;
    public float emotion;
    public Timestamp createdAt;
    public Timestamp updatedAt;
    //long createdTime;

    /**
     * 服务器上不保存的属性
     */
    //是否正在发送
    @Ignore
    boolean progressing;
    @Ignore
    String uuid;

    public ChatMessage(){
        progressing = false;
    }

    @Ignore
    public ChatMessage(String fromId, String toId, String content) {
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        createdAt = new Timestamp(System.currentTimeMillis());
        uuid = UUID.randomUUID().toString();
        progressing = true;
    }

    public static ChatMessage getTimeStampHolderInstance(Timestamp timestamp) {
        ChatMessage cm = new ChatMessage(null, null, null);
        cm.id = (long) -1;
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

    public Timestamp getCreatedTime() {
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

    @NotNull
    public Long getId() {
        return id;
    }

    public boolean isTimeStamp(){
        if(id==null) return false;
        return id==-1;
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

    public float getEmotion() {
        return emotion;
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
                Objects.equals(relationId, message.relationId)&&
                Objects.equals(sensitive,message.sensitive)&&
                Objects.equals(emotion,message.emotion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromId, toId, conversationId, relationId);
    }

}
