package com.stupidtree.accessibility

import android.app.Activity
import android.graphics.Rect
import android.view.View
import androidx.appcompat.widget.Toolbar

object ViewUtils {


    /**
     * 获得view在group中的相对位置（pixels）
     * @param view 子view
     * @param group 容器
     * @return 相对位置
     */
    fun getChildPositionInView(view: View, group: View): Rect {
        val result = Rect()
        val viewLoc = IntArray(2)
        view.getLocationOnScreen(viewLoc)
        val decLoc = IntArray(2)
        group.getLocationOnScreen(decLoc)
        result.left = viewLoc[0] - decLoc[0]
        result.top = viewLoc[1] - decLoc[1]
        result.right = result.left + view.measuredWidth
        result.bottom = result.top + view.measuredHeight
        return result
    }

}