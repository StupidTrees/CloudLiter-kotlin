package com.stupidtree.hichat.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.source.RelationWebSource;
import com.stupidtree.hichat.data.source.UserWebSource;
import com.stupidtree.hichat.ui.base.DataState;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Repository层：用户资料页面的Repository
 */
public class ProfileRepository {
    private static ProfileRepository instance;

    public static ProfileRepository getInstance() {
        if (instance == null) {
            instance = new ProfileRepository();
        }
        return instance;
    }

    //数据源1：网络类型数据源，用户网络操作
    private final UserWebSource userWebSource;

    ProfileRepository() {
        userWebSource = UserWebSource.getInstance();
    }

    /**
     * 获取用户资料
     *
     * @param id    用户id
     * @param token 令牌
     * @return 用户资料
     * 这里的用户资料本体是UserProfile类
     * 其中DataState用于包装这个本体，附带状态信息
     * MutableLiveData则是UI层面的，用于和ViewModel层沟通
     */
    public LiveData<DataState<UserProfile>> getUserProfile(@Nullable String id, @NonNull String token) {
        return userWebSource.getUserProfile(id, token);
    }


    /**
     * 更改用户头像
     *
     * @param token    令牌
     * @param filePath 头像路径
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeAvatar(@NonNull String token, @NonNull String filePath) {
        //读取图片文件
        File file = new File(filePath);
        // MutableLiveData<DataState<String>> result = new MutableLiveData<>();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        //构造一个图片格式的POST表单
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("upload", file.getName(), requestFile);
        //调用网络数据源的服务，上传头像
        return Transformations.map(userWebSource.changeAvatar(token, body), input -> {
            if (input.getState() == DataState.STATE.SUCCESS) {
                //通知本地用户更新资料
                LocalUserRepository.getInstance().ChangeLocalAvatar(input.getData());
            }
            return input;
        });
    }


    /**
     * 更改用户昵称
     *
     * @param token    令牌
     * @param nickname 新昵称
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeNickname(@NonNull String token, @NonNull String nickname) {
        return Transformations.map(userWebSource.changeNickname(token, nickname), input -> {
            if (input.getState() == DataState.STATE.SUCCESS) {
                LocalUserRepository.getInstance().ChangeLocalNickname(nickname);
            }
            return input;
        });
    }


    /**
     * 更改用户性别
     *
     * @param token  令牌
     * @param gender 新性别 MALE/FEMALE
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeGender(@NonNull String token, @NonNull String gender) {
        return Transformations.map(userWebSource.changeGender(token, gender), input -> {
            if (input.getState() == DataState.STATE.SUCCESS) {
                LocalUserRepository.getInstance().ChangeLocalGender(gender);
            }
            return input;
        });
    }

    /**
     * 更改用户性别
     *
     * @param token 令牌
     * @param color 新颜色 赤橙黄绿青蓝紫
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeColor(@NonNull String token, @NonNull String color) {
        return Transformations.map(userWebSource.changeColor(token, color), input -> {
            if (input.getState() == DataState.STATE.SUCCESS) {
                //？？？？？？？？？？？？？？
            }
            return input;
        });
    }

    /**
     * 更改用户签名
     *
     * @param token     令牌
     * @param signature 新签名
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeSignature(@NonNull String token, @NonNull String signature) {
        return Transformations.map(userWebSource.changeSignature(token, signature), input -> {
            if (input.getState() == DataState.STATE.SUCCESS) {
                LocalUserRepository.getInstance().ChangeLocalSignature(signature);
            }
            return input;
        });
    }

    /**
     * 获取用户词云
     *
     * @param token 用户令牌
     * @return 词频表
     */
    public LiveData<DataState<HashMap<String, Float>>> getUserWordCloud(@Nullable String token, @NonNull String userId) {
        return userWebSource.getUserWordCloud(token,userId);
    }

}
