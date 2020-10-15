package com.stupidtree.hichat.data.source;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.stupidtree.hichat.data.model.ChatMessage;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM message WHERE conversationId IS :conversationId AND id < :fromId   ORDER BY id DESC LIMIT :pageSize ")
    LiveData<List<ChatMessage>> getMessages(String conversationId,int pageSize,long fromId);

    @Query("SELECT * FROM message WHERE conversationId IS :conversationId  ORDER BY id DESC LIMIT :pageSize")
    LiveData<List<ChatMessage>> getMessages(String conversationId,int pageSize);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveMessage(List<ChatMessage> data);
}
