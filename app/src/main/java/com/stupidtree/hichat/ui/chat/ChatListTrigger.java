package com.stupidtree.hichat.ui.chat;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.ui.base.Trigger;

public class ChatListTrigger extends Trigger {
    enum MODE{ADD_MESSAGE,FETCH_WHOLE_PAGE,LOAD_MORE}
    MODE mode;
    String conversationId;
    ChatMessage newMessage;
    int pageNum;
    int pageSize;

    /**
     * 添加单条消息
     */
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

    /**
     * 刷新整个页面
     */
    public static ChatListTrigger getActioning(String conversationId, int pageSize){
        ChatListTrigger chatListTrigger = new ChatListTrigger();
        chatListTrigger.mode = MODE.FETCH_WHOLE_PAGE;
        chatListTrigger.conversationId = conversationId;
        chatListTrigger.pageSize = pageSize;
        chatListTrigger.setActioning();
        return chatListTrigger;
    }

    public static ChatListTrigger getActioning(String conversationId, int pageSize, int pageNum){
        ChatListTrigger chatListTrigger = new ChatListTrigger();
        chatListTrigger.mode = MODE.LOAD_MORE;
        chatListTrigger.conversationId = conversationId;
        chatListTrigger.setActioning();
        chatListTrigger.pageSize = pageSize;
        chatListTrigger.pageNum = pageNum;
        return chatListTrigger;
    }


    public MODE getMode() {
        return mode;
    }

    public ChatMessage getNewMessage() {
        return newMessage;
    }
}
