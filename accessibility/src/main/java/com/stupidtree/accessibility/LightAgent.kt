package com.stupidtree.accessibility

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stupidtree.accessibility.structure.*
import java.lang.ref.WeakReference

/**
 * 针对某个窗口的代理
 */
class LightAgent(activity: Activity) {
    val window: WeakReference<Activity> = WeakReference(activity)
    private val rootStructure = RootStructure()
    private val structureOfViews = mutableMapOf<View, VisualStructure?>()

    init {
        traverseWindow(activity.window.decorView,null,rootStructure,activity.window.decorView)
    }


    private fun traverseWindow(v: View, parentStructure:VisualStructure?,ancestorStructure:VisualStructure,decView:View) {
        structureOfViews[v] = parentStructure
        val structure = getStructureForView(v)
        structure?.let { visualStructure ->
            visualStructure.init(v,decView)
            parentStructure?.let {
                parentStructure.addChild(it)
            }?: kotlin.run {
                ancestorStructure.addChild(visualStructure)
            }
        }
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                traverseWindow(v.getChildAt(i), structure,structure?:ancestorStructure,decView)
            }
        }

    }


    private fun getStructureForView(v: View): VisualStructure? {
        return when (v) {
            is androidx.appcompat.widget.Toolbar -> TopBar()
            is Toolbar -> TopBar()
            is BottomNavigationView -> BottomNavigation()
            is DrawerLayout -> Drawer()
            else -> null
        }
    }


    companion object {
        fun initForWindow(activity: Activity): LightAgent {
            return LightAgent(activity)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LightAgent

        if (window.get() != other.window.get()) return false

        return true
    }

    override fun hashCode(): Int {
        return window.get().hashCode()
    }

}