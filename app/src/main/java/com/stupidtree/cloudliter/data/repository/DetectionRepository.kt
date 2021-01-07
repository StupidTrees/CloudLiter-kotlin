package com.stupidtree.cloudliter.data.repository

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier
import com.stupidtree.cloudliter.data.source.ai.yolo.YOLOSource
import com.stupidtree.cloudliter.data.source.ai.yolo.YoloV4Classifier
import com.stupidtree.cloudliter.ui.base.DataState

class DetectionRepository(application: Application) {
    val yoloSource:YOLOSource = YOLOSource.getInstance(application)

    fun detectImage(bitmap: Bitmap): LiveData<DataState<List<Classifier.Recognition>>> {
        return yoloSource.detectImage(bitmap)
    }

    companion object {
        //单例模式
        @JvmStatic
        @Volatile
        private var instance: DetectionRepository? = null
        fun getInstance(application: Application): DetectionRepository {
            if (instance == null) {
                instance = DetectionRepository(application)
            }
            return instance!!
        }

    }

}