package com.stupidtree.cloudliter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "face_result",primaryKeys = ["imageId","rectId"])
class FaceResult {
    var imageId:String=""
    var rectId:String=""

    var userId:String?=""
    var userName:String?=""
    var confidence:Float = 0f
}