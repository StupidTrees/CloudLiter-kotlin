package com.stupidtree.cloudliter.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.ui.widgets.PopUpText

object NotificationUtils {
    /**
     * 检测是否开启通知
     *
     * @param context
     */
    fun checkNotification(context: AppCompatActivity) {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Android 8.0及以上
            initNotification(context)
            val channel = mNotificationManager.getNotificationChannel("cloudLiterMessageChanel") //CHANNEL_ID是自己定义的渠道ID
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_DEFAULT) { //未开启
                PopUpText().setTitle(R.string.attention_please)
                        .setDialogCancelable(false)
                        .setText(context.getString(R.string.need_that_freaking_notification_permission))
                        .setOnConfirmListener(object :PopUpText.OnConfirmListener{
                            override fun OnConfirm() {
                                setNotification(context, channel)
                            }
                        }).show(context.supportFragmentManager, "attention")
            }
        }
    }

    fun initNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("cloudLiterMessageChanel", "云升", NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true) //是否在桌面icon右上角展示小红点    channel.setLightColor(Color.RED); //小红点颜色    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知    notificationManager.createNotificationChannel(channel);}
            channel.importance = NotificationManager.IMPORTANCE_DEFAULT
            channel.enableVibration(false)
            channel.vibrationPattern = longArrayOf(0)
            channel.setSound(null, null)
            notificationManager.createNotificationChannels(listOf(channel))
            // notificationManager.createNotificationChannels();
        }
    }

    /**
     * 如果没有开启通知，跳转至设置界面
     *
     * @param context
     */
    private fun setNotification(context: Context, channel: NotificationChannel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.id)
            context.startActivity(intent)
        }
    }
}