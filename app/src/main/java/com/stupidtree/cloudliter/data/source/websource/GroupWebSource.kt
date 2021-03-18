package com.stupidtree.cloudliter.data.source.websource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.data.source.websource.service.GroupService
import com.stupidtree.cloudliter.data.source.websource.service.codes
import com.stupidtree.component.data.DataState
import com.stupidtree.component.web.BaseWebSource
import com.stupidtree.component.web.LiveDataCallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GroupWebSource : BaseWebSource<GroupService>(Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
        .baseUrl("http://hita.store:3000").build()) {
    override fun getServiceClass(): Class<GroupService> {
        return GroupService::class.java
    }

    /**
     * 获取我的所有消息
     *
     * @param token 令牌
     * @return 查询结果
     */
    fun queryMyGroups(token: String?): LiveData<DataState<List<RelationGroup>?>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.queryMyGroups(token)) { input ->
            if (input == null) {
                return@map DataState<List<RelationGroup>?>(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState<List<RelationGroup>?>(input.data!!)
                    codes.TOKEN_INVALID -> return@map DataState<List<RelationGroup>?>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<List<RelationGroup>?>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    /**
     * 获取添加结果
     *
     * @param token 令牌
     * @return 查询结果
     */
    fun addMyGroups(token: String?, groupName: String?): LiveData<DataState<String?>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.addMyGroups(token, groupName)) { input ->
            if (input == null) {
                return@map DataState<String?>(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> {
                        return@map DataState(input.data, DataState.STATE.SUCCESS)
                    }
                    codes.TOKEN_INVALID -> {
                        return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    }
                    codes.GROUP_NAME_EXIST -> return@map DataState<String?>(DataState.STATE.SPECIAL, input.message)
                    else -> {
                        return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                    }
                }
            }
        }
    }

    /**
     * 获取删除结果
     *
     * @param token 令牌
     * @return 查询结果
     */
    fun deleteMyGroups(token: String?, groupName: String?): LiveData<DataState<String?>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.deleteMyGroups(token, groupName)) { input->
            Log.e("her", "！")
            if (input == null) {
                return@map DataState<String?>(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> {
                         return@map DataState(input.data, DataState.STATE.SUCCESS)
                    }
                    codes.TOKEN_INVALID -> {
                        return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    }
                    codes.GROUP_NAME_EXIST -> return@map DataState<String?>(DataState.STATE.SPECIAL, input.message)
                    else -> {
                        return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                    }
                }
            }
        }
    }



    /**
     * 重命名分组
     *
     * @param token 令牌
     * @return 查询结果
     */
    fun renameGroup(token: String, groupId:String,name:String): LiveData<DataState<String?>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.renameGroup(token,groupId,name)) { input->
            Log.e("her", "！")
            if (input == null) {
                return@map DataState<String?>(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> {
                        return@map DataState(input.data, DataState.STATE.SUCCESS)
                    }
                    codes.TOKEN_INVALID -> {
                        return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                    }
                    codes.GROUP_NAME_EXIST -> return@map DataState<String?>(DataState.STATE.SPECIAL, input.message)
                    else -> {
                        return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
                    }
                }
            }
        }
    }


    /**
     * 为好友分配分组
     * @param token 令牌
     * @param friendId 关系id
     * @param groupId 分组Id
     * @return 操作结果
     */
    fun assignGroup(token: String?, friendId: String?, groupId: String?): LiveData<DataState<*>> {
        return Transformations.map(service.assignGroup(token, friendId, groupId)) { input: ApiResponse<Any?>? ->
            if (input == null) {
                return@map DataState<Any>(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState(input.data, DataState.STATE.SUCCESS)
                    codes.TOKEN_INVALID -> return@map DataState<Any>(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState<Any>(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    companion object {
        //单例模式
        @Volatile
        var instance: GroupWebSource? = null
            get() {
                if (field == null) {
                    field = GroupWebSource()
                }
                return field
            }
            private set
    }
}