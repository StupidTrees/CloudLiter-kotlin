package com.stupidtree.hichat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.preference.Preference;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.source.UserPreferenceSource;

import java.util.Objects;

/**
 * 此类封装了加载用户头像的各个方法
 * 以及各种图形函数
 */
public class ImageUtils {

    public static void loadAvatarNoCacheInto(@NonNull Context context, String filename, @NonNull ImageView target) {
        if (TextUtils.isEmpty(filename)) {
            target.setImageResource(R.drawable.place_holder_avatar);
        } else {
            GlideUrl glideUrl = new GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                    filename, new LazyHeaders.Builder().addHeader("device-type", "android").build());
            Glide.with(context).load(glideUrl
            ).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.place_holder_avatar)
                    .into(target);
        }

    }
    public static void loadAvatarInto(@NonNull Context context, String filename, @NonNull ImageView target) {
        if (TextUtils.isEmpty(filename)) {
            target.setImageResource(R.drawable.place_holder_avatar);
        } else {
            GlideUrl glideUrl = new GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                    filename, new LazyHeaders.Builder().addHeader("device-type", "android").build());
            Glide.with(context).load(glideUrl
            ).apply(RequestOptions.bitmapTransform(new CircleCrop())).placeholder(R.drawable.place_holder_avatar).into(target);
        }

    }

    /**
     * 加载聊天文件
     * @param context 上下文
     * @param filename 图片路径
     * @param target 目标ImageView
     */
    public static void loadChatMessageInto(@NonNull Context context, String filename, @NonNull ImageView target) {
        if (TextUtils.isEmpty(filename)) {
            target.setImageResource(R.drawable.place_holder_avatar);
        } else {
            GlideUrl glideUrl = new GlideUrl("http://hita.store:3000/message/image?path=" +
                    filename, new LazyHeaders.Builder().addHeader("device-type", "android").build());
            Glide.with(context).load(glideUrl)
                    .placeholder(R.drawable.place_holder_loading)
                    //.apply(RequestOptions.bitmapTransform(new CornerTransform(context,dp2px(context,12))))
                    .into(target);
        }

    }

    public static void loadAvatarIntoNotification(@NonNull Context context, @NonNull String filename,NotificationTarget target){
       // Log.e("path",filename);
        Glide.with( context.getApplicationContext() ) // safer!
                .asBitmap()
                .load("http://hita.store:3000/user/profile/avatar?path=" + filename)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(target);
    }
    public static void loadLocalAvatarInto(@NonNull Context context, String filename, @NonNull ImageView target) {
        String sign = UserPreferenceSource.getInstance(context).getMyAvatarGlideSignature();
        if (TextUtils.isEmpty(filename)) {
            target.setImageResource(R.drawable.place_holder_avatar);
        } else {
            GlideUrl glideUrl = new GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                    filename, new LazyHeaders.Builder().addHeader("device-type", "android").build());
            Glide.with(context).load(glideUrl
            ).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .signature(new ObjectKey(sign))
                    .placeholder(R.drawable.place_holder_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into(target);
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
    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId,@Px float size) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap result;
        if (drawable instanceof BitmapDrawable) {
           result= ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0,canvas.getWidth(),canvas.getHeight());
            drawable.draw(canvas);
            result = bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
        return imageScale(result,size,size);
       // return result;
    }


    private static Bitmap imageScale(Bitmap bitmap, float dst_w, float dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = dst_w / src_w;
        float scale_h = dst_h / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        return Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
    }


    /**
     * convert dp to its equivalent px
     *
     * 将dp转换为与之相等的px
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
