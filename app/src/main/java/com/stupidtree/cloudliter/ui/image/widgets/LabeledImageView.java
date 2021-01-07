package com.stupidtree.cloudliter.ui.image.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.stupidtree.cloudliter.R;
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier;

import java.util.List;

public class LabeledImageView extends ViewGroup {
    Bitmap imageBitmap = null;
    int width, height;
    List<Classifier.Recognition> labels;


    public LabeledImageView(Context context) {
        super(context);
    }

    public LabeledImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LabeledImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LabeledImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImage(Bitmap image) {
        this.imageBitmap = image;
        removeAllViews();
        requestLayout();
        setBackground(new BitmapDrawable(image));
//        ImageView imageView = new ImageView(getContext());
//        imageView.setImageBitmap(imageBitmap);
        // addView(imageView);
        //invalidate();
    }

    public void setLabels(List<Classifier.Recognition> labels) {
        this.labels = labels;
        removeAllLabels();
        for (Classifier.Recognition r : labels) {
            View v = new DetectionRectView(getContext(), r,width);
            v.setContentDescription(r.getTitle());
            addView(v);
        }
    }

    private void removeAllLabels() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof DetectionRectView) {
                removeView(child);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float ratio = 1f;
        if (imageBitmap == null) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.EXACTLY));
            height = 0;
            width = 0;
        } else {
            ratio = (float)imageBitmap.getHeight()/(float) imageBitmap.getWidth();
            width = MeasureSpec.makeMeasureSpec(widthMeasureSpec,MeasureSpec.UNSPECIFIED);
            height = (int) (width*ratio);
            setMeasuredDimension(width,height);
        }
        float ratioW = width / 416f;
        float ratioH = height/ 416f;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof DetectionRectView) {
                DetectionRectView rv = (DetectionRectView) child;
                RectF loc = rv.recognition.getLocation();
                int cw = MeasureSpec.makeMeasureSpec((int) (ratioW * loc.width()), MeasureSpec.EXACTLY);
                int cH = MeasureSpec.makeMeasureSpec((int) (ratioH * loc.height()), MeasureSpec.EXACTLY);
                child.measure(cw, cH);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();//获得子控件个数
        float ratioW = width/ 416f;
        float ratioH = height/ 416f;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof DetectionRectView) {
                DetectionRectView dr = (DetectionRectView) child;
                RectF loc = dr.recognition.getLocation();
                int left = (int) (ratioW * (loc.centerX() - loc.width() / 2));
                int right = (int) (ratioW * (loc.centerX() + loc.width() / 2));
                int top = (int) (ratioH * (loc.centerY() - loc.height() / 2));
                int bottom = (int) (ratioH * (loc.centerY() + loc.height() / 2));
                child.layout(left, top, right, bottom);
            }
        }
    }
}
