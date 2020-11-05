package com.stupidtree.cloudliter.utils

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserProfile.COLOR

object ColorUtils {
    @ColorInt
    fun getColorByEnum(context: Context?, color: COLOR?): Int {
        val res: Int = when (color) {
            COLOR.RED -> R.color.profileRed
            COLOR.ORANGE -> R.color.profileOrange
            COLOR.YELLOW -> R.color.profileYellow
            COLOR.GREEN -> R.color.profileGreen
            COLOR.CYAN -> R.color.profileCyan
            COLOR.PURPLE -> R.color.profilePurple
            else -> R.color.profileBlue
        }
        return ContextCompat.getColor(context!!, res)
    }
}