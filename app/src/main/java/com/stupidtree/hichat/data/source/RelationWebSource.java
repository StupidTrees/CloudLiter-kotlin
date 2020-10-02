package com.stupidtree.hichat.data.source;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.gson.JsonElement;
import com.stupidtree.hichat.data.model.FriendContact;
import com.stupidtree.hichat.service.LiveDataCallAdapter;
import com.stupidtree.hichat.service.RelationService;
import com.stupidtree.hichat.service.codes;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.LinkedList;
import java.util.List;

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
                .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
                .baseUrl("http://hita.store:3000").build());
    }

    @Override
    protected Class<RelationService> getServiceClass() {
        return RelationService.class;
    }


    /**
     * 获取好友列表
     *
     * @param token 登录状态token
     * @param id    此用户id（可选）
     * @return 朋友列表的LiveData
     */
    public LiveData<DataState<List<FriendContact>>> getFriends(String token, String id) {
        return Transformations.map(service.getFriends(token, id), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            } else {
                switch (input.getCode()) {
                    case codes.SUCCESS:
                        List<FriendContact> res = new LinkedList<>();
                        if (input.getData().isJsonArray()) {
                            for (JsonElement je : input.getData().getAsJsonArray()) {
                                FriendContact fc = FriendContact.getInstanceFromJsonObject(je);
                                if (null != fc) {
                                    res.add(fc);
                                }
                            }
                        }
                        return new DataState<>(res);
                    case codes.TOKEN_INVALID:
                        return new DataState<>(TOKEN_INVALID);
                    default:
                        return new DataState<>(FETCH_FAILED);
                }
            }
        });
    }


    /**
     * 建立好友关系
     *
     * @param token  用户登陆状态的token
     * @param friend 目标的用户id
     * @return 操作结果
     */
    public LiveData<DataState<Boolean>> makeFriends(@NonNull String token, String friend) {
        return Transformations.map(service.makeFriends(token, friend), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            } else {
                Log.e("result",input.getCode()+",,"+input.getMessage());
                switch (input.getCode()) {
                    case codes.TOKEN_INVALID:
                        return new DataState<>(TOKEN_INVALID);
                    case codes.SUCCESS:
                        return new DataState<>(true);
                    default:
                        return new DataState<>(FETCH_FAILED);
                }
            }
        });
    }


    /**
     * 判断是否为好友
     * @param token 令牌
     * @param userId 我的id
     * @param friend 他的id
     * @return 操作结果
     */
    public LiveData<DataState<Boolean>> isFriends(@NonNull String token,String userId,String friend){
        return Transformations.map(service.isFriends(token, userId, friend), input -> {
            if(input==null){
                return new DataState<>(FETCH_FAILED);
            }else{
                switch (input.getCode()){
                    case codes.SUCCESS:
                        return new DataState<>(input.getData());
                    case codes.TOKEN_INVALID:
                        return new DataState<>(TOKEN_INVALID);
                    default:return new DataState<>(FETCH_FAILED,input.getMessage());
                }
            }
        });
    }
}
