package com.stupidtree.cloudliter.data.repository

import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.source.RelationWebSource
import com.stupidtree.cloudliter.ui.base.DataState

/**
 * 层次：Repository层
 * 好友Repository
 */
class FriendsRepository private constructor() {
    //数据源：网络类型数据原
    private val relationWebSource: RelationWebSource = RelationWebSource.instance!!

    /**
     * 获取所有好友
     * 直接调用数据源的相应函数
     * @param token 用户登陆状态token
     * @param id 用户id
     * @return 转发自数据源的LiveData
     */
    fun getFriends(token: String): LiveData<DataState<List<UserRelation>?>> {
        return relationWebSource.getFriends(token)
    }

    companion object {
        //单例模式
        @JvmStatic
        @Volatile
        var instance: FriendsRepository? = null
            get() {
                if (field == null) {
                    field = FriendsRepository()
                }
                return field
            }
            private set
    }

}