package com.stupidtree.cloudliter.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stupidtree.cloudliter.data.model.Conversation

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation  ORDER BY updatedAt DESC")
    fun getConversations(): LiveData<MutableList<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveConversations(data: MutableList<Conversation>)

    @Query("DELETE FROM conversation")
    fun clearTable()
}