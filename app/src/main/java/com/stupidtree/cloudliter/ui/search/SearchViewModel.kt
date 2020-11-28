package com.stupidtree.cloudliter.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.UserSearched
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.ProfileRepository
import com.stupidtree.cloudliter.data.repository.UserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.Trigger
import com.stupidtree.cloudliter.ui.search.SearchTrigger.Companion.getSearchInstance

/**
 * 层次：ViewModel
 * "好友搜索"页面所绑定的ViewModel
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */
    //用户仓库
    var userRepository: UserRepository = UserRepository.getInstance(application)!!

    //词云仓库
    var profileRepository: ProfileRepository = ProfileRepository.getInstance(application)

    //本地用户仓库
    var localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)


    /**
     * 数据区
     */
    //数据本体：搜索结果表
    var searchListStateLiveData: LiveData<DataState<List<UserSearched>?>>? = null
        get() {
            if (field == null) {
                searchListStateLiveData = Transformations.switchMap(searchTriggerLiveData) { input: SearchTrigger ->
                    val userLocal = localUserRepository.getLoggedInUser()
                    if (userLocal.isValid) {
                        //通知用户仓库进行搜索，从中获取搜索结果
                        return@switchMap userRepository.searchUser(input.searchText.toString(), userLocal.token.toString(), searchMode.value!!)
                    }
                    MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
                }
            }
            return field!!
        }

    //数据本体：我的词云显示
    var myWordCloudLiveData: LiveData<DataState<HashMap<String,Float?>?>>? = null
        get() {
            if(field==null){
                myWordCloudLiveData = Transformations.switchMap(myWordCloudController){
                    val userLocal = localUserRepository.getLoggedInUser()
                    if (userLocal.isValid) {
                        //通知用户仓库进行搜索，从中获取搜索结果
                        return@switchMap profileRepository.getUserWordCloud(userLocal.token!!,userLocal.id!!)
                    }
                    return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
                }
            }
            return field!!;
        }

    //Trigger：控制↑的刷新
    var searchTriggerLiveData = MutableLiveData<SearchTrigger>()
    var myWordCloudController = MutableLiveData<Trigger>()

    //UI数据：搜索模式
    var searchMode = MutableLiveData(false)
    /**
     * 进行搜哦
     * @param text 检索语句
     */
    fun beginSearch(text: String?) {
        searchTriggerLiveData.value = getSearchInstance(text)
    }


    fun beginRefresh(){
        myWordCloudController.value = Trigger.actioning
    }

    fun switchSearchMode(){
        searchMode.value = !searchMode.value!!
    }

    fun setSearchMode(searchWordCloud:Boolean){
        searchMode.value = searchWordCloud
    }
}