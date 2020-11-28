package com.stupidtree.cloudliter.ui.group


import com.stupidtree.cloudliter.ui.base.Trigger
class RenameGroupTrigger : Trigger() {
    var groupId: String? = null
    var name: String? = null

    companion object {
        fun getActioning(groupId: String?, name: String?): RenameGroupTrigger {
            val renameGroupTrigger = RenameGroupTrigger()
            renameGroupTrigger.setActioning()
            renameGroupTrigger.groupId = groupId
            renameGroupTrigger.name = name
            return renameGroupTrigger
        }
    }
}