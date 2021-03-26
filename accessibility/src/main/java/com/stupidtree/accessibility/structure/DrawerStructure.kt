package com.stupidtree.accessibility.structure

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import com.stupidtree.accessibility.R

class DrawerStructure : VisualStructure() {

    override fun init(view: View) {
        super.init(view)
        if (view is DrawerLayout) {
            view.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

                }

                override fun onDrawerOpened(drawerView: View) {
                    drawerView.announceForAccessibility(drawerView.context.getString(R.string.drawer_opened))
                }

                override fun onDrawerClosed(drawerView: View) {
                    drawerView.announceForAccessibility(view.context.getString(R.string.drawer_closed))
                }

                override fun onDrawerStateChanged(newState: Int) {

                }

            })
        }
    }

    override fun initLocation(view: View) {
        if (view is ViewGroup) {
            //取最上层view的layout-gravity
            for (i in 0 until view.childCount) {
                val lp = view.getChildAt(view.childCount - 1 - i).layoutParams
                if (lp is DrawerLayout.LayoutParams) {
                    locationHorizontal = when {
                        lp.gravity.and(Gravity.END) == Gravity.END -> LOCATION.RIGHT
                        lp.gravity.and(Gravity.START) == Gravity.START -> LOCATION.LEFT
                        else -> LOCATION.CENTER
                    }
                    break
                }
            }
        }
    }



    override fun getDescription(context: Context): String {
        val str = if (locationHorizontal == LOCATION.LEFT) context.getString(R.string.drawer_left)
        else context.getString(R.string.drawer_right)
        return str+super.getDescription(context)
    }

}