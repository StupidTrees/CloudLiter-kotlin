package com.stupidtree.cloudliter.data

import androidx.room.TypeConverter
import com.stupidtree.cloudliter.data.model.UserLocal
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

    @JvmStatic
    @TypeConverter
    fun genderToString(date: UserLocal.GENDER): String {
        return date.name
    }

    @JvmStatic
    @TypeConverter
    fun stringToGender(str:String): UserLocal.GENDER {
        return UserLocal.GENDER.valueOf(str)
    }

    @JvmStatic
    @TypeConverter
    fun accessibilityToString(date: UserLocal.ACCESSIBILITY): String {
        return date.name
    }

    @JvmStatic
    @TypeConverter
    fun stringToAccessibility(str:String): UserLocal.ACCESSIBILITY {
        return UserLocal.ACCESSIBILITY.valueOf(str)
    }
}