package com.stupidtree.cloudliter.data.repository

import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.data.source.websource.GroupWebSource
import com.stupidtree.cloudliter.ui.base.DataState

/**
 * 层次：Repository
 * 好友分组的仓库
 */
class GroupRepository internal constructor() {
    //数据源1：网络数据源，分组网络数据
    var groupWebSource: GroupWebSource = GroupWebSource.instance!!

    /**
     * 获取我的所有好友分组
     * @param token 令牌
     * @return 查询结果
     */
    fun queryMyGroups(token: String): LiveData<DataState<List<RelationGroup>?>> {
        return groupWebSource.queryMyGroups(token)
    }

    /**
     * 为好友分配分组
     * @param token 令牌
     * @param friendId 关系id
     * @param groupId 分组Id
     * @return 操作结果
     */
    fun assignGroup(token: String, friendId: String, groupId: String): LiveData<DataState<*>> {
        return groupWebSource.assignGroup(token, friendId, groupId)
    }

    /**
     * 获取添加情况
     * @param token 令牌
     * @return 查询结果
     */
    fun addMyGroups(token: String, groupName: String): LiveData<DataState<String?>> {
        return groupWebSource.addMyGroups(token, groupName)
    }

    /**
     * 获取删除情况
     * @param token 令牌
     * @return 查询结果
     */
    fun deleteMyGroups(token: String, groupName: String): LiveData<DataState<String?>> {
        return groupWebSource.deleteMyGroups(token, groupName)
    }
    /**
     * 获取删除情况
     * @param token 令牌
     * @return 查询结果
     */
    fun renameMyGroups(token: String, groupId:String, name:String): LiveData<DataState<String?>> {
        return groupWebSource.renameGroup(token,groupId,name)
    }
    companion object {
        //也是单例模式
        var instance: GroupRepository? = null
            get() {
                if (field == null) {
                    field = GroupRepository()
                }
                return field
            }
            private set
    }

}