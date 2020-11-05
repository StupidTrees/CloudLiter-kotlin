package com.stupidtree.cloudliter.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.stupidtree.cloudliter.data.model.WordsTag
import java.util.*

class WordsCloudView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {
    private var paint: Paint? = null
    private var paint1: Paint? = null
    private var mWidth = 0
    private var mHeight = 0
    private val showTags: MutableList<WordsTag> = ArrayList()
    private var tags: List<String>? = null
    private fun initView() {
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.color = Color.BLACK
        paint1 = Paint()
        paint1!!.isAntiAlias = true
        paint1!!.color = Color.parseColor("#B7B7B7")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(mWidth, mHeight)
        showTags.clear()
        computeSingleRect(tags, dp2px(30), 0, 0, mWidth, mHeight)
    }

    fun setData(tags: List<String>?) {
//        if (tags == null || tags.size() == 0) {
//            throw new IllegalArgumentException("tags can not be null...");
//        }
        requireNotNull(tags) { "tags can not be null..." }
        this.tags = tags
        showTags.clear()
        computeSingleRect(tags, dp2px(30), 0, 0, mWidth, mHeight)
        invalidate()
    }

    fun isUsed(wordsTags: List<WordsTag>, name: String): Boolean {
        for (wordsTag in wordsTags) {
            return name == wordsTag.name
        }
        return false
    }

