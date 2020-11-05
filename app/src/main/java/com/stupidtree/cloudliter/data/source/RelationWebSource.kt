package com.stupidtree.cloudliter.data.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.RelationEvent
import com.stupidtree.cloudliter.data.model.RelationEvent.ACTION
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.service.LiveDataCallAdapter
import com.stupidtree.cloudliter.service.RelationService
import com.stupidtree.cloudliter.service.codes
import com.stupidtree.cloudliter.ui.base.DataState
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 层次：DataSource
 * 用户关系的数据源
 * 类型：网络数据
 * 数据：异步读，异步写
 */
class RelationWebSource : BaseWebSource<RelationService>(Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
        .baseUrl("http://hita.store:3000").build()) {
    override fun getServiceClass(): Class<RelationService> {
        return RelationService::class.java
    }

    /**
     * 获取好友列表
     *
     * @param token 登录状态token
     * @return 朋友列表的LiveData
     */
    fun getFriends(token: String): LiveData<DataState<List<UserRelation>?>> {
        return Transformations.map(service.getFriends(token)) { input ->
            Log.e("getFriends", input.toString())
            if (null == input) {
                return@map DataState<List<UserRelation>?>(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState<List<UserRelation>?>(input.data!!)
                    codes.TOKEN_INVALID -> return@map DataState<List<UserRelation>?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<List<UserRelation>?>(DataState.STATE.FETCH_FAILED)
                }
            }
        }
    }

    /**
     * 判断是否为好友
     * @param token 令牌
     * @param userId 我的id
     * @param friend 他的id
     * @return 操作结果
     */
    fun isFriends(token: String, userId: String, friend: String): LiveData<DataState<Boolean?>> {
        return Transformations.map(service.isFriends(token, userId, friend)) { input ->
            if (input == null) {
                return@map DataState<Boolean?>(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState(input.data)
                    codes.TOKEN_INVALID -> return@map DataState<Boolean?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<Boolean?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    /**
     * 获取我和朋友的关系对象
     * @param token 令牌
     * @param friendId 朋友的id
     * @return 操作结果
     */
    fun queryRelation(token: String, friendId: String): LiveData<DataState<UserRelation?>> {
        return Transformations.map(service.queryRelation(token, friendId)) { input ->
            if (input == null) {
                return@map DataState<UserRelation?>(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState(input.data)
                    codes.TOKEN_INVALID -> return@map DataState<UserRelation?>(DataState.STATE.TOKEN_INVALID)
                    codes.RELATION_NOT_EXIST -> return@map DataState<UserRelation?>(DataState.STATE.NOT_EXIST)
                    else -> return@map DataState<UserRelation?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    /**
     * 更换备注
     * @param token 令牌
     * @param remark 备注
     * @return 操作结果
     */
    fun changeRemark(token: String, remark: String, friend_id: String): LiveData<DataState<String?>> {
        return Transformations.map(service.changeRemark(friend_id, remark, token)) { input->
            if (input != null) {
                println("input remark is $input")
                when (input.code) {
                    codes.SUCCESS -> {
                        println("SUCCEED")
                        return@map DataState<String?>(DataState.STATE.SUCCESS)
                    }
                    codes.TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
            DataState<String?>(DataState.STATE.FETCH_FAILED)
        }
    }

    /**
     * 发送好友请求
     *
     * @param token 登录状态token
     * @return 操作结果
     */
    fun sendFriendRequest(token: String, friendId: String): LiveData<DataState<String?>> {
        return Transformations.map(service.sendFriendRequest(token, friendId)) { input: ApiResponse<Any?>? ->
            if (null == input) {
                return@map DataState<String?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState<String?>(DataState.STATE.SUCCESS)
                codes.TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID, input.message)
                codes.REQUEST_ALREADY_SENT -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID, "已经发送过申请啦！")
                else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 获得所有和我有关的好友请求
     * @param token 令牌
     * @return 请求结果
     */
    fun queryMine(token: String): LiveData<DataState<List<RelationEvent>?>> {
        return Transformations.map(service.queryMine(token)) { input->
            if (null == input) {
                return@map DataState<List<RelationEvent>?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState<List<RelationEvent>?>(input.data!!)
                codes.TOKEN_INVALID -> return@map DataState<List<RelationEvent>?>(DataState.STATE.TOKEN_INVALID, input.message)
                else -> return@map DataState<List<RelationEvent>?>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 响应好友请求
     *
     * @param token 登录状态token
     * @return 操作结果
     */
    fun responseFriendRequest(token: String, eventId: String, action: ACTION): LiveData<DataState<*>> {
        return Transformations.map(service.responseFriendRequest(token, eventId, action.toString())) { input: ApiResponse<Any?>? ->
            if (null == input) {
                return@map DataState<Any>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState<Any>(DataState.STATE.SUCCESS)
                codes.TOKEN_INVALID -> return@map DataState<Any>(DataState.STATE.TOKEN_INVALID, input.message)
                else -> return@map DataState<Any>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 删除好友
     * @param token 登录状态token
     * @param friendId 好友id
     * @return 操作结果
     */
    fun deleteFriend(token: String, friendId: String): LiveData<DataState<*>> {
        return Transformations.map(service.deleteFriend(token, friendId)) { input: ApiResponse<Any?>? ->
            if (null == input) {
                return@map DataState<Any>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState<Any>(DataState.STATE.SUCCESS)
                codes.TOKEN_INVALID -> return@map DataState<Any>(DataState.STATE.TOKEN_INVALID, input.message)
                else -> return@map DataState<Any>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 获取未读好友事件数目
     * @param token 登录状态token
     * @return 操作结果
     */
    fun countUnread(token: String): LiveData<DataState<Int?>> {
        return Transformations.map(service.countUnread(token)) { input  ->
            if (null == input) {
                return@map DataState<Int?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data)
                codes.TOKEN_INVALID -> return@map DataState<Int?>(DataState.STATE.TOKEN_INVALID, input.message)
                else -> return@map DataState<Int?>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    /**
     * 标记好友事件全部已读
     * @param token 登录状态token
     * @return 操作结果
     */
    fun markRead(token: String): LiveData<DataState<Any?>> {
        return Transformations.map(service.markRead(token)) { input  ->
            if (null == input) {
                return@map DataState<Any?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(input.data)
                codes.TOKEN_INVALID -> return@map DataState<Any?>(DataState.STATE.TOKEN_INVALID, input.message)
                else -> return@map DataState<Any?>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    companion object {
        //单例模式
        @Volatile
        var instance: RelationWebSource? = null
            get() {
                if (field == null) {
                    field = RelationWebSource()
                }
                return field
            }
            private set
    }
}