package com.stupidtree.cloudliter.data

import androidx.room.TypeConverter
import java.sql.Timestamp

/**
 * ROOM需要使用转换器将时间戳转换为Date
 */
object TypeConverters {
    @JvmStatic
    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.time
    }

    @JvmStatic
    @TypeConverter
    fun dateToTimestamp(date: Long?): Timestamp? {
        return date?.let { Timestamp(it) }
    }
}