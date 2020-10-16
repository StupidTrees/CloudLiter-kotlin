package com.stupidtree.hichat.data.repository;

import androidx.lifecycle.LiveData;
import com.stupidtree.hichat.data.model.RelationGroup;
import com.stupidtree.hichat.data.source.GroupWebSource;
import com.stupidtree.hichat.ui.base.DataState;
import java.util.List;

/**
 * 层次：Repository
 * 好友分组的仓库
 */
public class GroupRepository {

    //也是单例模式
    private static GroupRepository instance;

    public static GroupRepository getInstance() {
        if (instance == null) {
            instance = new GroupRepository();
        }
        return instance;
    }

    GroupRepository() {
        groupWebSource = GroupWebSource.getInstance();
    }

    //数据源1：网络数据源，分组网络数据
    GroupWebSource groupWebSource;


    /**
     * 获取我的所有好友分组
     * @param token 令牌
     * @return 查询结果
     */
    public LiveData<DataState<List<RelationGroup>>> queryMyGroups(String token) {
        return groupWebSource.queryMyGroups(token);
    }

}
