package com.stupidtree.hichat.ui.profile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.RelationGroup;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.data.repository.GroupRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.data.repository.ProfileRepository;
import com.stupidtree.hichat.data.repository.RelationRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.StringTrigger;
import com.stupidtree.hichat.ui.myprofile.ChangeInfoTrigger;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 层次：ViewModel
 * 其他用户（好友、搜索结果等）的资料页面绑定的ViewModel
 */
public class ProfileViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：用户资料
    LiveData<DataState<UserProfile>> profileLiveData;
    //数据本体：我和这个用户的好友关系
    LiveData<DataState<UserRelation>> relationLiveData;
    //数据本体：用户词云
    LiveData<DataState<HashMap<String,Float>>> wordCloudLiveData;
    //Trigger：控制↑三个的刷新
    MutableLiveData<StringTrigger> profileController = new MutableLiveData<>();

    //状态数据：添加好友的结果
    LiveData<DataState<?>> makeFriendsResult;
    //Trigger：控制添加好友的请求
    MutableLiveData<StringTrigger> makeFriendsController = new MutableLiveData<>();

    //状态数据：更改签名的结果
    LiveData<DataState<String>> changeRemarkResult;
    //Trigger：控制更改签名请求的发送，其中携带了新昵称字符串
    MutableLiveData<ChangeInfoTrigger> changeRemarkController = new MutableLiveData<>();

    //状态数据：删除好友的结果
    LiveData<DataState<?>> deleteFriendResult;
    //Trigger：控制删除好友
    MutableLiveData<StringTrigger> deleteFriendController = new MutableLiveData<>();

    //状态数据：分配好友分组的结果
    LiveData<DataState<?>> assignGroupResult;
    //Trigger:控制分配好友分组
    MutableLiveData<StringTrigger> assignGroupController = new MutableLiveData<>();

    
    /**
     * 仓库区
     */
    //用户资料仓库
    private final ProfileRepository repository;
    //用户关系仓库
    private final RelationRepository relationRepository;
    //本地用户仓库
    private final LocalUserRepository localUserRepository;
    //好友分组仓库
    private final GroupRepository groupRepository;

    public ProfileViewModel() {
        repository = ProfileRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
        relationRepository = RelationRepository.getInstance();
        groupRepository = GroupRepository.getInstance();
    }

    public LiveData<DataState<UserProfile>> getUserProfileLiveData() {
        if (profileLiveData == null) {
            profileLiveData = Transformations.switchMap(profileController, input -> {
                UserLocal user = localUserRepository.getLoggedInUser();
                if (input.isActioning()) {
                    if (user.isValid()) {
                        //从用户资料仓库中拉取数据
                        return repository.getUserProfile(input.getData(), Objects.requireNonNull(user.getToken()));
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>();
            });
        }
        return profileLiveData;
    }

    public LiveData<DataState<UserRelation>> getRelationLiveData() {
        if (relationLiveData == null) {
            relationLiveData = Transformations.switchMap(profileController, input -> {
                UserLocal user = localUserRepository.getLoggedInUser();
                if (input.isActioning()) {
                    if (user.isValid()) {

                        //如果就是自己
                        if (Objects.equals(user.getId(), input.getData())) {
                            return new MutableLiveData<>(new DataState<>(DataState.STATE.SPECIAL));
                        } else {
                            //通知用户资料仓库进行好友判别
                            return relationRepository.queryRelation(Objects.requireNonNull(user.getToken()), input.getData());
                        }
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return relationLiveData;
    }

    public LiveData<DataState<HashMap<String, Float>>> getWordCloudLiveData() {
        if(wordCloudLiveData==null){
            wordCloudLiveData = Transformations.switchMap(profileController, input -> {
                UserLocal user = localUserRepository.getLoggedInUser();
                if (input.isActioning()) {
                    if (user.isValid()) {
                        return repository.getUserWordCloud(user.getToken(),input.getData());
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return wordCloudLiveData;
    }

    public LiveData<DataState<?>> getMakeFriendsResult() {
        if (makeFriendsResult == null) {
            makeFriendsResult = Transformations.switchMap(makeFriendsController, input -> {
                UserLocal user = localUserRepository.getLoggedInUser();
                if (input.isActioning()) {
                    if (user.isValid()) {
                        //也是通过这个仓库进行好友建立
                        return relationRepository.sendFriendRequest(Objects.requireNonNull(user.getToken()), input.getData());
                        // return relationRepository.makeFriends(Objects.requireNonNull(user.getToken()),input.getData());
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>();
            });
        }
        return makeFriendsResult;
    }
    public LiveData<DataState<String>> getChangeRemarkResult() {
        if (changeRemarkResult == null) {
            //也是一样的
            changeRemarkResult = Transformations.switchMap(changeRemarkController, input -> {
                if (input.isActioning()) {
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if (userLocal.isValid() && relationLiveData.getValue() != null) {
                       // System.out.println("friend id is" + relationLiveData.getValue().getData().getFriendId());
                        return relationRepository.changeRemark(Objects.requireNonNull(userLocal.getToken()), input.getValue(), relationLiveData.getValue().getData().getFriendId());
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>();
            });
        }
        return changeRemarkResult;
    }

    public LiveData<DataState<?>> getDeleteFriendResult() {
        if (deleteFriendResult == null) {
            deleteFriendResult = Transformations.switchMap(deleteFriendController, input -> {
                if (input.isActioning()) {
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if (userLocal.isValid()) {
                        return relationRepository.deleteFriend(Objects.requireNonNull(userLocal.getToken()), input.getData());
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return deleteFriendResult;
    }

    public LiveData<DataState<?>> getAssignGroupResult() {
        if(assignGroupResult==null){
            assignGroupResult = Transformations.switchMap(assignGroupController, input -> {
                if (input.isActioning()) {
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if (userLocal.isValid() && getUserId()!=null) {
                        return groupRepository.assignGroup(Objects.requireNonNull(userLocal.getToken()),getUserId(),input.getData());
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return assignGroupResult;
    }

    /**
     * 开始页面刷新
     *
     * @param id 这个页面是谁的资料
     */
    public void startRefresh(String id) {
        profileController.setValue(StringTrigger.getActioning(id));
    }

    /**
     * 开始建立朋友关系
     *
     * @param id 这个页面是谁的
     */
    public void startMakingFriends(String id) {
        makeFriendsController.setValue(StringTrigger.getActioning(id));
    }

    /**
     * 开始删除好友
     *
     * @param id 好友id
     */
    public void startDeletingFriend(String id) {
        deleteFriendController.setValue(StringTrigger.getActioning(id));
    }


    /**
     * 获取该用户id
     *
     * @return
     */
    @Nullable
    public String getUserId() {
        if (profileLiveData.getValue() != null) {
            return profileLiveData.getValue().getData().getId();
        }
        return null;
    }



    /**
     * 请求更换备注
     *
     * @param newRemark 备注
     */
    public void startChangeRemark(String newRemark) {
        if (relationLiveData.getValue() != null) {
            changeRemarkController.setValue(ChangeInfoTrigger.getActioning(newRemark));
        }

    }

    /**
     * 请求设置分组
     * @param group 分组
     */
    public void startAssignGroup(@NonNull RelationGroup group){
        assignGroupController.setValue(StringTrigger.getActioning(group.getId()));
    }
    public void logout(@NonNull Context context){
        localUserRepository.logout(context);
    }


    @Nullable
    public UserRelation getUserRelation() {
        if (relationLiveData.getValue() != null) {
            return relationLiveData.getValue().getData();
        }
        return null;
    }

    @Nullable
    public UserProfile getUserProfile() {
        if (profileLiveData.getValue() != null) {
            return profileLiveData.getValue().getData();
        }
        return null;
    }
    @Nullable
    public UserLocal getUserLocal() {
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        if(userLocal.isValid()) return userLocal;
        return null;
    }

}
