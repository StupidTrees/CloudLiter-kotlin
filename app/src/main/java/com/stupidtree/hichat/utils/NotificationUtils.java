package com.stupidtree.hichat.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.widgets.PopUpText;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NotificationUtils {
    /**
     * 检测是否开启通知
     *
     * @param context
     */
    public static void checkNotification(final AppCompatActivity context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Android 8.0及以上
            NotificationChannel channel = mNotificationManager.getNotificationChannel("cloudLiterMessageChanel");//CHANNEL_ID是自己定义的渠道ID
            if (channel.getImportance() == NotificationManager.IMPORTANCE_DEFAULT) {//未开启
                new PopUpText().setTitle(R.string.attention_please)
                        .setDialogCancelable(false)
                        .setText(context.getString(R.string.need_that_freaking_notification_permission))
                        .setOnConfirmListener(() -> setNotification(context,channel)).show(context.getSupportFragmentManager(),"attention");
            }
        }

    }
    /**
     * 如果没有开启通知，跳转至设置界面
     *
     * @param context
     */
    private static void setNotification(Context context,NotificationChannel channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
            context.startActivity(intent);
        }
    }


}
