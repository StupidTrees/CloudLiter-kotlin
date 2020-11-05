package com.stupidtree.cloudliter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.NotificationTarget
import com.bumptech.glide.signature.ObjectKey
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.source.UserPreferenceSource.Companion.getInstance

/**
 * 此类封装了加载用户头像的各个方法
 * 以及各种图形函数
 */
object ImageUtils {
    fun loadAvatarNoCacheInto(context: Context, filename: String?, target: ImageView) {
        if (TextUtils.isEmpty(filename)) {
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
        if (TextUtils.isEmpty(filename)) {
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
        if (TextUtils.isEmpty(filename)) {
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
        if (TextUtils.isEmpty(filename)) {
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
}