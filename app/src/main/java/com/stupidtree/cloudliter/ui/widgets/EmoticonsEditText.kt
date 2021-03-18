package com.stupidtree.cloudliter.ui.widgets

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.TextUtils

class EmoticonsEditText : AppCompatEditText {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun setText(text: CharSequence, type: BufferType) {
        if (!TextUtils.isEmpty(text)) {
            super.setText(replace(text.toString()), type)
        } else {
            super.setText(text, type)
        }
    }

    override fun append(text: CharSequence, start: Int, end: Int) {
        if (!TextUtils.isEmpty(text)) {
            super.append(replace(text.toString()), start, end)
        } else {
            super.append(text, start, end)
        }
    }

    private fun replace(text: String): CharSequence {
        return try {
            val spannableString = SpannableString(text)
            var start = 0
            val pattern = EmoticonsTextView.buildPattern()
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                val faceText = matcher.group()
                var key = faceText.substring(1)
                key = key.substring(key.indexOf("[") + 1, key.indexOf("]"))
                val imageId = context.resources.getIdentifier("yunmoji_$key", "drawable", context.packageName)
                val bitmap = ImageUtils.getBitmapFromDrawable(context, imageId, textSize * 1.3f)
                val imageSpan = ImageSpan(context, bitmap)
                val startIndex = text.indexOf(faceText, start)
                val endIndex = startIndex + faceText.length
                if (startIndex >= 0) spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = endIndex - 1
            }
            spannableString
        } catch (e: Exception) {
            text
        }
    }
}