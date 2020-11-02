package com.stupidtree.cloudliter.ui.group;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.cloudliter.data.model.RelationGroup;
import com.stupidtree.cloudliter.data.model.UserLocal;
import com.stupidtree.cloudliter.data.repository.GroupRepository;
import com.stupidtree.cloudliter.data.repository.LocalUserRepository;
import com.stupidtree.cloudliter.ui.base.DataState;
import com.stupidtree.cloudliter.ui.base.Trigger;
import com.stupidtree.cloudliter.ui.myprofile.ChangeInfoTrigger;

import java.util.List;
import java.util.Objects;

public class GroupEditorViewModel extends ViewModel {
    /**
     * 数据区
     */
    //数据本体
    LiveData<DataState<List<RelationGroup>>> listData;
    //Trigger:控制↑的刷新
    MutableLiveData<Trigger> listDataController = new MutableLiveData<>();

    //状态数据：添加分组的结果
    LiveData<DataState<String>> addGroupResult;
    //Trigger：控制添加请求的发送
    MutableLiveData<ChangeInfoTrigger> addGroupController = new MutableLiveData<>();

    //状态数据：删除分组的结果
    LiveData<DataState<String>> deleteGroupResult;
    //Trigger：控制删除请求的发送
    MutableLiveData<ChangeInfoTrigger> deleteGroupController = new MutableLiveData<>();
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

    public LiveData<DataState<String>> getAddGroupResult() {
        if(addGroupResult==null){
            //也是一样的
            addGroupResult = Transformations.switchMap(addGroupController, input -> {
                if(input.isActioning()){
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if(userLocal.isValid()){
                        //System.out.println("viewmodel stage:func: getAddGroupResult return is "+groupRepository.addMyGroups(userLocal.getToken(),input.getValue()));
                        return groupRepository.addMyGroups(Objects.requireNonNull(userLocal.getToken()),input.getValue());
                    }else{
                        //System.out.println("viewmodel stage: no log in,func: getAddGroupResult return is "+groupRepository.addMyGroups(userLocal.getToken(),input.getValue()));
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>();
            });
        }
        return addGroupResult;
    }

    public LiveData<DataState<String>> getDeleteGroupResult() {
        if(deleteGroupResult==null){
            //也是一样的
            deleteGroupResult = Transformations.switchMap(deleteGroupController, input -> {
                if(input.isActioning()){
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if(userLocal.isValid()){
                        //System.out.println("viewmodel stage:func: getDeleteGroupResult return is "+groupRepository.deleteMyGroups(userLocal.getToken(),input.getValue()));
                        return groupRepository.deleteMyGroups(Objects.requireNonNull(userLocal.getToken()),input.getValue());
                    }else{
                        //System.out.println("viewmodel stage: no log in,func: getDeleteGroupResult return is "+groupRepository.deleteMyGroups(userLocal.getToken(),input.getValue()));
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>();
            });
        }
        return deleteGroupResult;
    }

    /**
     * 发起添加好友分组请求
     * @param group 新组名
     */
    public void startAddGroup(String group){
        System.out.println("viewmodel stage:func: startAddGroup group is "+group);
        addGroupController.setValue(ChangeInfoTrigger.getActioning(group));
    }

    /**
     * 发起删除好友分组请求
     * @param group 新组名
     */
    public void startDeleteGroup(String group){
        System.out.println("viewmodel stage:func: startDeleteGroup group is "+group);
        deleteGroupController.setValue(ChangeInfoTrigger.getActioning(group));
    }

    /**
     * 开始刷新页面
     */
    public void startRefresh(){
        listDataController.setValue(Trigger.getActioning());
    }
}
