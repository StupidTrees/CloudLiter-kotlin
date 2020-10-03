package com.stupidtree.hichat.ui.chat;

import com.stupidtree.hichat.ui.base.Trigger;

public class FriendStateTrigger extends Trigger {
    String id;
    Boolean online;

    public static FriendStateTrigger getActioning(String id,boolean online){
        FriendStateTrigger ft = new FriendStateTrigger();
        ft.online = online;
        ft.id = id;
        ft.setActioning();
        return ft;
    }

    public static FriendStateTrigger getStill(){
        FriendStateTrigger ft = new FriendStateTrigger();
        ft.cancelActioning();
        return ft;
    }
}
