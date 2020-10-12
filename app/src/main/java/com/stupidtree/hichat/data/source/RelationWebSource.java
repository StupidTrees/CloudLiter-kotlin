package com.stupidtree.hichat.data.source;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stupidtree.hichat.data.model.RelationEvent;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.service.LiveDataCallAdapter;
import com.stupidtree.hichat.service.RelationService;
import com.stupidtree.hichat.service.codes;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.stupidtree.hichat.service.codes.SUCCESS;
import static com.stupidtree.hichat.ui.base.DataState.STATE.FETCH_FAILED;
import static com.stupidtree.hichat.ui.base.DataState.STATE.NOT_EXIST;
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
    public LiveData<DataState<List<UserRelation>>> getFriends(String token, String id) {
        return Transformations.map(service.getFriends(token, id), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            } else {
                switch (input.getCode()) {
                    case codes.SUCCESS:
                        List<UserRelation> res = new LinkedList<>();
                        if (input.getData().isJsonArray()) {
                            for (JsonElement je : input.getData().getAsJsonArray()) {
                                UserRelation fc = UserRelation.getInstanceFromJsonObject(je);
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


    /**
     * 获取我和朋友的关系对象
     * @param token 令牌
     * @param friendId 朋友的id
     * @return 操作结果
     */
    public LiveData<DataState<UserRelation>> queryRelation(@NonNull String token,@NonNull String friendId){
        return Transformations.map(service.queryRelation(token,friendId), input -> {
            if(input==null){
                return new DataState<>(FETCH_FAILED);
            }else{
                switch (input.getCode()){
                    case codes.SUCCESS:
                        JsonObject jo = input.getData();
                        UserRelation ur = UserRelation.getInstanceFromJsonObject(jo);
                        Log.e("获取关系对象", String.valueOf(ur));
                        if(ur!=null){
                            return new DataState<>(ur);
                        }else{
                            return new DataState<>(FETCH_FAILED);
                        }
                    case codes.TOKEN_INVALID:
                        return new DataState<>(TOKEN_INVALID);
                    case codes.RELATION_NOT_EXIST:
                        return new DataState<>(NOT_EXIST);
                    default:return new DataState<>(FETCH_FAILED,input.getMessage());
                }
            }
        });
    }


    /**
     * 更换备注
     * @param token 令牌
     * @param remark 备注
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeRemark(@NonNull String token,@NonNull String remark,@NonNull String friend_id){
        return Transformations.map(service.changeRemark(friend_id,remark, token), input -> {
            //Log.e( "changeRemark: ", input.toString());
            if(input!=null){
                System.out.println("input remark is "+ input);
                switch (input.getCode()){
                    case SUCCESS:
                        System.out.println("SUCCEED");
                        return new DataState<>(DataState.STATE.SUCCESS);
                    case codes.TOKEN_INVALID:
                        return new DataState<>(DataState.STATE.TOKEN_INVALID);
                    default:
                        return new DataState<>(DataState.STATE.FETCH_FAILED,input.getMessage());
                }
            }

            return new DataState<>(DataState.STATE.FETCH_FAILED);
        });
    }


    /**
     * 发送好友请求
     *
     * @param token 登录状态token
     * @return 操作结果
     */
    public LiveData<DataState<?>> sendFriendRequest(@NonNull String token, @NonNull String friendId) {
        return Transformations.map(service.sendFriendRequest(token, friendId), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            }
            switch (input.getCode()) {
                case codes.SUCCESS:
                    return new DataState<>(DataState.STATE.SUCCESS);
                case codes.TOKEN_INVALID:
                    return new DataState<>(TOKEN_INVALID,input.getMessage());
                case codes.REQUEST_ALREADY_SENT:
                    return new DataState<>(TOKEN_INVALID,"已经发送过申请啦！");
                default:
                    return new DataState<>(FETCH_FAILED,input.getMessage());
            }
        });
    }


    /**
     * 获得所有和我有关的好友请求
     * @param token 令牌
     * @return 请求结果
     */
    public LiveData<DataState<List<RelationEvent>>> queryMine(@NonNull String token){
        return Transformations.map(service.queryMine(token), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            }
            switch (input.getCode()) {
                case codes.SUCCESS:
                    return new DataState<>(input.getData());
                case codes.TOKEN_INVALID:
                    return new DataState<>(TOKEN_INVALID,input.getMessage());
                default:
                    return new DataState<>(FETCH_FAILED,input.getMessage());
            }
        });
    }


    /**
     * 响应好友请求
     *
     * @param token 登录状态token
     * @return 操作结果
     */
    public LiveData<DataState<?>> responseFriendRequest(@NonNull String token, @NonNull String eventId,@NonNull RelationEvent.ACTION action) {
        return Transformations.map(service.responseFriendRequest(token,eventId,action.toString()), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            }
            switch (input.getCode()) {
                case codes.SUCCESS:
                    return new DataState<>(DataState.STATE.SUCCESS);
                case codes.TOKEN_INVALID:
                    return new DataState<>(TOKEN_INVALID,input.getMessage());
                default:
                    return new DataState<>(FETCH_FAILED,input.getMessage());
            }
        });
    }

    /**
     * 删除好友
     * @param token 登录状态token
     * @param friendId 好友id
     * @return 操作结果
     */
    public LiveData<DataState<?>> deleteFriend(@NonNull String token, @NonNull String friendId) {
        return Transformations.map(service.deleteFriend(token,friendId), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            }
            switch (input.getCode()) {
                case codes.SUCCESS:
                    return new DataState<>(DataState.STATE.SUCCESS);
                case codes.TOKEN_INVALID:
                    return new DataState<>(TOKEN_INVALID,input.getMessage());
                default:
                    return new DataState<>(FETCH_FAILED,input.getMessage());
            }
        });
    }
    /**
     * 获取未读好友事件数目
     * @param token 登录状态token
     * @return 操作结果
     */
    public LiveData<DataState<Integer>> countUnread(@NonNull String token) {
        return Transformations.map(service.countUnread(token), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            }
            switch (input.getCode()) {
                case codes.SUCCESS:
                    return new DataState<>(input.getData());
                case codes.TOKEN_INVALID:
                    return new DataState<>(TOKEN_INVALID,input.getMessage());
                default:
                    return new DataState<>(FETCH_FAILED,input.getMessage());
            }
        });
    }


    /**
     * 标记好友事件全部已读
     * @param token 登录状态token
     * @return 操作结果
     */
    public LiveData<DataState<Object>> markRead(@NonNull String token) {
        return Transformations.map(service.markRead(token), input -> {
            if (null == input) {
                return new DataState<>(FETCH_FAILED);
            }
            switch (input.getCode()) {
                case codes.SUCCESS:
                    return new DataState<>(input.getData());
                case codes.TOKEN_INVALID:
                    return new DataState<>(TOKEN_INVALID,input.getMessage());
                default:
                    return new DataState<>(FETCH_FAILED,input.getMessage());
            }
        });
    }
}
