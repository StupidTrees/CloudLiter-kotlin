package com.stupidtree.hichat.data.source;

import com.stupidtree.hichat.service.UserService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://hita.store:3000").build());
    }

    @Override
    protected Class<UserService> getServiceClass() {
        return UserService.class;
    }

    public UserService getService(){
        return service;
    }
}
