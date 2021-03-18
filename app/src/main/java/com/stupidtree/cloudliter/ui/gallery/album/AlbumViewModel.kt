package com.stupidtree.cloudliter.ui.gallery.album

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.repository.ImageRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState

class AlbumViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 仓库区
     */
    private val imageRepository = ImageRepository.getInstance(application)
    private val localUserRepository = LocalUserRepository.getInstance(application)

    /**
     * LiveData区
     */
    private val queryLiveData = MutableLiveData<AlbumQuery>()
    private var currentPage = 0

    val imagesLiveData = Transformations.switchMap(queryLiveData) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap Transformations.map(imageRepository.getImagesOfClass(userLocal.token!!, it.key, it.pageSize, it.pageNum)) { dt ->
                if(it.nextPage && dt.state== DataState.STATE.SUCCESS && dt.data?.isNotEmpty()==true){
                    currentPage++
                }
                return@map dt.setListAction(if (it.nextPage) DataState.LIST_ACTION.APPEND else DataState.LIST_ACTION.REPLACE_ALL)
            }
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }


    /**
     * 从头开始加载
     */
    fun refreshAll(key: String) {
        currentPage = 0
        queryLiveData.value = AlbumQuery(key = key, pageNum = 0, pageSize = PAGE_SIZE, nextPage = false)
    }

    /**
     * 加载下一页
     */
    fun nextPage() {
        val old = queryLiveData.value
        old?.let {
            queryLiveData.value = AlbumQuery(key = it.key, pageNum = currentPage+1, pageSize = PAGE_SIZE, nextPage = true)
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}