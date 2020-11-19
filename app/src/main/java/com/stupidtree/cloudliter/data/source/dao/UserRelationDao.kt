package com.stupidtree.cloudliter.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.UserRelation
import java.sql.Timestamp

@Dao
interface UserRelationDao {
    @Query("SELECT * FROM relation ")
    fun getFriends(): LiveData<List<UserRelation>?>

    @Query("SELECT * FROM relation WHERE friendId IS :friendId")
    fun queryRelation(friendId:String):LiveData<UserRelation?>

    @Query("DELETE FROM relation WHERE friendId IS :friendId")
    fun deleteRelation(friendId:String)

    @Delete
    fun deleteRelations(friendIds:List<UserRelation>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRelations(data: List<UserRelation>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRelation(data:UserRelation)


    @Query("DELETE FROM relation")
    fun clearTable()

}