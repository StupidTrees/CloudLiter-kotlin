package com.stupidtree.cloudliter.ui.chat

import com.stupidtree.component.data.Trigger

class FriendStateTrigger : Trigger() {
    var conversationId: String? = null
    var online: String? = null
    var num:Int = 0

    companion object {
        fun getActioning(id: String?, online: String?,num:Int): FriendStateTrigger {
            val ft = FriendStateTrigger()
            ft.online = online
            ft.conversationId = id
            ft.num = num
            ft.setActioning()
            return ft
        }

        val still: FriendStateTrigger
            get() {
                val ft = FriendStateTrigger()
                ft.cancelActioning()
                return ft
            }
    }
}