    private var cal = 0
    private fun computeSingleRect(tags: List<String>?, textSize: Int, pLeft: Int, pTop: Int, pRight: Int, pBottom: Int) {
        var mTextSize = textSize
        if (tags == null || tags.isEmpty() || mTextSize < MIN_TEXT_SIZE || pBottom == 0 || pRight == 0 || pLeft >= pRight || pTop >= pBottom) {
            return
        }
        var cLeft: Int
        var cTop: Int
        var cRight = 0
        var cBottom = 0
        var textWidth: Int
        var textHeight: Int
        val size = tags.size
        var index = (Math.random() * size).toInt()
        if (cal < tags.size) {
            index = cal
            cal++
        }
        val name = tags[index]

        //计算当前rect的宽高
        val rectWidth = pRight - pLeft
        val rectHeight = pBottom - pTop
        if (rectWidth > rectHeight) {
            //父布局长大于高，横向布局合适
            paint!!.textSize = mTextSize.toFloat()
            textWidth = paint!!.measureText(name).toInt()
            textHeight = (paint!!.fontMetrics.bottom - paint!!.fontMetrics.top).toInt()
            if (textHeight > rectHeight) {
                //记录之前的textsize
                val beforeTextSize = mTextSize
                while (textHeight > rectHeight) {
                    mTextSize--
                    paint!!.textSize = mTextSize.toFloat()
                    textHeight = (paint!!.fontMetrics.bottom - paint!!.fontMetrics.top).toInt()
                }
                textWidth = paint!!.measureText(name).toInt()
                while (textWidth > rectWidth) {
                    mTextSize--
                    paint!!.textSize = mTextSize.toFloat()
                    textWidth = paint!!.measureText(name).toInt()
                }
                if (mTextSize < MIN_TEXT_SIZE) {
                    return
                }
                textHeight = (paint!!.fontMetrics.bottom - paint!!.fontMetrics.top).toInt()
                cLeft = pLeft
                cTop = pTop
                cRight = textWidth + pLeft
                cBottom = textHeight + pTop
                showTags.add(WordsTag(name, mTextSize, cLeft, cTop, cRight, cBottom))
                textWidth = paint!!.measureText(name).toInt()
                if (pRight - cRight > textWidth) {
                    //右
                    computeSingleRect(tags, beforeTextSize, cRight, pTop, pRight, pBottom)
                } else {
                    //右
                    computeSingleRect(tags, --mTextSize, cRight, pTop, pRight, pBottom)
                }
            } else {
                if (textWidth >= rectWidth) {
                    while (textWidth > rectWidth) {
                        mTextSize--
                        paint!!.textSize = mTextSize.toFloat()
                        textWidth = paint!!.measureText(name).toInt()
                    }
                    if (mTextSize < MIN_TEXT_SIZE) {
                        return
                    }
                    textHeight = (paint!!.fontMetrics.bottom - paint!!.fontMetrics.top).toInt()
                    cLeft = pLeft
                    cTop = pTop
                    cRight = pRight
                    cBottom = cTop + textHeight
                    showTags.add(WordsTag(name, mTextSize, cLeft, cTop, cRight, cBottom))

                    //下
                    mTextSize += 4
                    computeSingleRect(tags, mTextSize, cLeft, cBottom, cRight, pBottom)
                } else {
                    cLeft = (Math.random() * (rectWidth / 3)).toInt() + pLeft // 除以3是为了尽快找到合适的位置
                    while (cLeft + textWidth > pRight) {
                        cLeft--
                    }
                    cTop = (Math.random() * (rectHeight / 2)).toInt() + pTop
                    while (cTop + textHeight > pBottom) {
                        cTop--
                    }
                    cRight = cLeft + textWidth
                    cBottom = cTop + textHeight
                    showTags.add(WordsTag(name, mTextSize, cLeft, cTop, cRight, cBottom))
                    //左
                    computeSingleRect(tags, --mTextSize, pLeft, pTop, cLeft, cBottom)
                    //上
                    computeSingleRect(tags, --mTextSize, cLeft, pTop, pRight, cTop)
                    //右
                    computeSingleRect(tags, --mTextSize, cRight, cTop, pRight, pBottom)
                    //下
                    computeSingleRect(tags, --mTextSize, pLeft, cBottom, cRight, pBottom)
                }
            }
        } else {
            //父布局高大于长，纵向布局合适
            val beforeTextSize = mTextSize
            paint!!.textSize = mTextSize.toFloat()
            textHeight = (paint!!.fontMetrics.bottom - paint!!.fontMetrics.top).toInt()
            while (textHeight * name.length > rectHeight) {
                mTextSize--
                paint!!.textSize = mTextSize.toFloat()
                textHeight = (paint!!.fontMetrics.bottom - paint!!.fontMetrics.top).toInt()
            }
            if (mTextSize < MIN_TEXT_SIZE) {
                return
            }
            textWidth = (paint!!.measureText(name) / name.length).toInt()
            val length = name.length
            if (pLeft + textWidth > pRight) {
                //右 右边空间不足
                computeSingleRect(tags, --mTextSize, pLeft, pTop, pRight, pBottom)
                return
            }
            for (i in 0 until length) {
                cLeft = pLeft
                cTop = pTop + i * textHeight
                cRight = cLeft + textWidth
                cBottom = cTop + textHeight
                showTags.add(WordsTag(name[i].toString(), mTextSize, cLeft, cTop, cRight, cBottom))
            }
            if (pRight - cRight > textWidth) {
                //右
                computeSingleRect(tags, beforeTextSize, cRight, pTop, pRight, cBottom)
                //下
                computeSingleRect(tags, --mTextSize, pLeft, cBottom, pRight, pBottom)
            } else {
                //右
                computeSingleRect(tags, --mTextSize, cRight, pTop, pRight, cBottom)
                //下
                computeSingleRect(tags, --mTextSize, pLeft, cBottom, pRight, pBottom)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("---------->", showTags.size.toString() + "个")
        Log.d("-------->", tags.toString() + "haha")
        for (name in tags!!) {
//            float maxTextSize = 0;
            @SuppressLint("DrawAllocation") var maxWordsTag = WordsTag("", 0, 0, 0, 0, 0)
            for (wordsTag in showTags) {
                if (wordsTag.name == name && maxWordsTag.textsize <= wordsTag.textsize) {
//                    maxTextSize = wordsTag.getTextsize();
                    maxWordsTag = wordsTag
                }
            }
            Log.d("----->", maxWordsTag.name + maxWordsTag.textsize)
            paint!!.setTextSize(maxWordsTag.textsize.toFloat())
            canvas.drawText(maxWordsTag.name, maxWordsTag.left.toFloat(), maxWordsTag.bottom - paint!!.fontMetrics.bottom, paint!!)
            showTags.remove(maxWordsTag)
        }
        for (showTag in showTags) {
            paint!!.textSize = showTag.textsize.toFloat()
            paint1!!.textSize = showTag.textsize.toFloat()
            canvas.drawText(showTag.name, showTag.left.toFloat(), showTag.bottom - paint!!.fontMetrics.bottom, paint1!!)
        }
    }

    companion object {
        const val MIN_TEXT_SIZE = 14
        fun dp2px(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

    init {
        initView()
    }
}