package com.stupidtree.accessibility.ai


import android.app.Application
import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ai.aiboost.AiBoostInterpreter
import com.stupidtree.accessibility.ai.ImageUtils.convertBitmapToByteBuffer
import com.stupidtree.accessibility.ai.ImageUtils.getFloat
import com.stupidtree.component.data.DataState
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.locks.ReentrantLock

class IconClassifierSource(private var application: Application) {

    var aiboost: AiBoostInterpreter? = null
    private val options = AiBoostInterpreter.Options()
    var labels: MutableList<String>? = null


    fun classifyIcon(bitmap: Bitmap): LiveData<DataState<List<Classification>>> {
        val result = MutableLiveData<DataState<List<Classification>>>()
        Thread {
            try {
                val res = classifyFrame(bitmap)
                result.postValue(DataState(res))
            } catch (e: Exception) {
                e.printStackTrace()
                result.postValue(DataState(DataState.STATE.FETCH_FAILED))
            }
        }.start()
        return result
    }


    private val lock = ReentrantLock()
    private fun classifyFrame(bitmap: Bitmap): List<Classification> {
        lock.lock()
        if(labels==null){
            initModel()
        }
        val scaledBmp = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE_X, IMAGE_SIZE_Y, false)
        convertBitmapToByteBuffer(aiboost?.getInputTensor(0), scaledBmp)
        val output = aiboost?.getOutputTensor(0)
        output?.rewind()
        val result = ByteArray(output?.remaining() ?: 0)
        aiboost?.runWithOutInputOutput()
        output?.get(result, 0, result.size)
        lock.unlock()
        val r = mutableListOf<Classification>()
        for (i in 0 until NUM_OF_LABELS) {
            val classification = Classification()
            classification.confidence = getFloat(result, i * 4)
            classification.name = labels?.get(i)
            r.add(classification)
        }

        return r
    }


    private fun initModel() {
        labels = mutableListOf()
        val reader = BufferedReader(InputStreamReader(application.assets.open(LABELS_PATH)))
        reader.forEachLine {
            labels?.add(it)
        }
        reader.close()
        val input = application.assets.open(MODEL_PATH)
        val length = input.available()
        val buffer = ByteArray(length)
        input.read(buffer)
        val modelBuf = ByteBuffer.allocateDirect(length)
        modelBuf.order(ByteOrder.nativeOrder())
        modelBuf.put(buffer)
        val inputShape = arrayOf(intArrayOf(DIM_BATCH_SIZE, IMAGE_SIZE_Y, IMAGE_SIZE_X, DIM_PIXEL_SIZE))
        options.setNumThreads(3)
        options.setDeviceType(AiBoostInterpreter.Device.QUALCOMM_DSP)
        options.setQComPowerLevel(AiBoostInterpreter.QCOMPowerLEVEL.QCOM_TURBO)
        options.setNativeLibPath(application.applicationInfo.nativeLibraryDir)
        aiboost = AiBoostInterpreter(modelBuf, inputShape, options)
    }


    companion object {
        //单例模式
        @Volatile
        var instance: IconClassifierSource? = null
        fun getInstance(application: Application): IconClassifierSource {
            if (instance == null) {
                instance = IconClassifierSource(application)
            }
            return instance!!
        }

        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 1
        const val IMAGE_SIZE_X = 100
        const val IMAGE_SIZE_Y = 100
        const val NUM_OF_LABELS = 72
        const val MODEL_PATH = "ic1.tflite"
        const val LABELS_PATH = "icon_classes.txt"
    }
}