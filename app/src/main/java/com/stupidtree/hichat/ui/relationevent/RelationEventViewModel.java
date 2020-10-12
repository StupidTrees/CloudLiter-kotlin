package com.stupidtree.hichat.ui.relationevent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.RelationEvent;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.data.repository.RelationRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.Trigger;

import java.util.List;
import java.util.Objects;

public class RelationEventViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：列表数据
    private LiveData<DataState<List<RelationEvent>>> listData;
    //Trigger：控制↑的刷新
    private MutableLiveData<Trigger> listDataController = new MutableLiveData<>();


    //状态数据：同意好友请求
    private LiveData<DataState<?>> responseResult;
    private MutableLiveData<ResponseFriendTrigger> responseFriendTrigger = new MutableLiveData<>();

    //状态数据：标记已读的结果
    private LiveData<DataState<Object>> markReadResult;
    //Trigger：控制↑的进行
    private MutableLiveData<Trigger> markReadController = new MutableLiveData<>();

    /**
     * 仓库区
     */
    LocalUserRepository localUserRepository;
    RelationRepository relationRepository;


    public RelationEventViewModel(){
        localUserRepository = LocalUserRepository.getInstance();
        relationRepository = RelationRepository.getInstance();
    }

    public LiveData<DataState<List<RelationEvent>>> getListData() {
        if(listData==null){
            listData = Transformations.switchMap(listDataController, input -> {
                if(!input.isActioning()){
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
                }
                UserLocal userLocal = localUserRepository.getLoggedInUser();
                if(userLocal.isValid()){
                    return relationRepository.queryMine(Objects.requireNonNull(userLocal.getToken()));
                }else{
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                }
            });
        }
        return listData;
    }

    public LiveData<DataState<?>> getResponseResult() {
        if(responseResult==null){
            responseResult = Transformations.switchMap(responseFriendTrigger, input -> {
                if(input.isActioning()){
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if(userLocal.isValid()){
                        return relationRepository.responseFriendRequest(Objects.requireNonNull(userLocal.getToken()),input.getEventId(),input.getAction());
                    }else{
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return responseResult;
    }
    public LiveData<DataState<Object>> getMarkReadResult() {
        if(markReadResult==null){
            return Transformations.switchMap(markReadController, input -> {
                if(input.isActioning()){
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if(userLocal.isValid()){
                        return relationRepository.markRead(Objects.requireNonNull(userLocal.getToken()));
                    }else{
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }

                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return markReadResult;
    }
    /**
     * 开始标记已读
     */
    public void startMarkRead(){
        markReadController.setValue(Trigger.getActioning());
    }
    public void startRefresh(){
        listDataController.setValue(Trigger.getActioning());
    }

    @Nullable
    public String getLocalUserId(){
        return localUserRepository.getLoggedInUser().getId();
    }

    public void responseFriendRequest(@NonNull String eventId, RelationEvent.ACTION action){
        responseFriendTrigger.setValue(ResponseFriendTrigger.getActioning(eventId,action));
    }
}
