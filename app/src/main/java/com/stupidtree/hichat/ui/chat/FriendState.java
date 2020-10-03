package com.stupidtree.hichat.ui.chat;

/**
 * 好友状态
 */
public class FriendState {
    enum STATE{ONLINE,OFFLINE}
    STATE state;

    public static FriendState getOnline(){
        FriendState fs = new FriendState();
        fs.setState(STATE.ONLINE);
        return fs;
    }
    public static FriendState getOffline(){
        FriendState fs = new FriendState();
        fs.setState(STATE.OFFLINE);
        return fs;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }
}
