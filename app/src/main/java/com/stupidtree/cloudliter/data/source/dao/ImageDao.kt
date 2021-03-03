package com.stupidtree.cloudliter.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.Image
import java.sql.Timestamp

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveImageSync(data: Image)

    @Query("select * from image where id is :id")
    fun findImageById(id:String):LiveData<Image>

}