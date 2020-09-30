package com.stupidtree.hichat.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LiveDataCallAdapter implements CallAdapter<LiveData<?>> {
    private final Type responseType;

    // 下面的 responseType 方法需要数据的类型
    LiveDataCallAdapter(Type responseType) {
        this.responseType = responseType;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public <R> LiveData<?> adapt(Call<R> call) {
        MutableLiveData<R> result = new MutableLiveData<>();
        call.enqueue(new Callback<R>() {
            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                result.postValue(response.body());
            }

            @Override
            public void onFailure(Call<R> call, Throwable t) {
                result.postValue(null);
            }
        });
        return result;
    }




    public static class LiveDataCallAdapterFactory extends CallAdapter.Factory {
        public static final LiveDataCallAdapterFactory INSTANCE = new LiveDataCallAdapterFactory();

        @Override
        public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
            // 获取原始类型
            Class<?> rawType = getRawType(returnType);
            // 返回值必须是CustomCall并且带有泛型
            if (rawType == LiveData.class && returnType instanceof ParameterizedType) {
                Type callReturnType = getParameterUpperBound(0, (ParameterizedType) returnType);
                return new LiveDataCallAdapter(callReturnType);
            }
            return null;
        }
    }
}
