package com.stupidtree.hichat.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.data.source.RelationWebSource;
import com.stupidtree.hichat.ui.base.DataState;

public class RelationRepository {
    private static RelationRepository instance;

    /**
     * 数据源
     */
    //数据源：网络类型
    RelationWebSource relationWebSource;

    //将已登录用户缓存在内存里
    UserLocal loggedInUser = null;


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
}
