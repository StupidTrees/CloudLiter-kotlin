package com.stupidtree.hichat.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hichat.R;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;

/**
 * 此类整合了一些文本处理有关的函数
 */
public class TextUtils {
    public static boolean isEmpty(CharSequence text) {
        return android.text.TextUtils.isEmpty(text);
    }

    static public boolean isUsernameValid(String username) {
        return !isEmpty(username) && username.length() > 3;
    }

    static public boolean isPasswordValid(String password) {
        return !isEmpty(password) && password.length() >= 8;
    }

    /**
     * 获得聊天列表的日期字符串（模仿微信）
     *
     * @param context   上下文
     * @param timestamp 时间
     * @return 字符串
     */
    static public String getConversationTimeText(Context context, Timestamp timestamp) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timestamp.getTime());
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf_year = new SimpleDateFormat(context.getString(R.string.date_format_1), Locale.getDefault());
        SimpleDateFormat sdf_month = new SimpleDateFormat(context.getString(R.string.date_format_2), Locale.getDefault());
        SimpleDateFormat sdf_am_hour = new SimpleDateFormat(context.getString(R.string.date_format_am), Locale.getDefault());
        SimpleDateFormat sdf_pm_hour = new SimpleDateFormat(context.getString(R.string.date_format_pm), Locale.getDefault());

        Calendar yesterday = getDateAddedCalendar(now, -1);
        Calendar theDayBeforeYesterday = getDateAddedCalendar(now, -2);

        if (isSameDay(time, now)) { //今天
            if (time.get(Calendar.AM_PM) == Calendar.AM) {
                return sdf_am_hour.format(time.getTime());
            } else {
                return sdf_pm_hour.format(time.getTime());
            }

        } else if (isSameDay(time, yesterday)) {//昨天
            return context.getString(R.string.yesterday);
        } else if (isSameDay(time, theDayBeforeYesterday)) {
            return context.getString(R.string.the_day_before_yesterday); //前天
        } else if (time.get(Calendar.YEAR) != now.get(Calendar.YEAR)) { //不是同一年
            return sdf_year.format(time.getTime());
        } else if (time.get(Calendar.MONTH) != now.get(Calendar.MONTH)) { //不是同个月
            return sdf_month.format(time.getTime());
        } else {
            return sdf_month.format(time.getTime());
        }
    }

    /**
     * 获得聊天窗口内的时间戳字符串（模仿QQ）
     *
     * @param context   上下文
     * @param timestamp 时间
     * @return 字符串
     */
    static public String getChatTimeText(Context context, Timestamp timestamp) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timestamp.getTime());
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf_year = new SimpleDateFormat(context.getString(R.string.date_format_1), Locale.getDefault());
        SimpleDateFormat sdf_month = new SimpleDateFormat(context.getString(R.string.date_format_2), Locale.getDefault());
        SimpleDateFormat sdf_am_hour = new SimpleDateFormat(context.getString(R.string.date_format_am), Locale.getDefault());
        SimpleDateFormat sdf_pm_hour = new SimpleDateFormat(context.getString(R.string.date_format_pm), Locale.getDefault());

        Calendar yesterday = getDateAddedCalendar(now, -1);
        Calendar theDayBeforeYesterday = getDateAddedCalendar(now, -2);

        if (isSameDay(time, now)) { //今天
            if (time.get(Calendar.AM_PM) == Calendar.AM) {
                return sdf_am_hour.format(time.getTime());
            } else {
                return sdf_pm_hour.format(time.getTime());
            }

        } else if (isSameDay(time, yesterday) || isSameDay(time, theDayBeforeYesterday)) {//昨天、前天
            String prefix = isSameDay(time, yesterday) ? context.getString(R.string.yesterday) : context.getString(R.string.the_day_before_yesterday);
            if (time.get(Calendar.AM_PM) == Calendar.AM) {
                return context.getString(R.string.date_format_with_prefix, prefix, sdf_am_hour.format(time.getTime()));
            } else {
                return context.getString(R.string.date_format_with_prefix, prefix, sdf_pm_hour.format(time.getTime()));
            }
        } else if (time.get(Calendar.YEAR) != now.get(Calendar.YEAR)) { //不是同一年
            return sdf_year.format(time.getTime());
        } else if (time.get(Calendar.MONTH) != now.get(Calendar.MONTH)) { //不是同个月
            return sdf_month.format(time.getTime());
        } else {
            return sdf_month.format(time.getTime());
        }
    }


    static private Calendar getDateAddedCalendar(Calendar c, int day) {
        Calendar n = (Calendar) c.clone();
        n.add(Calendar.DATE, day);
        return n;
    }

    static private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
                && a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH);
    }


    @NonNull
    public static String getP2PIdOrdered(String id1, String id2) {
        try {
            long i1 = Long.parseLong(id1);
            long i2 = Long.parseLong(id2);
            return Math.min(i1, i2) + "-" + Math.max(i1, i2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return id1+"-"+id2;
        }
    }

    public static String orderP2PId(String p2pId) {
        String[] res = p2pId.split("-");
        if(res.length>1){
            return getP2PIdOrdered(res[0],res[1]);
        }
        return p2pId;
    }
}
