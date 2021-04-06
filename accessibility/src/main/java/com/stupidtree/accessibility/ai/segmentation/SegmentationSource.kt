package com.stupidtree.accessibility.ai.segmentation


import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ai.aiboost.AiBoostInterpreter
import com.stupidtree.accessibility.ai.icon.Classification
import com.stupidtree.component.data.DataState
import jackmego.com.jieba_android.JiebaSegmenter
import org.ansj.splitWord.analysis.ToAnalysis

class SegmentationSource(private var application: Application) {


    fun cutSentence(sentence: String): LiveData<DataState<List<Token>>> {
        val result = MutableLiveData<DataState<List<Token>>>()
        Thread {
            try {
                val res = mutableListOf<Token>()
                if (useJieba) {
                    for ((i, t) in JiebaSegmenter.getJiebaSegmenterSingleton().getDividedString(sentence).withIndex()) {
                        val token = Token()
                        token.index = i
                        token.name = t
                        if (token.name.isNullOrBlank()) continue
                        res.add(token)
                    }
                } else {
                    for ((i,t) in ToAnalysis.parse(sentence).withIndex()) {
                        val token = Token()
                        token.name = t.name
                        token.tag = t.natureStr
                        token.index = i
                        if (token.name.isNullOrBlank()) continue
                        res.add(token)
                    }
                }

                result.postValue(DataState(res))
            } catch (e: Exception) {
                e.printStackTrace()
                result.postValue(DataState(DataState.STATE.FETCH_FAILED))
            }
        }.start()
        return result
    }


    companion object {

        const val useJieba = true

        @Volatile
        var instance: SegmentationSource? = null
        fun getInstance(application: Application): SegmentationSource {
            if (instance == null) {
                instance = SegmentationSource(application)
            }
            return instance!!
        }

        fun initSegmentation(application: Application) {
            if (useJieba) {
                JiebaSegmenter.init(application)
            } else {
                ToAnalysis.parse("test")
            }

        }

    }
}