package com.stupidtree.hichat.ui.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmoticonsTextView extends androidx.appcompat.widget.AppCompatTextView {

    public EmoticonsTextView(Context context) {
        super(context);
    }

    public EmoticonsTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EmoticonsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        if (!TextUtils.isEmpty(text)) {
            super.setText(replace(text.toString()), type);
        } else {
            super.setText(text, type);
        }
    }

    public static  Pattern buildPattern() {
        return Pattern.compile("\\[y[0-9]{3}]", Pattern.CASE_INSENSITIVE);
    }

    private CharSequence replace(String text) {
        try {
            SpannableString spannableString = new SpannableString(text);
            int start = 0;
            Pattern pattern = buildPattern();
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String faceText = matcher.group();
                String key = faceText.substring(1);
                key = key.substring(key.indexOf("[") + 1, key.indexOf("]"));
                int imageId = getContext().getResources().getIdentifier("yunmoji_" + key, "drawable", getContext().getPackageName());
                Bitmap bitmap = ImageUtils.getBitmapFromDrawable(getContext(), imageId,getTextSize()*1.2f);
                ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
                int startIndex = text.indexOf(faceText, start);
                int endIndex = startIndex + faceText.length();
                if (startIndex >= 0)
                    spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = (endIndex - 1);
            }
            return spannableString;
        } catch (Exception e) {
            return text;
        }
    }
}
