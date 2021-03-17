package com.stupidtree.cloudliter.data.source.ai.detect

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ai.aiboost.AiBoostInterpreter
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier
import com.stupidtree.cloudliter.ui.base.DataState
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ObjectDetectSource(application: Application) {
    private var aiBoost: AiBoostInterpreter?

    init {
        val input: InputStream = application.assets.open("yolov4-416-fp32.tflite")
        val length: Int = input.available()
        val buffer = ByteArray(length)
        input.read(buffer)
        val modelbuf: ByteBuffer = ByteBuffer.allocateDirect(length)
        modelbuf.order(ByteOrder.nativeOrder())
        modelbuf.put(buffer)
        val DIM_BATCH_SIZE = 1
        val DIM_PIXEL_SIZE = 3
        val BYTE_NUM_PER_CHANNEL = 1
        val IMAGE_SIZE_X = 416
        val IMAGE_SIZE_Y = 416
        val input_shapes = arrayOf(intArrayOf(DIM_BATCH_SIZE, IMAGE_SIZE_Y, IMAGE_SIZE_X, DIM_PIXEL_SIZE))
        val options = AiBoostInterpreter.Options()
        options.setNumThreads(1)
        options.setDeviceType(AiBoostInterpreter.Device.QUALCOMM_DSP)
        options.setQComPowerLevel(AiBoostInterpreter.QCOMPowerLEVEL.QCOM_TURBO)
        aiBoost = try {
            AiBoostInterpreter(modelbuf, input_shapes, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun detectImage(bitmap: Bitmap): LiveData<DataState<List<Classifier.Recognition>>> {
        val result = MutableLiveData<DataState<List<Classifier.Recognition>>>()
        Thread {
            //val imgData = aiBoost.getInputTensor(0)
            //imgData.rewind()
//            bitmap.getPixels(intValues, 0, attr.bitmap.getWidth(), 0, 0, attr.bitmap.getWidth(), attr.bitmap.getHeight())
//            var pixel = 0
//            for (i in 0 until IMAGE_SIZE_X) {
//                for (j in 0 until IMAGE_SIZE_Y) {
//                    val `val`: Int = intValues.get(pixel++)
//                    imgData.put((`val` shr 16 and 0xFF).toByte())
//                    imgData.put((`val` shr 8 and 0xFF).toByte())
//                    imgData.put((`val` and 0xFF).toByte())
//                }
//            }
        }.start()
        return result
    }


    companion object {
        const val IMAGE_SIZE = 416f
        //单例模式
        @Volatile
        var instance: ObjectDetectSource? = null
        fun getInstance(application: Application): ObjectDetectSource {
            if (instance == null) {
                instance = ObjectDetectSource(application)
            }
            return instance!!
        }
    }
}