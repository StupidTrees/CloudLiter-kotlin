package com.stupidtree.cloudliter.ui.chat

/**
 * 好友状态
 */
class OnlineState {
    enum class STATE {
        GROUP, ONLINE, OFFLINE, YOU, OTHER
    }

    var state: STATE? = null
    var num: Int = 0

    companion object {
        val online: OnlineState
            get() {
                val fs = OnlineState()
                fs.state = STATE.ONLINE
                return fs
            }

        val offline: OnlineState
            get() {
                val fs = OnlineState()
                fs.state = STATE.OFFLINE
                return fs
            }

        val withYou: OnlineState
            get() {
                val fs = OnlineState()
                fs.state = STATE.YOU
                return fs
            }

        val withOther: OnlineState
            get() {
                val fs = OnlineState()
                fs.state = STATE.OTHER
                return fs
            }
    }
}