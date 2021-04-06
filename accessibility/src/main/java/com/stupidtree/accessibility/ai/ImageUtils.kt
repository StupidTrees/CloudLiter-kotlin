package com.stupidtree.accessibility.ai

import android.graphics.Bitmap
import android.graphics.Color
import com.stupidtree.accessibility.ai.icon.IconClassifierSource
import java.nio.ByteBuffer

object ImageUtils {


    fun getFloat(arr: ByteArray, index: Int, reverse: Boolean = true): Float {
        val intVal = if (reverse) {
            -0x1000000 and (arr[index + 3].toInt() shl 24) or
                    (0x00ff0000 and (arr[index + 2].toInt() shl 16)) or
                    (0x0000ff00 and (arr[index + 1].toInt() shl 8)) or
                    (0x000000ff and arr[index + 0].toInt())
        } else {
            -0x1000000 and (arr[index + 0].toInt() shl 24) or
                    (0x00ff0000 and (arr[index + 1].toInt() shl 16)) or
                    (0x0000ff00 and (arr[index + 2].toInt() shl 8)) or
                    (0x000000ff and arr[index + 3].toInt())
        }

        return java.lang.Float.intBitsToFloat(intVal)
    }


    fun convertBitmapToByteBuffer(imageData: ByteBuffer?, bitmap: Bitmap) {
        if (imageData == null) {
            return
        }
        imageData.rewind()
        val intValues = IntArray(IconClassifierSource.IMAGE_SIZE_X * IconClassifierSource.IMAGE_SIZE_Y)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until IconClassifierSource.IMAGE_SIZE_X) {
            for (j in 0 until IconClassifierSource.IMAGE_SIZE_Y) {
                val color: Int = intValues[pixel++]
                val r: Int = Color.red(color) //取得此像素点的r(红色)分量
                val g: Int = Color.green(color) //取得此像素点的g(绿色)分量
                val b: Int = Color.blue(color) //取得此像素点的b(蓝色分量)
                val a: Int = Color.alpha(color) //取得此像素点的a通道值
                //此公式将r,g,b运算获得灰度值，经验公式不需要理解
                var gray = (r.toFloat() * 0.3f + g.toFloat() * 0.59f + b.toFloat() * 0.11f)
                if (a <= 50) {
                    gray = 255f
                }
//                //下面前两个if用来做溢出处理，防止灰度公式得到到灰度超出范围（0-255）
//                if (gray > 255f) {
//                    gray = 255f
//                }
//                if (gray < 0f) {
//                    gray = 0f
//                }
//                if (gray != 0f) { //如果某像素的灰度值不是0(黑色)就将其置为255（白色）
//                    gray = 255f
//                }
                imageData.putFloat((gray / 255.0).toFloat())
            }
        }
    }

}