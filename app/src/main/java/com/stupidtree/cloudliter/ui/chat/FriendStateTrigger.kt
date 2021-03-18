package com.stupidtree.cloudliter.ui.chat

import com.stupidtree.component.data.Trigger

class FriendStateTrigger : Trigger() {
    var id: String? = null
    var online: String? = null

    companion object {
        fun getActioning(id: String?, online: String?): FriendStateTrigger {
            val ft = FriendStateTrigger()
            ft.online = online
            ft.id = id
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