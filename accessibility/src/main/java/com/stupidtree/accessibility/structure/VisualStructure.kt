package com.stupidtree.accessibility.structure

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
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

    open fun init(view: View) {
        initLocation(view)
        view.contentDescription = getDescription(view.context)
    }

    open fun swallowChildren():Boolean{
        return false
    }
    open fun initLocation(view: View) {
        when (val lp = view.layoutParams) {
            is LinearLayout.LayoutParams -> {
                locationFromGravity(lp.gravity)
            }
            is FrameLayout.LayoutParams -> {
                locationFromGravity(lp.gravity)
            }
            is CoordinatorLayout.LayoutParams -> {
                locationFromGravity(lp.gravity)
            }
            is DrawerLayout.LayoutParams -> {
                locationFromGravity(lp.gravity)
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun locationFromGravity(gravity: Int) {
        if (gravity.and(Gravity.CENTER) == Gravity.CENTER) {
            locationVertical = LOCATION.CENTER
            locationHorizontal = LOCATION.CENTER
            return
        }
        if (gravity.and(Gravity.START) == Gravity.START || gravity.and(Gravity.LEFT) == Gravity.LEFT) {
            locationHorizontal = LOCATION.LEFT
        } else if (gravity.and(Gravity.END) == Gravity.END || gravity.and(Gravity.RIGHT) == Gravity.RIGHT) {
            locationHorizontal = LOCATION.RIGHT
        } else if (gravity.and(Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL) {
            locationHorizontal = LOCATION.CENTER
        }
        when {
            gravity.and(Gravity.TOP) == Gravity.TOP -> {
                locationVertical = LOCATION.TOP
            }
            gravity.and(Gravity.BOTTOM) == Gravity.BOTTOM -> {
                locationVertical = LOCATION.BOTTOM
            }
            gravity.and(Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL -> {
                locationVertical = LOCATION.CENTER
            }
        }

    }

    open fun getDescription(context: Context): String {
        val sb = StringBuilder()
        for (child in children) {
            sb.append(child.getDescription(context)).append(", ")
        }
        return sb.toString()
    }
}