package com.stupidtree.hichat.ui.group.pick;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.RelationGroup;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.GroupRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.Trigger;

import java.util.List;

public class PickGroupViewModel extends AndroidViewModel {

    /**
     * 数据区
     */
    LiveData<DataState<List<RelationGroup>>> listData;
    MutableLiveData<Trigger> listController = new MutableLiveData<>();

    /**
     * 仓库区
     */
    GroupRepository groupRepository;
    LocalUserRepository localUserRepository;


    public PickGroupViewModel(@NonNull Application application) {
        super(application);
        groupRepository = GroupRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
    }

    public LiveData<DataState<List<RelationGroup>>> getListData() {
        if(listData==null){
            listData = Transformations.switchMap(listController, input -> {
                UserLocal userLocal = localUserRepository.getLoggedInUser();
                if(userLocal.isValid()){
                    return groupRepository.queryMyGroups(userLocal.getToken());
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
            });
        }
        return listData;
    }

    /**
     * 开始刷新
     */

    public void startRefresh(){
        listController.setValue(Trigger.getActioning());
    }
}
