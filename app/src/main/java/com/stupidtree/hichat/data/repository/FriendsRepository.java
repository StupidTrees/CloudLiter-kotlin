package com.stupidtree.hichat.data.repository;

import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.FriendContact;
import com.stupidtree.hichat.data.source.RelationWebSource;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.List;

/**
 * 层次：Repository层
 * 好友Repository
 */
public class FriendsRepository {
    //单例模式
    private static volatile FriendsRepository instance;

    //数据源：网络类型数据原
    private RelationWebSource relationWebSource;


    public static FriendsRepository getInstance(){
        if(instance==null){
            instance = new FriendsRepository();
        }
        return instance;
    }

    private FriendsRepository(){
        relationWebSource = RelationWebSource.getInstance();
    }

    /**
     * 获取所有好友
     * 直接调用数据源的相应函数
     * @param token 用户登陆状态token
     * @param id 用户id
     * @return 转发自数据源的LiveData
     */
    public LiveData<DataState<List<FriendContact>>> getFriends(String token, String id){
        return relationWebSource.getFriends(token,id);
    }
}
