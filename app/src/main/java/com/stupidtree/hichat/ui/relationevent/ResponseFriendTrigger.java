package com.stupidtree.hichat.ui.relationevent;

import androidx.annotation.NonNull;

import com.stupidtree.hichat.data.model.RelationEvent;
import com.stupidtree.hichat.ui.base.Trigger;

class ResponseFriendTrigger extends Trigger {
    String eventId;
    RelationEvent.ACTION action;


    public RelationEvent.ACTION getAction() {
        return action;
    }

    public String getEventId() {
        return eventId;
    }

    public static ResponseFriendTrigger getActioning(@NonNull String eventId, RelationEvent.ACTION action){
        ResponseFriendTrigger rft = new ResponseFriendTrigger();
        rft.action = action;
        rft.eventId = eventId;
        rft.setActioning();
        return rft;
    }
}
