package com.stupidtree.hichat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.source.UserPreferenceSource;

import java.util.Objects;

/**
 * 此类封装了加载用户头像的各个方法
 */
public class ImageUtils {

    public static void loadAvatarInto(@NonNull Context context, String filename, @NonNull ImageView target) {
        if (TextUtils.isEmpty(filename)) {
            target.setImageResource(R.drawable.ic_baseline_emoji_emotions_24);
        } else {
            GlideUrl glideUrl = new GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                    filename, new LazyHeaders.Builder().addHeader("device-type", "android").build());
            Glide.with(context).load(glideUrl
            ).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(target);
        }

    }

    public static void loadLocalAvatarInto(@NonNull Context context, String filename, @NonNull ImageView target) {
        String sign = UserPreferenceSource.getInstance(context).getMyAvatarGlideSignature();
        if (TextUtils.isEmpty(filename)) {
            target.setImageResource(R.drawable.ic_baseline_emoji_emotions_24);
        } else {
            GlideUrl glideUrl = new GlideUrl("http://hita.store:3000/user/profile/avatar?path=" +
                    filename, new LazyHeaders.Builder().addHeader("device-type", "android").build());
            Glide.with(context).load(glideUrl
            ).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .signature(new ObjectKey(sign))
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into(target);
           // p.edit().putString("my_avatar","normal").apply();
        }

    }
}
