package com.stupidtree.hichat.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject;
import com.stupidtree.hichat.data.ApiResponse;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.source.RelationWebSource;
import com.stupidtree.hichat.data.source.UserWebSource;
import com.stupidtree.hichat.service.codes;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.JsonUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository层：用户资料页面的Repository
 */
public class ProfileRepository {
    private static ProfileRepository instance;
    public static ProfileRepository getInstance() {
        if(instance==null){
            instance = new ProfileRepository();
        }
        return instance;
    }

    //数据源1：网络类型数据源，用户网络操作
    private UserWebSource userWebSource;
    //数据源2：网络类型数据源，关系网络操作
    private RelationWebSource relationWebSource;

    ProfileRepository(){
        userWebSource = UserWebSource.getInstance();
        relationWebSource = RelationWebSource.getInstance();
    }

    /**
     * 获取用户资料
     * @param id 用户id
     * @param token 令牌
     * @return 用户资料
     * 这里的用户资料本体是UserProfile类
     * 其中DataState用于包装这个本体，附带状态信息
     * MutableLiveData则是UI层面的，用于和ViewModel层沟通
     */
    public MutableLiveData<DataState<UserProfile>> getUserProfile(@Nullable String id, @NonNull String token){
        final MutableLiveData<DataState<UserProfile>> result = new MutableLiveData<>();
        userWebSource.getService().getUserProfile(id,token).enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                Log.e("fetched", String.valueOf(response.body()));
                ApiResponse<UserProfile> resp = response.body();
                if(resp!=null&&resp.getData()!=null){
                    result.setValue(new DataState<>(resp.getData()));
                }else{
                    result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
                }

            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                Log.e("failed", String.valueOf(t));
                result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
            }
        });
        return result;
    }

    /**
     * 判断某用户是否是本用户的好友
     * @param token 令牌
     * @param id1 （非必须）本用户id
     * @param id2 （必须）目标用户id
     * @return Boolean型判断结果
     */
    public MutableLiveData<DataState<Boolean>> isMyFriend(@NonNull String token,@Nullable String id1,@NonNull String id2){
        final MutableLiveData<DataState<Boolean>> result = new MutableLiveData<>();
        relationWebSource.service.isFriends(token,id1,id2).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                Log.e("fetched", String.valueOf(response.body()));
                result.setValue(new DataState<>(response.body().getData()));
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
            }
        });
        return result;
    }

    /**
     * 建立好友关系
     * @param token 令牌
     * @param friend 目标用户id
     * @return 操作结果
     */
    public MutableLiveData<DataState<Boolean>> makeFriends(@NonNull String token,@NonNull String friend){
        return relationWebSource.makeFriends(token,friend);
    }

    /**
     * 更改用户头像
     * @param token 令牌
     * @param filePath 头像路径
     * @return 操作结果
     */
    public MutableLiveData<DataState<String>> changeAvatar(@NonNull String token,@NonNull String filePath){
        //读取图片文件
        File file = new File(filePath);
        MutableLiveData<DataState<String>> result = new MutableLiveData<>();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        //构造一个图片格式的POST表单
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("upload", file.getName(), requestFile);
        //调用网络数据源的服务，上传头像
        userWebSource.getService().uploadAvatar(body,token).enqueue(
                new Callback<ApiResponse<JsonObject>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
                        if(response.body().getCode()== codes.SUCCESS){
                            //上传成功
                            //Log.e("result", String.valueOf(response.body()));
                            String file = JsonUtils.getStringData(response.body().getData(),"file");
                            if(file!=null){
                                result.setValue(new DataState<>(file));
                                //通知本地用户更新资料
                                MeRepository.getInstance().ChangeLocalAvatar(file);
                            }
                        }else{
                            result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED,response.body().getMessage()));
                        }
                      }

                    @Override
                    public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
                        result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
                    }
                }
        );
        return result;
    }


    /**
     * 更改用户昵称
     * @param token 令牌
     * @param nickname 新昵称
     * @return 操作结果
     */
    public MutableLiveData<DataState<String>> changeNickname(@NonNull String token,@NonNull String nickname){
        final MutableLiveData<DataState<String >> result = new MutableLiveData<>();
        userWebSource.getService().changeNickname(nickname,token).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
             ApiResponse<Object> resp = response.body();
             if(resp!=null){
                 Log.e("resp", String.valueOf(resp));
                 if(resp.getCode()==codes.SUCCESS){
                     result.setValue(new DataState<>(DataState.STATE.SUCCESS));
                     MeRepository.getInstance().ChangeLocalNickname(nickname);
                 }else{
                     result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED,resp.getMessage()));
                 }
             }else{
                 result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
             }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
            }
        });
        return result;
     }

    /**
     * 更改用户性别
     * @param token 令牌
     * @param gender 新性别 MALE/FEMALE
     * @return 操作结果
     */
    public MutableLiveData<DataState<String>> changeGender(@NonNull String token,@NonNull String gender){
        final MutableLiveData<DataState<String >> result = new MutableLiveData<>();
        userWebSource.getService().changeGender(gender,token).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                ApiResponse<Object> resp = response.body();
                if(resp!=null){
                    if(resp.getCode()==codes.SUCCESS){
                        result.setValue(new DataState<>(DataState.STATE.SUCCESS));
                        MeRepository.getInstance().ChangeLocalGender(gender);
                    }else{
                        result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED,resp.getMessage()));
                    }
                }else{
                    result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
            }
        });
        return result;
    }

}
