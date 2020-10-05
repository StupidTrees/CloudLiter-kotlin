package com.stupidtree.hichat.ui.relation;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.data.repository.RelationRepository;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.Objects;

public class RelationViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：用户关系
    LiveData<DataState<UserRelation>> relationData;
    //Trigger：控制↑的刷新
    MutableLiveData<RelationQueryTrigger> relationQueryController = new MutableLiveData<>();

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

    public void startFetchRelationData(String friendId){
        relationQueryController.setValue(RelationQueryTrigger.getActioning(friendId));
    }
}
