package com.stupidtree.cloudliter.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import com.stupidtree.cloudliter.R;
import com.stupidtree.cloudliter.ui.widgets.PopUpText;

import java.util.Collections;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationUtils {
    /**
     * 检测是否开启通知
     *
     * @param context
     */
    public static void checkNotification(final AppCompatActivity context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Android 8.0及以上
            initNotification(context);
            NotificationChannel channel = mNotificationManager.getNotificationChannel("cloudLiterMessageChanel");//CHANNEL_ID是自己定义的渠道ID
            if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_DEFAULT) {//未开启
                new PopUpText().setTitle(R.string.attention_please)
                        .setDialogCancelable(false)
                        .setText(context.getString(R.string.need_that_freaking_notification_permission))
                        .setOnConfirmListener(() -> setNotification(context, channel)).show(context.getSupportFragmentManager(), "attention");
            }
        }

    }

    static void initNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("cloudLiterMessageChanel", "云升", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点    channel.setLightColor(Color.RED); //小红点颜色    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知    notificationManager.createNotificationChannel(channel);}
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            notificationManager.createNotificationChannels(Collections.singletonList(channel));
            // notificationManager.createNotificationChannels();
        }

    }


    /**
     * 如果没有开启通知，跳转至设置界面
     *
     * @param context
     */
    private static void setNotification(Context context, NotificationChannel channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
            context.startActivity(intent);
        }
    }


}
