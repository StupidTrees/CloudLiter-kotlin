package com.stupidtree.cloudliter.data.repository

import androidx.lifecycle.LiveData
import com.stupidtree.cloudliter.data.model.RelationEvent
import com.stupidtree.cloudliter.data.model.RelationEvent.ACTION
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.source.RelationWebSource
import com.stupidtree.cloudliter.ui.base.DataState

class RelationRepository {
    /**
     * 数据源
     */
    //数据源：网络类型
    var relationWebSource: RelationWebSource = RelationWebSource.instance!!

    /**
     * 判断某用户是否是本用户的好友
     *
     * @param token 令牌
     * @param id1   （非必须）本用户id
     * @param id2   （必须）目标用户id
     * @return Boolean型判断结果
     */
    fun isMyFriend(token: String, id1: String, id2: String): LiveData<DataState<Boolean?>> {
        return relationWebSource.isFriends(token, id1, id2)
    }

    /**
     * 获取关系对象
     * @param token 令牌
     * @param friendId 朋友
     * @return 结果
     */
    fun queryRelation(token: String, friendId: String): LiveData<DataState<UserRelation?>> {
        return relationWebSource.queryRelation(token, friendId)
    }

    /**
     * 更改用户备注
     *
     * @param token    令牌
     * @param remark 新备注
     * @return 操作结果
     */
    fun changeRemark(token: String, remark: String, friend_id: String): LiveData<DataState<String?>> {
        return relationWebSource.changeRemark(token, remark, friend_id)
    }

    /**
     * 发送好友请求
     * @param token 令牌
     * @param friendId 对方id
     * @return 操作结果
     */
    fun sendFriendRequest(token: String, friendId: String): LiveData<DataState<String?>> {
        return relationWebSource.sendFriendRequest(token, friendId)
    }

    /**
     * 响应好友请求
     * @param token 令牌
     * @param eventId 事件id
     * @param action 操作
     * @return 操作结果
     */
    fun responseFriendRequest(token: String, eventId: String, action: ACTION): LiveData<DataState<*>> {
        return relationWebSource.responseFriendRequest(token, eventId, action)
    }

    /**
     * 获得所有和我有关的好友请求
     * @param token 令牌
     * @return 请求结果
     */
    fun queryMine(token: String): LiveData<DataState<List<RelationEvent>?>> {
        return relationWebSource.queryMine(token)
    }

    /**
     * 删除好友
     * @param token 令牌
     * @param friendId 好友id
     * @return 操作结果
     */
    fun deleteFriend(token: String, friendId: String): LiveData<DataState<*>> {
        return relationWebSource.deleteFriend(token, friendId)
    }

    /**
     * 获得未读好友事件数目
     * @param token 令牌
     * @return 查询结果
     */
    fun countUnread(token: String): LiveData<DataState<Int?>> {
        return relationWebSource.countUnread(token)
    }

    /**
     * 将我的所有好友事件标记为已读
     * @param token 令牌
     * @return 操作结果
     */
    fun markRead(token: String): LiveData<DataState<Any?>> {
        return relationWebSource.markRead(token)
    }

    companion object {
        @JvmStatic
        var instance: RelationRepository? = null
            get() {
                if (field == null) {
                    field = RelationRepository()
                }
                return field
            }
            private set
    }

}