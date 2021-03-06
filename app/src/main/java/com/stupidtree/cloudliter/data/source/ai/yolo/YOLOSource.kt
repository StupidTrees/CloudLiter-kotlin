package com.stupidtree.cloudliter.data.source.ai.yolo

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stupidtree.cloudliter.ui.base.DataState

class YOLOSource(private var application: Application) {
    private var classifier: Classifier? = null

    fun detectImage(bitmap: Bitmap): LiveData<DataState<List<Classifier.Recognition>>> {
        val result = MutableLiveData<DataState<List<Classifier.Recognition>>>()
        Thread {
            try {
                if (classifier == null) init()
                val cropped = Utils.processBitmap(bitmap, 416)
                val r = classifier!!.recognizeImage(cropped)
                result.postValue(DataState(r))
            } catch (e: Exception) {
                e.printStackTrace()
                result.postValue(DataState(DataState.STATE.FETCH_FAILED))
            }
        }.start()
        return result
    }

    @WorkerThread
    private fun init() {
       classifier = YoloV4Classifier.create(
                application.applicationContext.assets,
                "yolov4-416-fp32.tflite",
                "file:///android_asset/coco.txt",
                false)
    }


    companion object {
        //单例模式
        @Volatile
        var instance: YOLOSource? = null
        fun getInstance(application: Application): YOLOSource {
            if (instance == null) {
                instance = YOLOSource(application)
            }
            return instance!!
        }
    }
}