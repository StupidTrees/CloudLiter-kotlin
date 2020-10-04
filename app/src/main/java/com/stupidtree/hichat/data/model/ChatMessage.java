package com.stupidtree.hichat.data.model;

import com.google.gson.Gson;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class ChatMessage implements Serializable {
    String id;
    String fromId;
    String toId;
    String content;
    String conversationId;
    String relationId;
    Timestamp createdAt;
    Timestamp updatedAt;


    public ChatMessage(String fromId, String toId, String content) {
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        createdAt = new Timestamp(System.currentTimeMillis());
    }

    public void setMine(boolean mine){

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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getId() {
        return id;
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
