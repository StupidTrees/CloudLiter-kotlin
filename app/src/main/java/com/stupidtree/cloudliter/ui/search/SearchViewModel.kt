package com.stupidtree.cloudliter.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.UserSearched
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.UserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.search.SearchTrigger.Companion.getSearchInstance

/**
 * 层次：ViewModel
 * "好友搜索"页面所绑定的ViewModel
 */
class SearchViewModel(application: Application?) : AndroidViewModel(application!!) {
    /**
     * 数据区
     */
    //数据本体：搜索结果表
    var searchListStateLiveData: LiveData<DataState<List<UserSearched>>>? = null
        get() {
            if (field == null) {
                searchListStateLiveData = Transformations.switchMap(searchTriggerLiveData) { input: SearchTrigger ->
                    val userLocal = localUserRepository.loggedInUser
                    if (userLocal.isValid) {
                        //通知用户仓库进行搜索，从中获取搜索结果
                        return@switchMap userRepository.searchUser(input.searchText, userLocal.token)
                    }
                    MutableLiveData(DataState<List<UserSearched>>(DataState.STATE.NOT_LOGGED_IN))
                }
            }
            return field!!
        }

    //Trigger：控制↑的刷新
    var searchTriggerLiveData = MutableLiveData<SearchTrigger>()

    /**
     * 仓库区
     */
    //用户仓库
    var userRepository: UserRepository = UserRepository.getInstance(application)

    //本地用户仓库
    var localUserRepository: LocalUserRepository = LocalUserRepository.getInstance()

    /**
     * 进行搜哦
     * @param text 检索语句
     */
    fun beginSearch(text: String?) {
        searchTriggerLiveData.value = getSearchInstance(text)
    }

}