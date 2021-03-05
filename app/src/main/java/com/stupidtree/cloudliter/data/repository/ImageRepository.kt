package com.stupidtree.cloudliter.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.ImageEntity
import com.stupidtree.cloudliter.data.source.websource.ImageWebSource
import com.stupidtree.cloudliter.ui.base.DataState

class ImageRepository(application: Application) {
    //数据源1：网络类型数据，消息记录的网络数据源
    private val imageWebSource: ImageWebSource = ImageWebSource.getInstance()
    val imageDao = AppDatabase.getDatabase(application).imageDao()

    /**
     * 进行图片分类（上传图片文件形式）
     *
     * @param token    令牌
     * @param imageId 图片id
     * @return 返回结果
     */
    fun getImageInfo(token: String, imageId: String): LiveData<DataState<ImageEntity>> {
        val res = MediatorLiveData<DataState<ImageEntity>>()
        val cache = imageDao.findImageById(imageId)
        res.addSource(cache) {
            it?.let {
                res.value = DataState(it)
            }
        }
        res.addSource(imageWebSource.getImageEntity(token, imageId)) {
            it.data?.let { image ->
                Thread {
                    imageDao.saveImageSync(image)
                }.start()
            }
        }
        return res
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