package com.stupidtree.visual.face.expression

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aiunit.vision.common.ConnectionCallback
import com.aiunit.vision.face.FaceInputSlot
import com.aiunit.vision.face.FaceOutputSlot
import com.coloros.ocs.ai.cv.CVUnit
import com.coloros.ocs.ai.cv.CVUnitClient
import com.stupidtree.component.data.DataState


class FacialExpressionSource(private var application: Application) {
    var mCVClient: CVUnitClient = CVUnit.getFaceFerClient(application)
            .addOnConnectionSucceedListener { Log.i("AIUnit", " authorize connect: onConnectionSucceed") }
            .addOnConnectionFailedListener {
                Log.e("AIUnit", " authorize connect: onFailure: " + it.errorCode)
            }

    init {
        mCVClient.initService(application, object : ConnectionCallback {
            override fun onServiceConnect() {
                Log.i("AIUnit", "initService: onServiceConnect")
                val startCode = mCVClient.start()
            }

            override fun onServiceDisconnect() {
                Log.e("AIUnit", "initService: onServiceDisconnect: ")
            }
        })
    }

    fun getExpression(bitmap: Bitmap, ranges: List<ExpressionInput>): LiveData<DataState<List<ExpressionResult>>> {
        val res = MutableLiveData<DataState<List<ExpressionResult>>>()
        val xF = bitmap.width.toFloat() / 416f
        val yF = bitmap.height.toFloat() / 416f
        Thread {
            try {
                mCVClient.start()
                val resList = mutableListOf<ExpressionResult>()
                for (range in ranges) {
                    val rec = range.range
                    val cropped = Bitmap.createBitmap(bitmap, (xF * rec.left).toInt(), (yF * rec.top).toInt(), (rec.width() * xF).toInt(), (rec.height() * yF).toInt())
                    val inputSlot = mCVClient.createInputSlot() as FaceInputSlot
                    inputSlot.targetBitmap = cropped
                    val outputSlot = mCVClient.createOutputSlot() as FaceOutputSlot
                    mCVClient.process(inputSlot, outputSlot)
                    val faceList = outputSlot.faceList.faceResultList
                    if (faceList.size > 0) {
                        val res = ExpressionResult()
                        res.expression = faceList[0].expression
                        res.faceId = range.id
                        resList.add(res)
                    }
                }
                mCVClient.stop()
                res.postValue(DataState(resList))
            } catch (e: Exception) {
                res.postValue(DataState(DataState.STATE.FETCH_FAILED))
            }
        }.start()
        return res
    }

    companion object {
        //单例模式
        @Volatile
        var instance: FacialExpressionSource? = null
        fun getInstance(application: Application): FacialExpressionSource {
            if (instance == null) {
                instance = FacialExpressionSource(application)
            }
            return instance!!
        }
    }
}