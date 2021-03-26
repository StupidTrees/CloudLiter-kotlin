package com.stupidtree.accessibility

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.stupidtree.accessibility.structure.*
import java.lang.ref.WeakReference

/**
 * 针对某个窗口的代理
 */
class LightAgent(activity: Activity) {
    val window: WeakReference<Activity> = WeakReference(activity)
    private val rootStructure = object : VisualStructure() {}
    private val structureOfViews = mutableMapOf<View, VisualStructure?>()

    init {
        traverseWindow(activity.window.decorView, null, rootStructure, activity.window.decorView)
    }


    private fun traverseWindow(v: View, parentStructure: VisualStructure?, ancestorStructure: VisualStructure, decView: View) {
        structureOfViews[v] = parentStructure
        val structure = getStructureForView(v)
        structure?.let { visualStructure ->
            visualStructure.init(v)
            parentStructure?.let {
                parentStructure.addChild(it)
            } ?: kotlin.run {
                ancestorStructure.addChild(visualStructure)
            }
        }
        if(structure is ToolbarStructure){
            v.accessibilityTraversalAfter = NO_ID
            v.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        if (v is ViewGroup
                && structure?.swallowChildren() != true) {
            for (i in 0 until v.childCount) {
                traverseWindow(v.getChildAt(i), structure, structure ?: ancestorStructure, decView)
            }
        }

    }


    fun getPageDescription(context: Context): String {
        return rootStructure.getDescription(context)
    }


    private fun getStructureForView(v: View): VisualStructure? {
        return when (v) {
            is androidx.appcompat.widget.Toolbar, is Toolbar, is AppBarLayout, is CollapsingToolbarLayout -> ToolbarStructure()
            is BottomNavigationView -> NavigationStructure()
            is DrawerLayout -> DrawerStructure()
            is TabLayout -> TabStructure()
            is ViewPager, is ViewPager2 -> PagerStructure()
            is RecyclerView, is NestedScrollView, is ScrollView, is HorizontalScrollView -> ScrollableStructure()
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