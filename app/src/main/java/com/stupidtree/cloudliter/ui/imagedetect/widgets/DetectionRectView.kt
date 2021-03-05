package com.stupidtree.cloudliter.ui.imagedetect.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.stupidtree.cloudliter.R;
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier;

public class DetectionRectView extends View {
    Classifier.Recognition recognition;
    float windowWidth;
    Paint p = new Paint();
    Paint p2 = new Paint();

    public DetectionRectView(Context context, Classifier.Recognition recognition,float windowWidth) {
        super(context);
        this.recognition = recognition;
        this.windowWidth = windowWidth;
    }

    public DetectionRectView(Context context) {
        super(context);
    }

    public DetectionRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DetectionRectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        p.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        p.setStrokeWidth(windowWidth/100);
        p.setAlpha(188);
        p2.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        p2.setAlpha(60);
        canvas.drawLine(0,0,0,getHeight(),p);
        canvas.drawLine(getWidth(),0,getWidth(),getHeight(),p);
        canvas.drawLine(0,0,getWidth(),0,p);
        canvas.drawLine(0,getHeight(),getWidth(),getHeight(),p);
        canvas.drawRect(0,0,getWidth(),getHeight(),p2);
    }
}
