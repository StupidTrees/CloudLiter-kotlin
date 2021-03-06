package com.stupidtree.cloudliter.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.RelationEvent
import com.stupidtree.cloudliter.data.model.RelationEvent.ACTION
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.source.websource.RelationWebSource
import com.stupidtree.cloudliter.ui.base.DataState

class RelationRepository(application: Application) {
    /**
     * 数据源
     */
    //数据源：网络类型
    private var relationWebSource: RelationWebSource = RelationWebSource.instance!!

    private var chatMessageDao = AppDatabase.getDatabase(application).chatMessageDao()

    private var relationDao = AppDatabase.getDatabase(application).userRelationDao()


    /**
     * 获取关系对象
     * @param token 令牌
     * @param friendId 朋友
     * @return 结果
     */
    fun queryRelation(token: String, friendId: String): LiveData<DataState<UserRelation?>> {
        val result =  MediatorLiveData<DataState<UserRelation?>>()
        val localRes = relationDao.queryRelation(friendId)
        result.addSource(localRes){
            if(it!=null){
                result.value = DataState(it,DataState.STATE.SUCCESS)
            }else{
                result.value = DataState(it,DataState.STATE.NOT_EXIST)
            }
        }
        val webSource = relationWebSource.queryRelation(token, friendId)
        result.addSource(webSource){webResult->
            if(webResult.state==DataState.STATE.SUCCESS&&webResult.data!=null){
                Thread{
                    relationDao.saveRelation(webResult.data!!)
                }.start()
            }else if(webResult.state==DataState.STATE.NOT_EXIST){
                Thread{
                    relationDao.deleteRelation(friendId)
                }.start()
            }
        }
        return result
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
        return Transformations.map(relationWebSource.deleteFriend(token, friendId)) { input ->
            //删除成功，则清空本地缓存的聊天记录
            if (input.state == DataState.STATE.SUCCESS) {
                Thread {
                    chatMessageDao.clearConversation(friendId)
                }.start()
            }
            input
        }
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
        fun getInstance(application: Application): RelationRepository {
            if (instance == null) {
                instance = RelationRepository(application)
            }
            return instance!!
        }
    }

}