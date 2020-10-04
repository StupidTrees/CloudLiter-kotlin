package com.stupidtree.hichat.ui.chat;

import com.stupidtree.hichat.ui.base.Trigger;

public class ConversationTrigger extends Trigger {
    String friendId;

    public static ConversationTrigger getActioning(String friendId){
        ConversationTrigger ct = new ConversationTrigger();
        ct.setActioning();
        ct.friendId = friendId;
        return ct;
    }
}
