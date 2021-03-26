package com.stupidtree.accessibility.structure

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout

class ToolbarStructure: VisualStructure() {

    var appBarLayout:AppBarLayout?=null
    var collapsingLayout:CollapsingToolbarLayout?=null

    override fun init(view: View) {
        super.init(view)
        findElements(view)
    }

    private fun findElements(view:View){
        when (view) {
            is CollapsingToolbarLayout -> {
                collapsingLayout = view
            }
            is AppBarLayout -> {
                appBarLayout = view
            }
            is TabLayout -> {
                val ts = TabStructure()
                ts.init(view)
                addChild(ts)
            }
        }
        if(view is ViewGroup){
            for(i in 0 until view.childCount){
                val child = view.getChildAt(i)

                findElements(child)
            }
        }
    }

    override fun getDescription(context: Context): String {
        return collapsingLayout?.let {
            "可收缩标题栏"
        }?:run{
            "标题栏"
        }
    }

    override fun swallowChildren(): Boolean {
        return true
    }
}