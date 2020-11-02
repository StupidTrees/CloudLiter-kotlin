package com.stupidtree.cloudliter.ui.relationevent

import com.stupidtree.cloudliter.data.model.RelationEvent.ACTION
import com.stupidtree.cloudliter.ui.base.Trigger

internal class ResponseFriendTrigger : Trigger() {
    lateinit var eventId: String
    lateinit var action: ACTION

    companion object {
        fun getActioning(eventId: String, action: ACTION): ResponseFriendTrigger {
            val rft = ResponseFriendTrigger()
            rft.action = action
            rft.eventId = eventId
            rft.setActioning()
            return rft
        }
    }
}