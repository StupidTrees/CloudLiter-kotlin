package com.stupidtree.cloudliter.data.source.websource

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.ImageEntity
import com.stupidtree.cloudliter.data.source.websource.service.ImageService
import com.stupidtree.cloudliter.data.source.websource.service.LiveDataCallAdapter
import com.stupidtree.cloudliter.data.source.websource.service.codes
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.wordcloud.FaceEntity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ImageWebSource : BaseWebSource<ImageService>(Retrofit.Builder()
        .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://hita.store:3000").build()) {


    /**
     * 获取某图片的信息对象
     * @param token 令牌
     * @param imageId 图片id
     * @return 操作结果
     */
    fun getImageEntity(token: String, imageId: String): LiveData<DataState<ImageEntity>> {
        return Transformations.map(service.getImageEntity(token, imageId)) { input ->
            //Log.e("resp", input.toString())
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data!!)
                codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 获得所有人脸
     */
    fun getFaces(token: String): LiveData<DataState<List<FaceEntity>>> {
        return Transformations.map(service.getFaces(token)) { input ->
            //Log.e("resp", input.toString())
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data!!)
                codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 删除人脸
     */
    fun deleteFace(token: String,faceId:String): LiveData<DataState<String?>> {
        return Transformations.map(service.deleteFace(token,faceId)) { input ->
            //Log.e("resp", input.toString())
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(DataState.STATE.SUCCESS)
                codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }
    override fun getServiceClass(): Class<ImageService> {
        return ImageService::class.java
    }

    companion object {
        //单例模式
        var sInstance: ImageWebSource? = null
        fun getInstance(): ImageWebSource {
            if (sInstance == null){
                sInstance = ImageWebSource()
            }
            return sInstance!!
        }
    }

}