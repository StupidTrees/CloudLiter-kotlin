package com.stupidtree.accessibility.structure

import android.content.Context

class TopBar: VisualStructure() {
    override fun getDescription(context: Context): String {
        return "标题栏"
    }
}