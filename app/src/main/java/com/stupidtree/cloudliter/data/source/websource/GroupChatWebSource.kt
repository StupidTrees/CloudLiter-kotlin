package com.stupidtree.cloudliter.data.source.websource

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.gson.JsonObject
import com.stupidtree.cloudliter.data.model.GroupChat
import com.stupidtree.cloudliter.data.source.websource.service.GroupChatService
import com.stupidtree.cloudliter.data.source.websource.service.codes
import com.stupidtree.cloudliter.ui.conversation.group.GroupMemberEntity
import com.stupidtree.cloudliter.utils.JsonUtils
import com.stupidtree.component.data.DataState
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.component.web.BaseWebSource
import com.stupidtree.component.web.LiveDataCallAdapter
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GroupChatWebSource : BaseWebSource<GroupChatService>(Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
        .baseUrl("http://hita.store:3000").build()) {
    override fun getServiceClass(): Class<GroupChatService> {
        return GroupChatService::class.java
    }


    fun createGroupChat(token: String, name: String, userList: List<String>): LiveData<DataState<String?>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.createGroup(token, name, userList)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState(DataState.STATE.SUCCESS)
                    codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    fun renameGroupChat(token: String, name: String,groupId: String): LiveData<DataState<String?>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.renameGroup(token,name,groupId)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState(DataState.STATE.SUCCESS)
                    codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    fun quitGroupChat(token: String, groupId: String): LiveData<DataState<String?>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.quitGroup(token,groupId)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState(DataState.STATE.SUCCESS)
                    codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    fun destroyGroupChat(token: String, groupId: String): LiveData<DataState<String?>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.destroyGroup(token,groupId)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState(DataState.STATE.SUCCESS)
                    codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    fun getGroupInfo(token: String, groupId: String): LiveData<DataState<GroupChat>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.getGroupEntity(token,groupId)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map input.data?.let { DataState(it) }
                    codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    fun getAllGroupMembers(token: String, groupId: String): LiveData<DataState<List<GroupMemberEntity>>> {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.getAllMembers(token, groupId)) { input ->
            if (input == null) {
                return@map DataState(DataState.STATE.FETCH_FAILED)
            } else {
                when (input.code) {
                    codes.SUCCESS -> return@map DataState(input.data?: listOf())
                    codes.TOKEN_INVALID -> return@map DataState(DataState.STATE.TOKEN_INVALID)
                    else -> return@map DataState(DataState.STATE.FETCH_FAILED, input.message)
                }
            }
        }
    }

    /**
     * 更换头像
     * @param token 令牌
     * @param file 图片请求包
     * @return 返回
     */
    fun changeAvatar(token: String, groupId: String,file: MultipartBody.Part): LiveData<DataState<String?>> {
        return Transformations.map(service.uploadAvatar(file, token,groupId)) { input ->
            if (input == null) {
                return@map DataState<String?>(DataState.STATE.FETCH_FAILED)
            }
            when (input.code) {
                codes.SUCCESS -> return@map DataState(DataState.STATE.SUCCESS)
                codes.TOKEN_INVALID -> return@map DataState<String?>(DataState.STATE.TOKEN_INVALID)
                else -> return@map DataState<String?>(DataState.STATE.FETCH_FAILED, input.message)
            }
        }
    }

    companion object {
        //单例模式
        @Volatile
        var instance: GroupChatWebSource? = null
            get() {
                if (field == null) {
                    field = GroupChatWebSource()
                }
                return field
            }
            private set
    }
}