package com.stupidtree.cloudliter.ui.chat

/**
 * 好友状态
 */
class FriendState {
    enum class STATE {
        ONLINE, OFFLINE, YOU, OTHER
    }

   var state: STATE? = null


    companion object {
        val online: FriendState
            get() {
                val fs = FriendState()
                fs.state = STATE.ONLINE
                return fs
            }

        val offline: FriendState
            get() {
                val fs = FriendState()
                fs.state = STATE.OFFLINE
                return fs
            }

        val withYou: FriendState
            get() {
                val fs = FriendState()
                fs.state = STATE.YOU
                return fs
            }

        val withOther: FriendState
            get() {
                val fs = FriendState()
                fs.state = STATE.OTHER
                return fs
            }
    }
}