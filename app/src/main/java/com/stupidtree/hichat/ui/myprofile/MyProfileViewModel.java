package com.stupidtree.hichat.ui.myprofile;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.repository.MeRepository;
import com.stupidtree.hichat.data.repository.ProfileRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.profile.ProfileTrigger;

import java.util.Objects;

/**
 * 层次：ViewModel
 * 和”我的资料“Activity绑定的ViewModel
 */
public class MyProfileViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：我的用户资料
    LiveData<DataState<UserProfile>> userProfileLiveData;
    //Trigger：控制↑的刷新
    MutableLiveData<ProfileTrigger> profileController = new MutableLiveData<>();

    //状态数据：更改头像的结果
    LiveData<DataState<String>> changeAvatarResult;
    //Trigger：控制更改头像请求的发送，其中携带了新头像文件的路径字符串
    MutableLiveData<ChangeInfoTrigger> changeAvatarController = new MutableLiveData<>();

    //状态数据：更改昵称的结果
    LiveData<DataState<String>> changeNicknameResult;
    //Trigger：控制更改昵称请求的发送，其中携带了新昵称字符串
    MutableLiveData<ChangeInfoTrigger> changeNicknameController = new MutableLiveData<>();

    //状态数据：更改性别的结果
    LiveData<DataState<String>> changeGenderResult;
    //Trigger：控制更改性别请求的发送，其中携带了新性别字符串
    MutableLiveData<ChangeInfoTrigger> changeGenderController = new MutableLiveData<>();


    /**
     * 仓库区
     */
    //仓库1：用户资料仓库
    private ProfileRepository profileRepository;
    //仓库2：本地用户仓库
    private MeRepository meRepository;

    public MyProfileViewModel(){
        profileRepository = ProfileRepository.getInstance();
        meRepository = MeRepository.getInstance();
    }

    public LiveData<DataState<UserProfile>> getUserProfileLiveData() {
        if(userProfileLiveData==null){
            //controller改变的时候，通知userProfile改变
            userProfileLiveData = Transformations.switchMap(profileController, (Function<ProfileTrigger, LiveData<DataState<UserProfile>>>) input -> {
                UserLocal userLocal = meRepository.getLoggedInUserDirect();
                if(userLocal.isValid()){
                    //从用户资料仓库总取出数据
                    return profileRepository.getUserProfile(userLocal.getId(), Objects.requireNonNull(userLocal.getToken()));
                }else{
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                }
            });
        }
        return userProfileLiveData;
    }

    public LiveData<DataState<String>> getChangeAvatarResult() {
        if(changeAvatarResult==null){
            //controller改变时。。。巴拉巴拉
            changeAvatarResult = Transformations.switchMap(changeAvatarController, new Function<ChangeInfoTrigger, LiveData<DataState<String>>>() {
                @Override
                public LiveData<DataState<String>> apply(ChangeInfoTrigger input) {
                    if(input.isActioning()){
                        //要先判断本地用户当前是否登录
                        UserLocal userLocal = meRepository.getLoggedInUserDirect();
                        if(userLocal.isValid()){
                            //通知用户资料仓库，开始更换头像
                            return profileRepository.changeAvatar(Objects.requireNonNull(userLocal.getToken()),input.getValue());
                        }else{
                            return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                        }

                    }else{
                        return new MutableLiveData<>();
                    }
                }
            });
        }
        return changeAvatarResult;
    }


    public LiveData<DataState<String>> getChangeNicknameResult() {
        if(changeNicknameResult==null){
            //也是一样的
            changeNicknameResult = Transformations.switchMap(changeNicknameController, new Function<ChangeInfoTrigger, LiveData<DataState<String>>>() {
                @Override
                public LiveData<DataState<String>> apply(ChangeInfoTrigger input) {
                    if(input.isActioning()){
                        UserLocal userLocal = meRepository.getLoggedInUserDirect();
                        if(userLocal.isValid()){
                            return profileRepository.changeNickname(Objects.requireNonNull(userLocal.getToken()),input.getValue());
                        }else{
                            return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                        }
                    }
                    return new MutableLiveData<>();
                }
            });
        }
        return changeNicknameResult;
    }

    public LiveData<DataState<String>> getChangeGenderResult() {
        if(changeGenderResult==null){
            changeGenderResult = Transformations.switchMap(changeGenderController, input -> {
                if(input.isActioning()){
                    UserLocal userLocal = meRepository.getLoggedInUserDirect();
                    if(userLocal.isValid()){
                        return profileRepository.changeGender(Objects.requireNonNull(userLocal.getToken()),input.getValue());
                    }else{
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>();
            });
        }
        return changeGenderResult;
    }

    public void logout(){
        meRepository.logout();
    }

    /**
     * 发起更换头像请求
     * @param path 新头像的路径
     */
    public void startChangeAvatar(String path){
        changeAvatarController.setValue(ChangeInfoTrigger.getActioning(path));
    }

    /**
     * 发起更换昵称请求
     * @param nickname 新昵称字符串
     */
    public void startChangeNickname(String nickname){
        changeNicknameController.setValue(ChangeInfoTrigger.getActioning(nickname));
    }

    /**
     * 发起更换性别请求
     * @param gender 新性别
     */
    public void startChangeGender(UserLocal.GENDER gender){
        String genderStr = gender== UserLocal.GENDER.MALE?"MALE":"FEMALE";
        changeGenderController.setValue(ChangeInfoTrigger.getActioning(genderStr));
    }

    /**
     * 开始页面刷新（即用户profile的获取）
     */
    public void startRefresh(){
        UserLocal userLocal = meRepository.getLoggedInUserDirect();
        profileController.setValue(ProfileTrigger.getActioning(userLocal.getId()));
    }
}
