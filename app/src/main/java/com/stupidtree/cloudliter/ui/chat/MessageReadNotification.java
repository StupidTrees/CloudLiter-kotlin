package com.stupidtree.cloudliter.ui.chat;

import java.io.Serializable;
import java.sql.Timestamp;

public class MessageReadNotification implements Serializable {
    public enum TYPE{ALL,ONE};
    TYPE type;
    String id;
    String conversationId;
    String userId;
    Timestamp fromTime;

    public MessageReadNotification(String userId, String conversationId, Timestamp fromTime){
        this.type =  TYPE.ALL;
        this.userId = userId;
        this.conversationId = conversationId;
        this.fromTime = fromTime;
    }
    public MessageReadNotification(String userId, String conversationId,String id){
        this.type =  TYPE.ONE;
        this.userId = userId;
        this.conversationId = conversationId;
        this.id = id;
    }



    public TYPE getType() {
        return type;
    }

    public Timestamp getFromTime() {
        return fromTime;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }


    public String getConversationId() {
        return conversationId;
    }

    @Override
    public String toString() {
        return "MessageReadNotification{" +
                "type=" + type +
                ", id=" + id +
                ", userId='" + userId + '\'' +
                '}';
    }
}
