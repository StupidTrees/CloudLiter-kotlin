package com.stupidtree.cloudliter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.NotificationTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.source.websource.UserPreferenceSource.Companion.getInstance
import com.stupidtree.cloudliter.utils.TextUtils.isEmpty
import java.util.*

/**
 * 此类封装了加载用户头像的各个方法
 * 以及各种图形函数
 */
object ImageUtils {
    fun loadAvatarNoCacheInto(context: Context, filename: String?, target: ImageView) {
        if (isEmpty(filename)) {
            target.setImageResource(R.drawable.place_holder_avatar)
        } else {
            val glideUrl = GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                    filename, LazyHeaders.Builder().addHeader("device-type", "android").build())
            Glide.with(context).load(glideUrl
            ).apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.place_holder_avatar)
                    .into(target)
        }
    }

    fun loadAvatarInto(context: Context, filename: String?, target: ImageView) {
        if (isEmpty(filename)) {
            target.setImageResource(R.drawable.place_holder_avatar)
        } else {
            val glideUrl = GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                    filename, LazyHeaders.Builder().addHeader("device-type", "android").build())
            Glide.with(context).load(glideUrl
            ).apply(RequestOptions.bitmapTransform(CircleCrop())).placeholder(R.drawable.place_holder_avatar).into(target)
        }
    }

    /**
     * 加载聊天文件
     * @param context 上下文
     * @param filename 图片路径
     * @param target 目标ImageView
     */
    fun loadChatMessageInto(context: Context, filename: String, target: ImageView) {
        if (isEmpty(filename)) {
            target.setImageResource(R.drawable.place_holder_avatar)
        } else {
            val glideUrl = GlideUrl(getChatMessageImageUrl(filename), LazyHeaders.Builder().addHeader("device-type", "android").build())
            Glide.with(context).load(glideUrl)
                    .placeholder(R.drawable.place_holder_loading) //.apply(RequestOptions.bitmapTransform(new CornerTransform(context,dp2px(context,12))))
                    .into(target)
        }
    }

    fun getChatMessageImageUrl(filename: String): String {
        return "http://hita.store:3000/message/image?path=" +
                filename
    }

    fun loadAvatarIntoNotification(context: Context, filename: String, target: NotificationTarget) {
        // Log.e("path",filename);
        Glide.with(context.applicationContext) // safer!
                .asBitmap()
                .load("http://hita.store:3000/user/profile/avatar?path=$filename")
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(target)
    }

    fun loadLocalAvatarInto(context: Context, filename: String?, target: ImageView) {
        val sign = getInstance(context)!!.myAvatarGlideSignature
        if (isEmpty(filename)) {
            target.setImageResource(R.drawable.place_holder_avatar)
        } else {
            val glideUrl = GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                    filename, LazyHeaders.Builder().addHeader("device-type", "android").build())
            Glide.with(context).load(glideUrl
            ).apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .signature(ObjectKey(sign))
                    .placeholder(R.drawable.place_holder_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into(target)
            // p.edit().putString("my_avatar","normal").apply();
        }
    }

    fun loadUserAvatar(context: Context, filename: String?):MutableLiveData<Bitmap?>{
        val result = MutableLiveData<Bitmap?>()
        val glideUrl = GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                filename, LazyHeaders.Builder().addHeader("device-type", "android").build())
        Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.place_holder_avatar)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(object :CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        result.value = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        TODO("Not yet implemented")
                    }
                });
        // p.edit().putString("my_avatar","normal").apply();
        return result
    }

    /**
     * 从Drawable资源中获取bitmap图片（正方形）
     * @param context 上下文
     * @param drawableId 资源id
     * @param size 宽度（像素）
     * @return bitmap对象
     */
    fun getBitmapFromDrawable(context: Context?, @DrawableRes drawableId: Int, @Px size: Float): Bitmap {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        val result: Bitmap
        result = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawable || drawable is VectorDrawableCompat) {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
        return imageScale(result, size, size)
        // return result;
    }

    private fun imageScale(bitmap: Bitmap, dst_w: Float, dst_h: Float): Bitmap {
        val src_w = bitmap.width
        val src_h = bitmap.height
        val scale_w = dst_w / src_w
        val scale_h = dst_h / src_h
        val matrix = Matrix()
        matrix.postScale(scale_w, scale_h)
        return Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true)
    }

    /**
     * convert dp to its equivalent px
     *
     * 将dp转换为与之相等的px
     */
    fun dp2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


    /**
     * 根据内容生成二维码
     */
    fun createQRCodeBitmap(content: String?,
                           width: Int,
                           height: Int,
                           character_set: String?,
                           error_correction_level: String?,
                           margin: String?,
                           color_black: Int,
                           color_white: Int): Bitmap? {
        // 字符串内容判空
        if (isEmpty(content)) {
            return null
        }
        // 宽和高>=0
        return if (width < 0 || height < 0) {
            null
        } else try {
            /** 1.设置二维码相关配置  */
            val hints: Hashtable<EncodeHintType, String> = Hashtable()
            // 字符转码格式设置
            if (!isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set)
            }
            // 容错率设置
            if (!isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level)
            }
            // 空白边距设置
            if (!isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin)
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象  */
            val bitMatrix: BitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值  */
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black //黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white // 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象  */
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

}