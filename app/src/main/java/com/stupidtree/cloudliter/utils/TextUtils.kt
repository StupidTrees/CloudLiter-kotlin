package com.stupidtree.cloudliter.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * 此类整合了一些文本处理有关的函数
 */
object TextUtils {
    fun isEmpty(text: CharSequence?): Boolean {
        return TextUtils.isEmpty(text)
    }

    fun isUsernameValid(username: String?): Boolean {
        if (username != null) {
            return !isEmpty(username) && username.length > 3
        }
        return false
    }

    fun isPasswordValid(password: String?): Boolean {
        if (password != null) {
            return !isEmpty(password) && password.length >= 8
        }
        return false
    }

    /**
     * 获得聊天列表的日期字符串（模仿微信）
     *
     * @param context   上下文
     * @param timestamp 时间
     * @return 字符串
     */
    fun getConversationTimeText(context: Context, timestamp: Timestamp?): String {
        val time = Calendar.getInstance()
        time.timeInMillis = timestamp?.time ?: -1
        val now = Calendar.getInstance()
        val sdf_year = SimpleDateFormat(context.getString(R.string.date_format_1), Locale.getDefault())
        val sdf_month = SimpleDateFormat(context.getString(R.string.date_format_2), Locale.getDefault())
        val sdf_am_hour = SimpleDateFormat(context.getString(R.string.date_format_am), Locale.getDefault())
        val sdf_pm_hour = SimpleDateFormat(context.getString(R.string.date_format_pm), Locale.getDefault())
        val yesterday = getDateAddedCalendar(now, -1)
        val theDayBeforeYesterday = getDateAddedCalendar(now, -2)
        return if (isSameDay(time, now)) { //今天
            if (time[Calendar.AM_PM] == Calendar.AM) {
                sdf_am_hour.format(time.time)
            } else {
                sdf_pm_hour.format(time.time)
            }
        } else if (isSameDay(time, yesterday)) { //昨天
            context.getString(R.string.yesterday)
        } else if (isSameDay(time, theDayBeforeYesterday)) {
            context.getString(R.string.the_day_before_yesterday) //前天
        } else if (time[Calendar.YEAR] != now[Calendar.YEAR]) { //不是同一年
            sdf_year.format(time.time)
        } else if (time[Calendar.MONTH] != now[Calendar.MONTH]) { //不是同个月
            sdf_month.format(time.time)
        } else {
            sdf_month.format(time.time)
        }
    }

    /**
     * 获得聊天窗口内的时间戳字符串（模仿QQ）
     *
     * @param context   上下文
     * @param timestamp 时间
     * @return 字符串
     */
    fun getChatTimeText(context: Context, timestamp: Timestamp?): String {
        val time = Calendar.getInstance()
        time.timeInMillis = timestamp?.time ?: -1
        val now = Calendar.getInstance()
        val sdf_year = SimpleDateFormat(context.getString(R.string.date_format_1), Locale.getDefault())
        val sdf_month = SimpleDateFormat(context.getString(R.string.date_format_2), Locale.getDefault())
        val sdf_am_hour = SimpleDateFormat(context.getString(R.string.date_format_am), Locale.getDefault())
        val sdf_pm_hour = SimpleDateFormat(context.getString(R.string.date_format_pm), Locale.getDefault())
        val yesterday = getDateAddedCalendar(now, -1)
        val theDayBeforeYesterday = getDateAddedCalendar(now, -2)
        return if (isSameDay(time, now)) { //今天
            if (time[Calendar.AM_PM] == Calendar.AM) {
                sdf_am_hour.format(time.time)
            } else {
                sdf_pm_hour.format(time.time)
            }
        } else if (isSameDay(time, yesterday) || isSameDay(time, theDayBeforeYesterday)) { //昨天、前天
            val prefix = if (isSameDay(time, yesterday)) context.getString(R.string.yesterday) else context.getString(R.string.the_day_before_yesterday)
            if (time[Calendar.AM_PM] == Calendar.AM) {
                context.getString(R.string.date_format_with_prefix, prefix, sdf_am_hour.format(time.time))
            } else {
                context.getString(R.string.date_format_with_prefix, prefix, sdf_pm_hour.format(time.time))
            }
        } else if (time[Calendar.YEAR] != now[Calendar.YEAR]) { //不是同一年
            sdf_year.format(time.time)
        } else if (time[Calendar.MONTH] != now[Calendar.MONTH]) { //不是同个月
            sdf_month.format(time.time)
        } else {
            sdf_month.format(time.time)
        }
    }


    /**
     * 获取聊天语音的时长文本
     */
    fun getVoiceTimeText(context: Context,seconds:Int):String{
        val min = seconds/60
        val sec = seconds%60
        return context.getString(R.string.voice_duration_text,min,sec)
    }

    private fun getDateAddedCalendar(c: Calendar, day: Int): Calendar {
        val n = c.clone() as Calendar
        n.add(Calendar.DATE, day)
        return n
    }


    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a[Calendar.YEAR] == b[Calendar.YEAR] && a[Calendar.MONTH] == b[Calendar.MONTH] && a[Calendar.DAY_OF_MONTH] == b[Calendar.DAY_OF_MONTH]
    }

    fun getP2PIdOrdered(id1: String, id2: String): String {
        return try {
            val i1 = id1.toLong()
            val i2 = id2.toLong()
            Math.min(i1, i2).toString() + "-" + Math.max(i1, i2)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            "$id1-$id2"
        }
    }

    fun orderP2PId(p2pId: String): String {
        val res = p2pId.split("-".toRegex()).toTypedArray()
        return if (res.size > 1) {
            getP2PIdOrdered(res[0], res[1])
        } else p2pId
    }


    fun encodeUserBusinessCard(userLocal:UserLocal):String{
        val jo = HashMap<String, Any?>()
        jo["userId"] = userLocal.id
        jo["time"] = System.currentTimeMillis()
        val str = Gson().toJson(jo)
        return Base64Utils.encode(str);
    }

    fun decodeUserBusinessCard(encoded:String):HashMap<String, Any?>?{
        return try {
            val decoded = Base64Utils.decode(encoded)
            Gson().fromJson<HashMap<String, Any?>>(decoded,HashMap::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}