package com.stupidtree.cloudliter.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.ImageEntity
import com.stupidtree.cloudliter.data.source.websource.ImageWebSource
import com.stupidtree.component.data.DataState
import com.stupidtree.cloudliter.ui.face.FaceEntity
import com.stupidtree.cloudliter.ui.face.permission.FaceWhiteListEntity
import com.stupidtree.cloudliter.ui.gallery.faces.FriendFaceEntity
import com.stupidtree.cloudliter.ui.gallery.scene.SceneEntity

class ImageRepository(application: Application) {
    //数据源1：网络类型数据，消息记录的网络数据源
    private val imageWebSource: ImageWebSource = ImageWebSource.getInstance()
    private val imageDao = AppDatabase.getDatabase(application).imageDao()

    /**
     * 获得图片信息
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

    /**
     * 获取所有人脸
     */
    fun getFacesOfUser(token: String): LiveData<DataState<List<FaceEntity>>> {
        return imageWebSource.getFaces(token)
    }

    /**
     * 删除人脸
     */
    fun deleteFace(token: String, faceId: String): LiveData<DataState<String?>> {
        return imageWebSource.deleteFace(token, faceId)
    }


    /**
     * 获取某一类型的所有图片
     */
    fun getImagesOfClass(token: String, classKey: String,pageSize:Int,pageNum:Int): LiveData<DataState<List<String>>> {
        return imageWebSource.getImagesOfClass(token, classKey,pageSize,pageNum)
    }


    /**
     * 获取包含某一好友的所有图片
     */
    fun getImagesOfFriend(token: String, friendId: String,pageSize:Int,pageNum:Int): LiveData<DataState<List<String>>> {
        return imageWebSource.getImagesOfFriend(token,friendId,pageSize,pageNum)
    }
    /**
     * 获取所有类型
     */
    fun getAllScenes(token:String):LiveData<DataState<List<SceneEntity>>>{
        return imageWebSource.getALlClasses(token)
    }

    /**
     * 获取所有好友人脸
     */
    fun getFriendFaces(token:String):LiveData<DataState<List<FriendFaceEntity>>>{
        return imageWebSource.getFriendFaces(token)
    }


    /**
     * 获取白名单
     */
    fun getWhiteList(token: String):LiveData<DataState<List<FaceWhiteListEntity>>>{
        return imageWebSource.getFaceWhiteList(token)
    }

    /**
     * 添加到白名单
     */
    fun addToWhiteList(token: String,ids:List<String>):LiveData<DataState<Any>>{
        return imageWebSource.addToWhiteList(token,ids)
    }

    /**
     * 从白名单中移除
     */
    fun deleteFromWhiteList(token: String,friendId: String):LiveData<DataState<Any>>{
        return imageWebSource.removeFromWhiteList(token,friendId)
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