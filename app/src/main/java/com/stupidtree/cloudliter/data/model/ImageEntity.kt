package com.stupidtree.cloudliter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.HashMap

@Entity(tableName = "image")
class ImageEntity {
    @PrimaryKey
    var id:String = ""
    var fromId:String? = ""
    var toId:String? = ""
    var fileName:String = ""
    var sensitive:String? = ""

    var scene:String? = null
   // var tags:List<String> = listOf()

    /**
     * 将extra字段解析为图片敏感检  测结果
     *
     * @return 检测结果
     */
    fun getExtraAsImageAnalyse(): HashMap<String, Float> {
        val result = HashMap<String, Float>()
        try {
            val jo = Gson().fromJson(sensitive, JsonObject::class.java)
            for ((key, value) in jo.entrySet()) {
                result[key] = java.lang.Float.valueOf(value.asFloat.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}