package com.stupidtree.cloudliter.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.stupidtree.cloudliter.data.model.Conversation

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation  ORDER BY updatedAt DESC")
    fun getConversations(): LiveData<MutableList<Conversation>>

    @Query("SELECT * FROM conversation WHERE friendId IS :friendId")
    fun getConversationAt(friendId:String):LiveData<Conversation?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveConversations(data: MutableList<Conversation>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveConversation(data: Conversation)
    @Delete
    fun deleteConversations(data:List<Conversation>)

    @Query("DELETE FROM conversation")
    fun clearTable()
}