package com.stupidtree.accessibility.structure

import android.content.Context
import android.view.View
import com.stupidtree.accessibility.ViewUtils

abstract class VisualStructure {
    enum class LOCATION {
        TOP, BOTTOM, LEFT, RIGHT, CENTER
    }

    var locationHorizontal: LOCATION? = LOCATION.CENTER
    var locationVertical: LOCATION? = LOCATION.CENTER

    private val children: MutableList<VisualStructure> = mutableListOf()

    fun addChild(vs: VisualStructure) {
        children.add(vs)
    }

    fun init(view: View, decView: View) {
        initLocation(view,decView)
    }

    open fun initLocation(view: View,decView: View){
        val rect = ViewUtils.getChildPositionInView(view, decView)
        val h = (rect.top + rect.bottom) / (2 * decView.measuredHeight)
        val v = (rect.left + rect.right) / (2 * decView.measuredWidth)
        locationHorizontal = when {
            h < 0.35f -> LOCATION.LEFT
            h < 0.65f -> LOCATION.CENTER
            else -> LOCATION.RIGHT
        }
        locationVertical = when {
            v < 0.35f -> LOCATION.TOP
            v < 0.65f -> LOCATION.CENTER
            else -> LOCATION.BOTTOM
        }
    }

    abstract fun getDescription(context: Context): String
}