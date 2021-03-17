package com.stupidtree.cloudliter.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stupidtree.cloudliter.data.model.ImageEntity

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveImageSync(data: ImageEntity)

    @Query("select * from image where id is :id")
    fun findImageById(id:String):LiveData<ImageEntity?>

    @Query("update image set scene = :scene where id is :id")
    fun updateSceneSync(id:String,scene:String)

    @Query("select scene from image where id is :id")
    fun getSceneBtId(id:String):LiveData<String?>
}