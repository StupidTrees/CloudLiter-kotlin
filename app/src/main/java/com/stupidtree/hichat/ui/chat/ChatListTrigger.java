package com.stupidtree.hichat.ui.chat;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.ui.base.Trigger;

public class ChatListTrigger extends Trigger {
    enum MODE{ADD_MESSAGE,FETCH_WHOLE_PAGE}
    MODE mode;
    String conversationId;
    ChatMessage newMessage;

    public static ChatListTrigger getActioning(String conversationId,ChatMessage message){
        ChatListTrigger chatListTrigger = new ChatListTrigger();
        chatListTrigger.mode = MODE.ADD_MESSAGE;
        chatListTrigger.setActioning();
        chatListTrigger.conversationId = conversationId;
        chatListTrigger.newMessage = message;
        return chatListTrigger;
    }

    public static ChatListTrigger getStill(){
        ChatListTrigger chatListTrigger = new ChatListTrigger();
        chatListTrigger.cancelActioning();
        return chatListTrigger;
    }

    public String getConversationId() {
        return conversationId;
    }

    public static ChatListTrigger getActioning(String conversationId){
        ChatListTrigger chatListTrigger = new ChatListTrigger();
        chatListTrigger.mode = MODE.FETCH_WHOLE_PAGE;
        chatListTrigger.conversationId = conversationId;
        chatListTrigger.setActioning();
        return chatListTrigger;
    }

    public MODE getMode() {
        return mode;
    }

    public ChatMessage getNewMessage() {
        return newMessage;
    }
}
