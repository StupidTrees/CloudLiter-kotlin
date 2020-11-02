package com.stupidtree.cloudliter.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.cloudliter.data.model.UserSearched;
import com.stupidtree.cloudliter.data.source.UserPreferenceSource;
import com.stupidtree.cloudliter.data.source.UserWebSource;
import com.stupidtree.cloudliter.ui.base.DataState;
import com.stupidtree.cloudliter.ui.welcome.login.LoginResult;
import com.stupidtree.cloudliter.ui.welcome.signup.SignUpResult;

import java.util.List;

/**
 * 层次：Repository层
 * 用户操作的Repository
 */
public class UserRepository {

    //单例模式
    private static volatile UserRepository instance;

    //数据源1:网络类型数据，用户网络数据源
    private final UserWebSource userWebSource;
    //数据源2：SharedPreference类型数据，本地用户数据源
    private final UserPreferenceSource userPreferenceSource;



    private UserRepository(Context context) {
        userWebSource = UserWebSource.getInstance();
        userPreferenceSource = UserPreferenceSource.getInstance(context.getApplicationContext());
    }

    // public方法：获取单例
    public static UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }


    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    public LiveData<LoginResult> login(String username, String password) {
        return Transformations.map(userWebSource.login(username, password), input -> {
            if(input.getState()== LoginResult.STATES.SUCCESS){
                userPreferenceSource.saveLocalUser(input.getUserLocal());
            }
            return input;
        });
    }


    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param gender 性别
     * @param nickname 昵称
     * @return 注册结果
     */
    public LiveData<SignUpResult> signUp(String username, String password,
                                                String gender, String nickname)
    {
        return Transformations.map(userWebSource.signUp(username, password, gender, nickname), input -> {
            if(input.getState()== SignUpResult.STATES.SUCCESS){
                userPreferenceSource.saveLocalUser(input.getUserLocal());
            }
            return input;
        });
    }


    /**
     * 搜索用户
     * @param text 搜索语句
     * @param token 令牌
     * @return 搜索结果列表
     */
    public LiveData<DataState<List<UserSearched>>> searchUser(String text, String token) {
        return userWebSource.searchUser(text, token);
    }
}