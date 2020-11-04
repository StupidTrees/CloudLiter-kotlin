package com.stupidtree.cloudliter.ui.widgets

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.widget.TextView.BufferType
import android.text.SpannableString
import com.stupidtree.cloudliter.ui.widgets.EmoticonsTextView
import android.graphics.Bitmap
import com.stupidtree.cloudliter.utils.ImageUtils
import android.text.style.ImageSpan
import android.text.Spannable
import android.util.AttributeSet
import com.stupidtree.cloudliter.utils.TextUtils
import java.lang.Exception
import java.util.regex.Pattern

class EmoticonsTextView : AppCompatTextView {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}

    override fun setText(text: CharSequence, type: BufferType) {
        if (!TextUtils.isEmpty(text)) {
            super.setText(replace(text.toString()), type)
        } else {
            super.setText(text, type)
        }
    }

    private fun replace(text: String): CharSequence {
        return try {
            val spannableString = SpannableString(text)
            var start = 0
            val pattern = buildPattern()
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

    companion object {
        fun buildPattern(): Pattern {
            return Pattern.compile("\\[y[0-9]{3}]", Pattern.CASE_INSENSITIVE)
        }
    }
}