package com.stupidtree.cloudliter.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stupidtree.cloudliter.data.model.FaceResult
import com.stupidtree.cloudliter.data.model.ImageEntity

@Dao
interface FaceResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveFaceResultsSync(data: List<FaceResult>)

    @Query("select * from face_result where imageId is :imageId")
    fun findFaceResults(imageId:String):LiveData<List<FaceResult>>

}