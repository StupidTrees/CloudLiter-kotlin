package com.stupidtree.cloudliter.ui.chat

import com.stupidtree.component.data.Trigger

class VoiceMessageTrigger : Trigger() {
    var path: String? = null
    var seconds = 0

    companion object {
        fun getActioning(path: String?, seconds: Int): VoiceMessageTrigger {
            val vm = VoiceMessageTrigger()
            vm.path = path
            vm.seconds = seconds
            vm.setActioning()
            return vm
        }
    }
}