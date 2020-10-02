package com.stupidtree.hichat.ui.search;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserSearched;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.data.repository.UserRepository;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.List;

/**
 * 层次：ViewModel
 * "好友搜索"页面所绑定的ViewModel
 */
public class SearchViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：搜索结果表
    LiveData<DataState<List<UserSearched>>> searchListStateLiveData;
    //Trigger：控制↑的刷新
    MutableLiveData<SearchTrigger> searchTriggerLiveData = new MutableLiveData<>();


    /**
     * 仓库区
     */
    //用户仓库
    UserRepository userRepository;
    //本地用户仓库
    LocalUserRepository localUserRepository;

    public SearchViewModel(){
        userRepository = UserRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
    }


    @NonNull
    public LiveData<DataState<List<UserSearched>>> getSearchListStateLiveData(){
        if(searchListStateLiveData==null) {
            searchListStateLiveData = Transformations.switchMap(searchTriggerLiveData, input -> {
                UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
                if(userLocal.isValid()){
                    //通知用户仓库进行搜索，从中获取搜索结果
                    return userRepository.searchUser(input.getSearchText(), userLocal.getToken());
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
            });
        }
        return searchListStateLiveData;
    }


    /**
     * 进行搜哦
     * @param text 检索语句
     */
    public void beginSearch(String text){
        searchTriggerLiveData.setValue(SearchTrigger.getSearchInstance(text));
    }
}
