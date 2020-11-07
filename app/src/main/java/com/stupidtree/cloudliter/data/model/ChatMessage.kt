package com.stupidtree.cloudliter.data.model

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.utils.TextUtils
import java.io.Serializable
import java.sql.Timestamp
import java.util.*

@Entity(tableName = "message")
class ChatMessage : Serializable {
    enum class TYPE {
        TXT, IMG, VOICE
    }

    /**
     * 和服务器数据实体一致的属性
     */
    @PrimaryKey
    var id: String = ""
    var fromId: String? = null
    var toId: String? = null

    @ColumnInfo(defaultValue = "TXT")
    var type: String? = null
    var content: String? = null

    @Ignore
    var friendRemark: String? = null

    @Ignore
    var friendAvatar: String? = null
    var conversationId: String? = null
    var relationId: String? = null
    var read = false
    var sensitive = false
    var emotion = 0f
    var createdAt: Timestamp? = null
    var updatedAt: Timestamp? = null
    var extra: String? = null
    //long createdTime;

    /**
     * 服务器上不保存的属性
     */
    //是否正在发送
    @Ignore
    var isProgressing: Boolean

    enum class VOICE_STATE { PLAYING, PAUSED, STOPPED }

    @Ignore
    var playing: VOICE_STATE = VOICE_STATE.STOPPED

    @Ignore
    var uuid: String? = null

    constructor() {
        isProgressing = false
    }

    @Ignore
    constructor(fromId: String?, toId: String?, content: String?) {
        this.fromId = fromId
        this.toId = toId
        this.content = content
        type = "TXT"
        conversationId = if (fromId == null || toId == null) {
            null
        } else {
            TextUtils.getP2PIdOrdered(fromId, toId)
        }
        createdAt = Timestamp(System.currentTimeMillis())
        uuid = UUID.randomUUID().toString()
        isProgressing = true
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    val isTimeStamp: Boolean
        get() = id == "time"

    fun getType(): TYPE {

        return when (type) {
            "IMG" -> TYPE.IMG
            "VOICE" -> TYPE.VOICE
            else -> TYPE.TXT
        }
    }

    fun setType(type: TYPE) {
        this.type = type.name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val message = other as ChatMessage
        return id == message.id &&
                fromId == message.fromId &&
                toId == message.toId &&
                conversationId == message.conversationId &&
                sensitive == message.sensitive &&
                emotion == message.emotion &&
                read == message.read
    }

    override fun hashCode(): Int {
        return Objects.hash(id, fromId, toId, conversationId, relationId)
    }

    /**
     * 将extra字段解析为分词
     *
     * @return 分词列表
     */
    fun getExtraAsSegmentation(): MutableList<String> {
        val result: MutableList<String> = ArrayList()
        Log.e("getExtra", this.toString())
        try {
            for (s in Gson().fromJson<List<*>>(extra, MutableList::class.java)) {
                result.add(s.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 将extra字段解析为图片敏感检测结果
     *
     * @return 检测结果
     */
    fun getExtraAsImageAnalyse(): HashMap<String, Float> {
        val result = HashMap<String, Float>()
        try {
            val jo = Gson().fromJson(extra.toString(), JsonObject::class.java)
            for ((key, value) in jo.entrySet()) {
                result[key] = java.lang.Float.valueOf(value.asFloat.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    companion object {
        fun getTimeStampHolderInstance(timestamp: Timestamp?): ChatMessage {
            val cm = ChatMessage(null, null, null)
            cm.id = "time"
            cm.createdAt = timestamp
            return cm
        }
    }
}