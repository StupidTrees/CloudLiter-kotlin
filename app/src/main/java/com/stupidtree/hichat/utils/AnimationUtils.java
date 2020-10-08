package com.stupidtree.hichat.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;

/**
 * 动画控制函数
 */
public class AnimationUtils {

    /**
     * 旋转view
     * @param view view
     * @param down 是否转向下
     */
    public static void rotateTo(View view,boolean down) {
        float fromD, toD;
        if (down) {
            fromD = 0f;
            toD = 180f;
        } else {
            fromD = 180f;
            toD = 0f;
        }
        RotateAnimation ra = new RotateAnimation(fromD, toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setInterpolator(new DecelerateInterpolator());
        ra.setDuration(400);//设置动画持续周期
        ra.setRepeatCount(0);//设置重复次数
        ra.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        view.setAnimation(ra);
        view.startAnimation(ra);
    }
}
