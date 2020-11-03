package com.stupidtree.cloudliter.ui.base

class StringTrigger : Trigger() {
    var data: String? = null

    companion object {
        fun getActioning(data: String?): StringTrigger {
            val stringTrigger = StringTrigger()
            stringTrigger.setActioning()
            stringTrigger.data = data
            return stringTrigger
        }
    }
}