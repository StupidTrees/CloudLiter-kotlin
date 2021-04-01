package com.stupidtree.cloudliter.data.model

import androidx.room.Entity


@Entity(tableName = "accessibility_info")
class AccessibilityInfo {
    var conversationId: String = ""
    var visual: Int = 0
    var hearing: Int = 0
    var limb: Int = 0
}