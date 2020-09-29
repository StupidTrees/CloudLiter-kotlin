package com.stupidtree.hichat.data.source;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonElement;
import com.stupidtree.hichat.data.ApiResponse;
import com.stupidtree.hichat.data.model.FriendContact;
import com.stupidtree.hichat.service.RelationService;
import com.stupidtree.hichat.service.codes;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.stupidtree.hichat.ui.base.DataState.STATE.FETCH_FAILED;
import static com.stupidtree.hichat.ui.base.DataState.STATE.TOKEN_INVALID;

/**
 * 层次：DataSource
 * 用户关系的数据源
 * 类型：网络数据
 * 数据：异步读，异步写
 */
public class RelationWebSource extends BaseWebSource<RelationService> {

    //单例模式
    private static volatile RelationWebSource instance;

    public static RelationWebSource getInstance() {
        if (instance == null) {
            instance = new RelationWebSource();
        }
        return instance;
    }

    public RelationWebSource() {
        super(new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://hita.store:3000").build());
    }

    @Override
    protected Class<RelationService> getServiceClass() {
        return RelationService.class;
    }


    /**
     * 获取好友列表
     * @param token 登录状态token
     * @param id 此用户id（可选）
     * @return 朋友列表的LiveData
     */
    public MutableLiveData<DataState<List<FriendContact>>> getFriends(String token, String id) {
        final MutableLiveData<DataState<List<FriendContact>>> result = new MutableLiveData<>();
        service.getFriends(token, id).enqueue(new Callback<ApiResponse<JsonElement>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonElement>> call, Response<ApiResponse<JsonElement>> response) {
                Log.e("success", String.valueOf(response.body()));
                List<FriendContact> res = new LinkedList<>();
                ApiResponse<JsonElement> resp = response.body();
                if (resp != null) {
                    Log.e("data", String.valueOf(resp.getData()));
                    if (resp.getData().isJsonArray()) {
                        for (JsonElement je : resp.getData().getAsJsonArray()) {
                            FriendContact fc = FriendContact.getInstanceFromJsonObject(je);
                            if (null != fc) {
                                res.add(fc);
                            }
                        }
                    }
                }
                result.setValue(new DataState<>(res));
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonElement>> call, Throwable t) {
                Log.e("failed", String.valueOf(t));
                result.setValue(
                        new DataState<>(FETCH_FAILED, t.getMessage()));
            }
        });
        return result;
    }


    /**
     * 建立好友关系
     * @param token 用户登陆状态的token
     * @param friend 目标的用户id
     * @return 操作结果
     */
    public MutableLiveData<DataState<Boolean>> makeFriends(@NonNull String token, String friend) {
        MutableLiveData<DataState<Boolean>> result = new MutableLiveData<>();
        service.makeFriends(token, friend).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                switch (response.body().getCode()) {
                    case codes.TOKEN_INVALID:
                        result.setValue(new DataState<>(TOKEN_INVALID));
                        break;
                    case codes.SUCCESS:
                        result.setValue(new DataState<>(true));
                        break;
                    default:
                        result.setValue(new DataState<>(FETCH_FAILED));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                result.setValue(new DataState<>(FETCH_FAILED));
            }
        });
        return result;
    }


}
