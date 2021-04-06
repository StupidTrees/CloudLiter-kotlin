package com.stupidtree.cloudliter.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Animation.INFINITE
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation


/**
 * 动画控制函数
 */
object AnimationUtils {
    /**
     * 旋转view
     *
     * @param view view
     * @param down 是否转向下
     */
    fun rotateTo(view: View, down: Boolean) {
        val fromD: Float
        val toD: Float
        if (down) {
            fromD = 0f
            toD = 180f
        } else {
            fromD = 180f
            toD = 0f
        }
        val ra = RotateAnimation(fromD, toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        ra.interpolator = DecelerateInterpolator()
        ra.duration = 400 //设置动画持续周期
        ra.repeatCount = 0 //设置重复次数
        ra.fillAfter = true //动画执行完后是否停留在执行完的状态
        view.animation = ra
        view.startAnimation(ra)
    }

    fun rotate(view: View) {
        val fromD = 0f
        val toD = 360f
        val ra = RotateAnimation(fromD, toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        ra.interpolator = DecelerateInterpolator()
        ra.duration = 400 //设置动画持续周期
        ra.repeatCount = 0 //设置重复次数
        ra.fillAfter = true //动画执行完后是否停留在执行完的状态
        view.animation = ra
        view.startAnimation(ra)
    }

    /**
     * 顺时针旋转view(90°)
     *
     * @param view view
     * @param down 是否向下
     */
    fun rotateRightQuarterTo(view: View, down: Boolean) {
        val fromD: Float
        val toD: Float
        if (down) {
            fromD = 0f
            toD = 90f
        } else {
            fromD = 90f
            toD = 0f
        }
        val va = ValueAnimator.ofFloat(fromD, toD)
        va.duration = 360
        va.interpolator = DecelerateInterpolator()
        va.addUpdateListener { valueAnimator: ValueAnimator -> view.rotation = (valueAnimator.animatedValue as Float) }
        va.start()
    }

    @SuppressLint("WrongConstant")
    fun floatAnim(view: View, delay: Int = 0, durationX: Long = 2200, durationY: Long = 4000, rangeX: Float = 6.0f, rangeY: Float = 24.0f) {
        view.clearAnimation()
        val animators: MutableList<Animator> = ArrayList()
        val translationXAnim = ObjectAnimator.ofFloat(view, "translationX", -rangeX, rangeX, -rangeX)
        translationXAnim.duration = durationX
        translationXAnim.repeatCount = ValueAnimator.INFINITE //无限循环
        translationXAnim.repeatMode = INFINITE
        translationXAnim.interpolator = AccelerateDecelerateInterpolator()
        translationXAnim.start()

        animators.add(translationXAnim)
        val translationYAnim = ObjectAnimator.ofFloat(view, "translationY", -rangeY, rangeY, -rangeY)
        translationYAnim.duration = durationY
        translationYAnim.repeatCount = ValueAnimator.INFINITE
        translationYAnim.repeatMode = INFINITE
        translationYAnim.interpolator = AccelerateDecelerateInterpolator()
        translationYAnim.start()
        animators.add(translationYAnim)
        val btnSexAnimatorSet = AnimatorSet()
        btnSexAnimatorSet.playTogether(animators)
        btnSexAnimatorSet.startDelay = delay.toLong()
        btnSexAnimatorSet.start()
    }
}