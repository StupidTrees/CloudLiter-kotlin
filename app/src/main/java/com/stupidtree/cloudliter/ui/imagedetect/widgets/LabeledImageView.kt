package com.stupidtree.cloudliter.ui.imagedetect.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.stupidtree.cloudliter.ui.imagedetect.DetectResult

class LabeledImageView : ViewGroup {
    var imageBitmap: Bitmap? = null
    var mWidth = 0
    var mHeight = 0
    private var labels: List<DetectResult>? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    fun setImage(image: Bitmap?) {
        imageBitmap = image
        removeAllViews()
        requestLayout()
        background = BitmapDrawable(image)
        //        ImageView imageView = new ImageView(getContext());
//        imageView.setImageBitmap(imageBitmap);
        // addView(imageView);
        //invalidate();
    }

    fun updateLabels(labels: List<DetectResult>) {
        this.labels = labels
        removeAllLabels()
        for (r in labels) {
            val v: View = DetectionRectView(context, r, mWidth.toFloat())
            v.contentDescription = r.name
            addView(v)
        }
    }

    private fun removeAllLabels() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            (child as? DetectionRectView)?.let { removeView(it) }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var ratio = 1f
        if (imageBitmap == null) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.EXACTLY))
            mHeight = 0
            mWidth = 0
        } else {
            ratio = imageBitmap!!.height.toFloat() / imageBitmap!!.width.toFloat()
            mWidth = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.UNSPECIFIED)
            mHeight = (mWidth * ratio).toInt()
            setMeasuredDimension(mWidth, mHeight)
        }
        val ratioW = mWidth / 416f
        val ratioH = mHeight / 416f
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child is DetectionRectView) {
                val loc = child.recognition!!.rect
                val cw = MeasureSpec.makeMeasureSpec((ratioW * loc.width()).toInt(), MeasureSpec.EXACTLY)
                val cH = MeasureSpec.makeMeasureSpec((ratioH * loc.height()).toInt(), MeasureSpec.EXACTLY)
                child.measure(cw, cH)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount //获得子控件个数
        val ratioW = mWidth / 416f
        val ratioH = mHeight / 416f
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child is DetectionRectView) {
                val loc = child.recognition!!.rect
                val left = (ratioW * (loc.centerX() - loc.width() / 2)).toInt()
                val right = (ratioW * (loc.centerX() + loc.width() / 2)).toInt()
                val top = (ratioH * (loc.centerY() - loc.height() / 2)).toInt()
                val bottom = (ratioH * (loc.centerY() + loc.height() / 2)).toInt()
                child.layout(left, top, right, bottom)
            }
        }
    }
}