package com.stupidtree.accessibility.structure

import android.content.Context
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScrollableStructure : VisualStructure() {
    var horizontalScrollable: Boolean = false
    var verticalScrollable: Boolean = false

    override fun init(view: View) {
        super.init(view)
        if (view is RecyclerView) {
            val lm = view.layoutManager
            if (lm is LinearLayoutManager) {
                horizontalScrollable = lm.orientation == RecyclerView.HORIZONTAL
                verticalScrollable = lm.orientation == RecyclerView.VERTICAL
            } else if (lm is GridLayoutManager) {
                horizontalScrollable = lm.orientation == RecyclerView.HORIZONTAL
                verticalScrollable = lm.orientation == RecyclerView.VERTICAL
            }
        } else if (view is NestedScrollView || view is ScrollView) {
            horizontalScrollable = false
            verticalScrollable = true
        } else if (view is HorizontalScrollView) {
            horizontalScrollable = true
            verticalScrollable = false
        }
    }

    override fun swallowChildren(): Boolean {
        return true
    }

    override fun getDescription(context: Context): String {
        val str = when {
            horizontalScrollable && verticalScrollable -> "可自由滚动页面"
            horizontalScrollable && !verticalScrollable -> "可横向滚动页面"
            !horizontalScrollable && verticalScrollable -> "可纵向滚动页面"
            else -> "可滚动页面"
        }
        return "包含" + str + "," + super.getDescription(context)
    }
}