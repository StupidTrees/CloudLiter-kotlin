package com.stupidtree.cloudliter.data.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.model.Image
import com.stupidtree.cloudliter.data.source.ai.yolo.Classifier
import com.stupidtree.cloudliter.data.source.ai.yolo.YOLOSource
import com.stupidtree.cloudliter.data.source.websource.AiWebSource
import com.stupidtree.cloudliter.data.source.websource.ImageWebSource
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.imagedetect.BitmapRequestBody
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import java.io.ByteArrayOutputStream

class ImageRepository(application: Application) {
    //数据源1：网络类型数据，消息记录的网络数据源
    var imageWebSource: ImageWebSource = ImageWebSource.getInstance()


    /**
     * 进行图片分类（上传图片文件形式）
     *
     * @param token    令牌
     * @param imageId 图片id
     * @return 返回结果
     */
    fun getImageInfo(token: String, imageId:String): LiveData<DataState<Image>> {
        return imageWebSource.getImageEntity(token,imageId)
    }

    companion object {
        //单例模式
        @JvmStatic
        @Volatile
        private var instance: ImageRepository? = null
        fun getInstance(application: Application): ImageRepository {
            if (instance == null) {
                instance = ImageRepository(application)
            }
            return instance!!
        }
    }
}