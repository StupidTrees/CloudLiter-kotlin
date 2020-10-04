package com.stupidtree.hichat.data.source;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.model.UserSearched;
import com.stupidtree.hichat.service.LiveDataCallAdapter;
import com.stupidtree.hichat.service.UserService;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.welcome.login.LoginResult;
import com.stupidtree.hichat.ui.welcome.signup.SignUpResult;
import com.stupidtree.hichat.utils.JsonUtils;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.stupidtree.hichat.service.codes.SUCCESS;
import static com.stupidtree.hichat.service.codes.TOKEN_INVALID;
import static com.stupidtree.hichat.service.codes.USER_ALREADY_EXISTS;
import static com.stupidtree.hichat.service.codes.WRONG_PASSWORD;
import static com.stupidtree.hichat.service.codes.WRONG_USERNAME;

/**
 * 层次：DataSource
 * 用户的数据源
 * 类型：网络数据
 * 数据：异步读，异步写
 */
public class UserWebSource extends BaseWebSource<UserService> {

    //单例模式
    private static volatile UserWebSource instance;

    public static UserWebSource getInstance() {
        if (instance == null) {
            instance = new UserWebSource();
        }
        return instance;
    }

    public UserWebSource() {
        super(new Retrofit.Builder()
                .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://hita.store:3000").build());
    }

    @Override
    protected Class<UserService> getServiceClass() {
        return UserService.class;
    }


    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    public LiveData<LoginResult> login(String username,String password){
        return Transformations.map(service.login(username, password), input -> {
            LoginResult loginResult = new LoginResult();
            if(null==input){
                loginResult.set(LoginResult.STATES.ERROR, R.string.login_failed);
            }else{
                switch (input.getCode()){
                    case SUCCESS:
                        Log.e("RESPONSE", "登录成功");
                        String token = JsonUtils.getStringData(
                               input.getData(), "token");
                        if (null == token) {
                            Log.e("RESPONSE", "没有找到token");
                            loginResult.set(LoginResult.STATES.ERROR, R.string.login_failed);
                        } else {
                            loginResult.set(LoginResult.STATES.SUCCESS, R.string.login_success);
                            loginResult.setToken(token);
                            loginResult.setUserLocal(UserLocal.getFromResponseData(input.getData()));
                        }

                        break;
                    case WRONG_USERNAME:
                        Log.e("RESPONSE", "用户名错误");
                        loginResult.set(LoginResult.STATES.WRONG_USERNAME, R.string.login_failed_wrong_username);
                        break;
                    case WRONG_PASSWORD:
                        Log.e("RESPONSE", "密码错误");
                        loginResult.set(LoginResult.STATES.WRONG_PASSWORD, R.string.login_failed_wrong_password);
                        break;
                    default:
                        loginResult.set(LoginResult.STATES.ERROR, R.string.login_failed);
                }
            }
            return loginResult;

        });
//        userWebSource.getService().login(username, password)
//                .enqueue(new Callback<ApiResponse<JsonObject>>() {
//                    @Override
//                    public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
//                        LoginResult loginResult = new LoginResult();
//                        int code = response.body().getCode();
//                        Log.e("response", response.body().toString());
//                        switch (code) {
//                            case WRONG_USERNAME:
//                                break;
//                           case SUCCESS:
//                                  break;
//                            default:
//                                Log.e("ESPONSE", "登录失败" + code);
//
//                                break;
//                        }
//
//                        loginRes.setValue(loginResult);
//                    }
//
//                    @Override
//                    public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
//                        loginRes.setValue(new LoginResult(LoginResult.STATES.REQUEST_FAILED, R.string.http_request_failed));
//
//                    }
//                });

    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param gender 性别 MALE/FEMALE
     * @param nickname 昵称
     * @return 注册结果
     */
    public LiveData<SignUpResult> signUp(String username,String password,String gender,String nickname){
        return Transformations.map(service.signUp(username, password, gender, nickname),
                input -> {
                    SignUpResult signUpResult = new SignUpResult();
                    if(input!=null){
                        switch (input.getCode()){
                            case SUCCESS:
                                Log.e("RESPONSE", "注册成功");
                                String token = JsonUtils.getStringData(
                                        input.getData(), "token");
                                if (null == token) {
                                    Log.e("RESPONSE", "没有找到token");
                                    signUpResult.set(SignUpResult.STATES.ERROR, R.string.sign_up_failed);
                                } else {
                                    signUpResult.set(SignUpResult.STATES.SUCCESS, R.string.sign_up_success);
                                }
                                signUpResult.setUserLocal(UserLocal.getFromResponseData(input.getData()));
                              //
                                break;
                            case USER_ALREADY_EXISTS:
                                signUpResult.set(SignUpResult.STATES.USER_EXISTS, R.string.user_already_exists);
                                break;
                            default:
                                signUpResult.set(SignUpResult.STATES.ERROR, R.string.sign_up_failed);
                        }
                    }else{
                        signUpResult.set(SignUpResult.STATES.ERROR, R.string.sign_up_failed);
                    }
                    return signUpResult;
                });
    }


    /**
     * 搜索用户
     * @param text 检索语句
     * @param token 令牌
     * @return 搜索结果
     */
    public LiveData<DataState<List<UserSearched>>> searchUser(String text, String token){
        return Transformations.map(service.searchUser(text, token), input -> {
            if(input!=null){
                switch (input.getCode()){
                    case SUCCESS:
                        return new DataState<>(input.getData());
                    case TOKEN_INVALID:
                        return new DataState<>(DataState.STATE.TOKEN_INVALID);
                    default:
                        return new DataState<>(DataState.STATE.FETCH_FAILED,input.getMessage());
                }
            }
            return new DataState<>(DataState.STATE.FETCH_FAILED);
        });

    }


    /**
     * 获取用户资料
     * @param id 用户id
     * @param token 用户令牌
     * @return 资料
     */
    public LiveData<DataState<UserProfile>> getUserProfile(@Nullable String id, @NonNull String token){
        return Transformations.map(service.getUserProfile(id, token), input -> {
            if(input!=null){
                switch (input.getCode()){
                    case SUCCESS:
                        return new DataState<>(input.getData());
                    case TOKEN_INVALID:
                        return new DataState<>(DataState.STATE.TOKEN_INVALID);
                    default:
                        return new DataState<>(DataState.STATE.FETCH_FAILED);
                }
            }
            return new DataState<>(DataState.STATE.FETCH_FAILED);
        });
    }

    /**
     * 换昵称
     * @param token 令牌
     * @param nickname 昵称
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeNickname(@NonNull String token,@NonNull String nickname){
        return Transformations.map(service.changeNickname(nickname, token), input -> {
            if(input!=null){
                switch (input.getCode()){
                    case SUCCESS:
                        return new DataState<>(DataState.STATE.SUCCESS);
                    case TOKEN_INVALID:
                        return new DataState<>(DataState.STATE.TOKEN_INVALID);
                    default:
                        return new DataState<>(DataState.STATE.FETCH_FAILED,input.getMessage());
                }
            }
            return new DataState<>(DataState.STATE.FETCH_FAILED);
        });
    }


    /**
     * 更换性别
     * @param token 令牌
     * @param gender 性别 MALE/FEMALE
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeGender(@NonNull String token,@NonNull String gender){
        return Transformations.map(service.changeGender(gender, token), input -> {
            if(input!=null){
                switch (input.getCode()){
                    case SUCCESS:
                        return new DataState<>(DataState.STATE.SUCCESS);
                    case TOKEN_INVALID:
                        return new DataState<>(DataState.STATE.TOKEN_INVALID);
                    default:
                        return new DataState<>(DataState.STATE.FETCH_FAILED,input.getMessage());
                }
            }
            return new DataState<>(DataState.STATE.FETCH_FAILED);
        });
    }


    /**
     * 更换性别
     * @param token 令牌
     * @param signature 签名
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeSignature(@NonNull String token,@NonNull String signature){
        return Transformations.map(service.changeSignature(signature, token), input -> {
            Log.e( "changeSignature: ", input.toString());
            if(input!=null){

                switch (input.getCode()){
                    case SUCCESS:
                        System.out.println("SUCCEED");
                        return new DataState<>(DataState.STATE.SUCCESS);
                    case TOKEN_INVALID:
                        return new DataState<>(DataState.STATE.TOKEN_INVALID);
                    default:
                        return new DataState<>(DataState.STATE.FETCH_FAILED,input.getMessage());
                }
            }

            return new DataState<>(DataState.STATE.FETCH_FAILED);
        });
    }


    /**
     * 更换头像
     * @param token 令牌
     * @param file 图片请求包
     * @return 返回
     */
    public LiveData<DataState<String>> changeAvatar(@NonNull String token,@NonNull MultipartBody.Part file){
        return Transformations.map(service.uploadAvatar(file, token), input -> {
            if(input==null){
                return new DataState<>(DataState.STATE.FETCH_FAILED);
            }
            switch (input.getCode()){
                case SUCCESS:
                    String file1 = JsonUtils.getStringData(input.getData(),"file");
                    if(file1 !=null){
                        return new DataState<>(file1);
                    }else{
                        return new DataState<>(DataState.STATE.FETCH_FAILED);
                    }

                case TOKEN_INVALID:
                    return new DataState<>(DataState.STATE.TOKEN_INVALID);
                default:
                    return new DataState<>(DataState.STATE.FETCH_FAILED,input.getMessage());
            }
        });
    }
}
