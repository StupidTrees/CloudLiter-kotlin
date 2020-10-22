package com.stupidtree.hichat.data.model;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hichat.utils.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@Entity(tableName = "message")
public class ChatMessage implements Serializable {
    public enum TYPE {TXT, IMG}

    /**
     * 和服务器数据实体一致的属性
     */
    @PrimaryKey
    @NotNull
    public Long id;
    public String fromId;
    public String toId;
    @ColumnInfo(defaultValue = "TXT")
    public String type;
    public String content;
    @Ignore
    public String friendRemark;
    @Ignore
    public String friendAvatar;
    public String conversationId;
    public String relationId;
    public boolean read;
    public boolean sensitive;
    public float emotion;
    public Timestamp createdAt;
    public Timestamp updatedAt;
    public String extra;
    //long createdTime;

    /**
     * 服务器上不保存的属性
     */
    //是否正在发送
    @Ignore
    boolean progressing;
    @Ignore
    String uuid;

    public ChatMessage() {
        progressing = false;
    }

    @Ignore
    public ChatMessage(String fromId, String toId, String content) {
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        this.type = "TXT";
        if (fromId == null || toId == null) {
            conversationId = null;
        } else {
            conversationId = TextUtils.getP2PIdOrdered(fromId, toId);
        }
        createdAt = new Timestamp(System.currentTimeMillis());
        uuid = UUID.randomUUID().toString();
        progressing = true;
    }

    public static ChatMessage getTimeStampHolderInstance(Timestamp timestamp) {
        ChatMessage cm = new ChatMessage(null, null, null);
        cm.id = (long) -1;
        cm.createdAt = timestamp;
        return cm;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getCreatedTime() {
        return createdAt;
    }

    @NotNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getFriendRemark() {
        return friendRemark;
    }

    @NotNull
    public Long getId() {
        return id;
    }

    public boolean isTimeStamp() {
        return Objects.equals(id, (long) -1);
    }

    public TYPE getType() {
        return Objects.equals(type, "IMG") ? TYPE.IMG : TYPE.TXT;
    }

    public void setType(TYPE type) {
        this.type = type.name();
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUuid() {
        return uuid;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getRelationId() {
        return relationId;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setProgressing(boolean progressing) {
        this.progressing = progressing;
    }

    public boolean isProgressing() {
        return progressing;
    }

    public float getEmotion() {
        return emotion;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage message = (ChatMessage) o;
        return Objects.equals(id, message.id) &&
                Objects.equals(fromId, message.fromId) &&
                Objects.equals(toId, message.toId) &&
                Objects.equals(conversationId, message.conversationId) &&
                Objects.equals(relationId, message.relationId) &&
                Objects.equals(sensitive, message.sensitive) &&
                Objects.equals(emotion, message.emotion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromId, toId, conversationId, relationId);
    }


    /**
     * 将extra字段解析为分词
     *
     * @return 分词列表
     */
    @NotNull
    public List<String> getExtraAsSegmentation() {
        List<String> result = new ArrayList<>();
        Log.e("getExtra", String.valueOf(this));
        try {
            for (Object s : new Gson().fromJson(extra, List.class)) {
                result.add(String.valueOf(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将extra字段解析为图片敏感检测结果
     *
     * @return 检测结果
     */
    @NotNull
    public HashMap<String, Float> getExtraAsImageAnalyse() {
        HashMap<String, Float> result = new HashMap<>();
        try {
            JsonObject jo = new Gson().fromJson(extra.toString(), JsonObject.class);
            for (Map.Entry<String, JsonElement> e : jo.entrySet()) {
                result.put(e.getKey(), Float.valueOf(String.valueOf(e.getValue().getAsFloat())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
