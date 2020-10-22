package com.stupidtree.hichat.ui.group;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.RelationGroup;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.GroupRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.Trigger;

import java.util.List;

public class GroupEditorViewModel extends ViewModel {
    /**
     * 数据区
     */
    //数据本体
    LiveData<DataState<List<RelationGroup>>> listData;
    //Trigger:控制↑的刷新
    MutableLiveData<Trigger> listDataController = new MutableLiveData<>();
    /**
     * 仓库区
     */
    //好友分组
    GroupRepository groupRepository;
    //本地用户
    LocalUserRepository localUserRepository;

    public GroupEditorViewModel() {
        groupRepository = GroupRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
    }

    public LiveData<DataState<List<RelationGroup>>> getListData() {
        if (listData == null) {
            listData = Transformations.switchMap(listDataController, input -> {
                if (input.isActioning()) {
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if (userLocal.isValid()) {
                        return groupRepository.queryMyGroups(userLocal.getToken());
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN))
                                ;
                    }

                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return listData;
    }


    /**
     * 开始刷新页面
     */
    public void startRefresh(){
        listDataController.setValue(Trigger.getActioning());
    }
}
