package com.stupidtree.hichat.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject;
import com.stupidtree.hichat.HiApplication;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.ApiResponse;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserSearched;
import com.stupidtree.hichat.data.source.UserPreferenceSource;
import com.stupidtree.hichat.data.source.UserWebSource;
import com.stupidtree.hichat.service.codes;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.welcome.login.LoginResult;
import com.stupidtree.hichat.ui.welcome.signup.SignUpResult;
import com.stupidtree.hichat.utils.JsonUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.stupidtree.hichat.service.codes.SUCCESS;
import static com.stupidtree.hichat.service.codes.USER_ALREADY_EXISTS;
import static com.stupidtree.hichat.service.codes.WRONG_PASSWORD;
import static com.stupidtree.hichat.service.codes.WRONG_USERNAME;

/**
 * 层次：Repository层
 * 用户操作的Repository
 */
public class UserRepository {

    //单例模式
    private static volatile UserRepository instance;

    //数据源1:网络类型数据，用户网络数据源
    private UserWebSource userWebSource;
    //数据源2：SharedPreference类型数据，本地用户数据源
    private UserPreferenceSource userPreferenceSource;

    private UserRepository() {
        userWebSource = UserWebSource.getInstance();
        userPreferenceSource = UserPreferenceSource.getInstance(HiApplication.getContext());
    }

    // public方法：获取单例
    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }


    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    public MutableLiveData<LoginResult> login(String username, String password) {
        final MutableLiveData<LoginResult> loginRes = new MutableLiveData<>();
        userWebSource.getService().login(username, password)
                .enqueue(new Callback<ApiResponse<JsonObject>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
                        LoginResult loginResult = new LoginResult();
                        int code = response.body().getCode();
                        Log.e("response", response.body().toString());
                        switch (code) {
                            case WRONG_USERNAME:
                                Log.e("RESPONSE", "用户名错误");
                                loginResult.set(LoginResult.STATES.WRONG_USERNAME, R.string.login_failed_wrong_username);
                                break;
                            case WRONG_PASSWORD:
                                Log.e("RESPONSE", "密码错误");
                                loginResult.set(LoginResult.STATES.WRONG_PASSWORD, R.string.login_failed_wrong_password);
                                break;
                            case SUCCESS:
                                Log.e("RESPONSE", "登录成功");
                                String token = JsonUtils.getStringData(
                                        response.body().getData(), "token");

                                if (null == token) {
                                    Log.e("RESPONSE", "没有找到token");
                                    loginResult.set(LoginResult.STATES.ERROR, R.string.login_failed);
                                } else {
                                    loginResult.set(LoginResult.STATES.SUCCESS, R.string.login_success);
                                    loginResult.setToken(token);
                                }
                                userPreferenceSource.saveLocalUser(UserLocal.getFromResponseData(response.body().getData()));
                                break;
                            default:
                                Log.e("ESPONSE", "登录失败" + code);
                                loginResult.set(LoginResult.STATES.ERROR, R.string.login_failed);
                                break;
                        }

                        loginRes.setValue(loginResult);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
                        loginRes.setValue(new LoginResult(LoginResult.STATES.REQUEST_FAILED, R.string.http_request_failed));

                    }
                });

        return loginRes;
    }


    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param gender 性别
     * @param nickname 昵称
     * @return 注册结果
     */
    public MutableLiveData<SignUpResult> signUp(String username, String password,
                                                String gender, String nickname) {
        final MutableLiveData<SignUpResult> signUpRes = new MutableLiveData<>();
        userWebSource.getService().signUp(username, password, gender, nickname).enqueue(new Callback<ApiResponse<JsonObject>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
                SignUpResult signUpResult = new SignUpResult();
                int code = response.body().getCode();
                Log.e("response", response.body().toString());
                if (code == SUCCESS) {
                    Log.e("RESPONSE", "注册成功");
                    String token = JsonUtils.getStringData(
                            response.body().getData(), "token");
                    if (null == token) {
                        Log.e("RESPONSE", "没有找到token");
                        signUpResult.set(SignUpResult.STATES.ERROR, R.string.sign_up_failed);
                    } else {
                        signUpResult.set(SignUpResult.STATES.SUCCESS, R.string.sign_up_success);
                    }
                    userPreferenceSource.saveLocalUser(UserLocal.getFromResponseData(response.body().getData()));
                } else if (code == USER_ALREADY_EXISTS) {
                    Log.e("RESPONSE", "注册失败:" + code);
                    signUpResult.set(SignUpResult.STATES.USER_EXISTS, R.string.user_already_exists);
                } else {
                    Log.e("RESPONSE", "注册失败:" + code);
                    signUpResult.set(SignUpResult.STATES.ERROR, R.string.sign_up_failed);
                }

                signUpRes.setValue(signUpResult);
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
                signUpRes.setValue(new SignUpResult(SignUpResult.STATES.REQUEST_FAILED, R.string.http_request_failed));

            }
        });

        return signUpRes;
    }


    /**
     * 搜索用户
     * @param text 搜索语句
     * @param token 令牌
     * @return 搜索结果列表
     */
    public MutableLiveData<DataState<List<UserSearched>>> searchUser(String text, String token) {
        final MutableLiveData<DataState<List<UserSearched>>> result = new MutableLiveData<>();
        userWebSource.getService().searchUser(text, token).enqueue(new Callback<ApiResponse<List<UserSearched>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserSearched>>> call, Response<ApiResponse<List<UserSearched>>> response) {
                Log.e("searched", String.valueOf(response.body()));
                ApiResponse<List<UserSearched>> resp = response.body();
                if (resp != null) {
                    switch (resp.getCode()) {
                        case codes.SUCCESS:
                            result.setValue(new DataState<>(response.body().getData()));
                            break;
                        case codes.TOKEN_INVALID:
                            result.setValue(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                            break;
                        default:
                            result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
                    }

                } else {
                    result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED));
                }

            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserSearched>>> call, Throwable t) {
                Log.e("failed", String.valueOf(t));
                result.setValue(new DataState<>(DataState.STATE.FETCH_FAILED, t.getMessage()));
            }
        });
        return result;
    }
}