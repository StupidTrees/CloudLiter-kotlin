package com.stupidtree.cloudliter.utils;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.stupidtree.cloudliter.R;
import com.stupidtree.cloudliter.data.model.UserProfile;

public class ColorUtils {

    @ColorInt
    public static int getColorByEnum(Context context, UserProfile.COLOR color){
        int res;
        switch (color){
            case RED:res =  R.color.profileRed;
            break;
            case ORANGE:res =  R.color.profileOrange;
            break;
            case YELLOW:res =  R.color.profileYellow;
            break;
            case GREEN:res =  R.color.profileGreen;
            break;
            case CYAN:res =  R.color.profileCyan;
            break;
            case PURPLE:res =  R.color.profilePurple;
            break;
            default:res =  R.color.profileBlue;
        }
        return ContextCompat.getColor(context,res);
    }
}
