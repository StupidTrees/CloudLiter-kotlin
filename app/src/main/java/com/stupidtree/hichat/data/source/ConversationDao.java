package com.stupidtree.hichat.data.source;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;

import java.util.List;

@Dao
public interface ConversationDao {


    @Query("SELECT * FROM conversation  ORDER BY updatedAt DESC")
    LiveData<List<Conversation>> getConversations();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveConversations(List<Conversation> data);

    @Query("DELETE FROM conversation")
    void clearTable();
}
