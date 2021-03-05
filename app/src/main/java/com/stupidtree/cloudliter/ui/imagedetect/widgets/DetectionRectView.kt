package com.stupidtree.cloudliter.ui.imagedetect.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.ui.imagedetect.DetectResult

class DetectionRectView : View {
    @JvmField
    var recognition: DetectResult? = null
    var windowWidth = 0f
    var p = Paint()
    var p2 = Paint()

    constructor(context: Context?, recognition: DetectResult, windowWidth: Float) : super(context) {
        this.recognition = recognition
        this.windowWidth = windowWidth
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        p.color = ContextCompat.getColor(context, R.color.colorPrimary)
        p.strokeWidth = windowWidth / 100
        p.alpha = 188
        p2.color = ContextCompat.getColor(context, R.color.colorPrimary)
        p2.alpha = 60
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), p)
        canvas.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), p)
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, p)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), p)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p2)
    }
}