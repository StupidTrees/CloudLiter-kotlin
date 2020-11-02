package com.stupidtree.cloudliter.data.source;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.stupidtree.cloudliter.data.model.ChatMessage;

import java.sql.Timestamp;
import java.util.List;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM message WHERE conversationId IS :conversationId AND createdAt < :fromTime   ORDER BY id DESC LIMIT :pageSize ")
    LiveData<List<ChatMessage>> getMessages(String conversationId, int pageSize, Timestamp fromTime);

    @Query("SELECT * FROM message WHERE conversationId IS :conversationId  ORDER BY id DESC LIMIT :pageSize")
    LiveData<List<ChatMessage>> getMessages(String conversationId,int pageSize);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveMessage(List<ChatMessage> data);


    @Query("UPDATE message SET read = 1 WHERE id IS :messageId")
    void messageRead(String messageId);

    @Query("UPDATE message SET read = 1 WHERE conversationId IS :conversationId AND createdAt >= :fromTime")
    void messageAllRead(String conversationId,Timestamp fromTime);

    @Query("DELETE FROM message")
    void clearTable();
}
