package com.stupidtree.cloudliter.ui.relation

import com.stupidtree.cloudliter.ui.base.Trigger

class RelationQueryTrigger : Trigger() {
    lateinit var friendId: String

    companion object {
        fun getActioning(friendId: String): RelationQueryTrigger {
            val rqt = RelationQueryTrigger()
            rqt.friendId = friendId
            rqt.setActioning()
            return rqt
        }
    }
}