package com.stupidtree.accessibility.structure

import android.content.Context

class PagerStructure : VisualStructure() {

    override fun getDescription(context: Context): String {
        return "包含有可切换页面 " + super.getDescription(context)
    }
}