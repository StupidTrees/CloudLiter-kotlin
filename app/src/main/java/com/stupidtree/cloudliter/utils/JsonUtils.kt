package com.stupidtree.cloudliter.utils

import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject

/**
 * 此类整合了一些JSON格式数据解析有关的函数
 */
object JsonUtils {
    fun getIntegerData(jo: JsonObject?, key: String?): Int? {
        return try {
            jo?.get(key)?.asInt?:0
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getStringData(jo: JsonObject?, key: String?): String? {
        return try {
            jo?.get(key)?.asString
        } catch (ignored: Exception) {
            null
        }
    }

    fun getObjectData(jo: JsonObject?, key: String?): JsonObject? {
        return try {
            jo?.get(key)?.asJsonObject
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun getJSONObject(str: String?): JSONObject? {
        return try {
            str?.let {
                JSONObject(it)
            }
        } catch (e: JSONException) {
            null
        }
    }
}