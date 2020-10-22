package com.stupidtree.hichat.data;


import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * ROOM需要使用转换器将时间戳转换为Date
 */
public class TypeConverters {
    @TypeConverter
    public static Long fromTimestamp(Timestamp value) {
        return value == null ? null : value.getTime();
    }

    @TypeConverter
    public static Timestamp dateToTimestamp(Long date) {
        return date == null ? null : new Timestamp(date);
    }

}