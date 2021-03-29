package com.stupidtree.cloudliter.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stupidtree.cloudliter.data.model.ChatMessage
import java.sql.Timestamp

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM message WHERE conversationId IS :conversationId AND createdAt < :fromTime   ORDER BY createdAt DESC LIMIT :pageSize ")
    fun getMessages(conversationId: String, pageSize: Int, fromTime: Timestamp?): LiveData<List<ChatMessage>?>

    @Query("SELECT * FROM message WHERE conversationId IS :conversationId  ORDER BY createdAt DESC LIMIT :pageSize")
    fun getMessages(conversationId: String, pageSize: Int): LiveData<List<ChatMessage>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMessage(data: List<ChatMessage>)

    @Query("UPDATE message SET read = 1 WHERE id IS :messageId")
    fun messageRead(messageId: String)

    @Query("UPDATE message SET read = 1 WHERE conversationId IS :conversationId AND createdAt >= :fromTime")
    fun messageAllRead(conversationId: String, fromTime: Timestamp?)

    @Query("DELETE FROM message")
    fun clearTable()

    @Query("DELETE FROM message WHERE conversationId in (select id from conversation where friendId is :friendId)")
    fun clearConversation(friendId:String)
}