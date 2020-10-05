package com.stupidtree.hichat.ui.relation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.data.repository.ProfileRepository;
import com.stupidtree.hichat.data.repository.RelationRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.myprofile.ChangeInfoTrigger;

import java.util.Objects;

public class RelationViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：用户关系
    LiveData<DataState<UserRelation>> relationData;
    //Trigger：控制↑的刷新
    MutableLiveData<RelationQueryTrigger> relationQueryController = new MutableLiveData<>();
    //状态数据：更改签名的结果
    LiveData<DataState<String>> changeRemarkResult;
    //Trigger：控制更改签名请求的发送，其中携带了新昵称字符串
    MutableLiveData<ChangeInfoTrigger> changeRemarkController = new MutableLiveData<>();

    /**
     * 仓库区
     */
    RelationRepository relationRepository;
    LocalUserRepository localUserRepository;

    public RelationViewModel(){
        localUserRepository = LocalUserRepository.getInstance();
        relationRepository = RelationRepository.getInstance();
    }

    public LiveData<DataState<UserRelation>> getRelationData() {
        if(relationData==null){
            relationData = Transformations.switchMap(relationQueryController, input -> {
                if(input.isActioning()){
                    if(localUserRepository.isUserLoggedIn()){
                        return relationRepository.queryRelation(Objects.requireNonNull(localUserRepository.getLoggedInUserDirect().getToken()),
                                input.getFriendId());
                    }else{
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                  }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return relationData;
    }



    public LiveData<DataState<String>> getChangeRemarkResult() {
        if(changeRemarkResult==null){
            //也是一样的
            changeRemarkResult = Transformations.switchMap(changeRemarkController, input -> {
                if(input.isActioning()){
                    UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
                    if(userLocal.isValid()&&relationData.getValue().getData().getId()!=null){
                        System.out.println("friend id is"+relationData.getValue().getData().getId());
                        return relationRepository.changeRemark(Objects.requireNonNull(userLocal.getToken()),input.getValue(),relationData.getValue().getData().getId());
                    }else{
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>();
            });
        }
        return changeRemarkResult;
    }



    public void startFetchRelationData(String friendId){
        relationQueryController.setValue(RelationQueryTrigger.getActioning(friendId));
    }
}
