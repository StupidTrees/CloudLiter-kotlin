package com.stupidtree.hichat.ui.relation;

import android.widget.RelativeLayout;

import com.stupidtree.hichat.ui.base.Trigger;

public class RelationQueryTrigger extends Trigger {
    String friendId;
    public static RelationQueryTrigger getActioning(String friendId){
        RelationQueryTrigger rqt = new RelationQueryTrigger();
        rqt.friendId = friendId;
        rqt.setActioning();
        return rqt;
    }

    public String getFriendId() {
        return friendId;
    }
}
