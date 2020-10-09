package com.stupidtree.hichat.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.RelationEvent;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.data.source.RelationWebSource;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.List;

public class RelationRepository {
    private static RelationRepository instance;

    /**
     * 数据源
     */
    //数据源：网络类型
    RelationWebSource relationWebSource;

    public static RelationRepository getInstance() {
        if (instance == null) {
            instance = new RelationRepository();
        }
        return instance;
    }

    public RelationRepository(){
        relationWebSource = RelationWebSource.getInstance();
    }

    /**
     * 判断某用户是否是本用户的好友
     *
     * @param token 令牌
     * @param id1   （非必须）本用户id
     * @param id2   （必须）目标用户id
     * @return Boolean型判断结果
     */
    public LiveData<DataState<Boolean>> isMyFriend(@NonNull String token, @Nullable String id1, @NonNull String id2) {
        return relationWebSource.isFriends(token, id1, id2);
    }

    /**
     * 建立好友关系
     *
     * @param token  令牌
     * @param friend 目标用户id
     * @return 操作结果
     */
    public LiveData<DataState<Boolean>> makeFriends(@NonNull String token, @NonNull String friend) {
        return relationWebSource.makeFriends(token, friend);
    }


    /**
     * 获取关系对象
     * @param token 令牌
     * @param friendId 朋友
     * @return 结果
     */
    public LiveData<DataState<UserRelation>> queryRelation(@NonNull String token,@NonNull String friendId){
        return relationWebSource.queryRelation(token,friendId);
    }

    /**
     * 更改用户备注
     *
     * @param token    令牌
     * @param remark 新备注
     * @return 操作结果
     */
    public LiveData<DataState<String>> changeRemark(@NonNull String token, @NonNull String remark,@NonNull String friend_id) {
        return relationWebSource.changeRemark(token,remark,friend_id);
    }


    /**
     * 发送好友请求
     * @param token 令牌
     * @param friendId 对方id
     * @return 操作结果
     */
    public LiveData<DataState<?>> sendFriendRequest(@NonNull String token, @NonNull String friendId){
        return relationWebSource.sendFriendRequest(token,friendId);
    }


    /**
     * 响应好友请求
     * @param token 令牌
     * @param eventId 事件id
     * @param action 操作
     * @return 操作结果
     */
    public LiveData<DataState<?>> responseFriendRequest(@NonNull String token, @NonNull String eventId, @NonNull RelationEvent.ACTION action){
        return relationWebSource.responseFriendRequest(token,eventId,action);
    }


    /**
     * 获得所有和我有关的好友请求
     * @param token 令牌
     * @return 请求结果
     */
    public LiveData<DataState<List<RelationEvent>>> queryMine(@NonNull String token){
        return relationWebSource.queryMine(token);
    }


    /**
     * 删除好友
     * @param token 令牌
     * @param friendId 好友id
     * @return 操作结果
     */
    public LiveData<DataState<?>> deleteFriend(@NonNull String token, @NonNull String friendId){
        return relationWebSource.deleteFriend(token,friendId);
    }

}
