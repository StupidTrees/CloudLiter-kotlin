package com.stupidtree.cloudliter.data.repository


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.GroupChat
import com.stupidtree.cloudliter.data.source.websource.GroupChatWebSource
import com.stupidtree.cloudliter.ui.conversation.group.GroupMemberEntity
import com.stupidtree.component.data.DataState
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * 层次：Repository
 * 群聊的仓库
 */
class GroupChatRepository internal constructor(application: Application) {
    //数据源1：网络数据源，分组网络数据
    var groupWebSource: GroupChatWebSource = GroupChatWebSource.instance!!
    var conversationDao = AppDatabase.getDatabase(application).conversationDao()

    /**
     * 创建群聊
     */
    fun createGroupChat(token: String, name: String, userList: List<String>): LiveData<DataState<String?>> {
        return groupWebSource.createGroupChat(token, name, userList)
    }

    /**
     *  获取所有群成员信息
     */
    fun getAllGroupMembers(token: String, groupId: String): LiveData<DataState<List<GroupMemberEntity>>> {
        return groupWebSource.getAllGroupMembers(token, groupId)
    }


    fun renameGroup(token: String, name: String, groupId: String): LiveData<DataState<String?>> {
        return groupWebSource.renameGroupChat(token, name, groupId)
    }

    fun getGroupInfo(token: String, groupId: String): LiveData<DataState<GroupChat>> {
        return groupWebSource.getGroupInfo(token, groupId)
    }


    /**
     * 更改用户头像
     *
     * @param token    令牌
     * @param filePath 头像路径
     * @return 操作结果
     */
    fun changeAvatar(token: String, groupId: String, filePath: String): LiveData<DataState<String?>> {
        val file = File(filePath)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("upload", file.name, requestFile)
        //调用网络数据源的服务，上传头像
        return groupWebSource.changeAvatar(token, groupId, body)
    }

    fun quitGroup(token: String, groupId: String): LiveData<DataState<String?>> {
        return Transformations.map(groupWebSource.quitGroupChat(token, groupId)) {
            if (it.state == DataState.STATE.SUCCESS) {
                Thread {
                    conversationDao.deleteGroupConversation(groupId)
                }.start()
            }
            return@map it
        }
    }

    fun destroyGroup(token: String, groupId: String): LiveData<DataState<String?>> {
        return Transformations.map(groupWebSource.destroyGroupChat(token, groupId)) {
            if (it.state == DataState.STATE.SUCCESS) {
                Thread {
                    conversationDao.deleteGroupConversation(groupId)
                }.start()
            }
            return@map it
        }
    }

    companion object {
        //也是单例模式
        var INSTANCE: GroupChatRepository? = null
        fun getInstance(application: Application): GroupChatRepository {
            if (INSTANCE == null) {
                INSTANCE = GroupChatRepository(application)
            }
            return INSTANCE!!
        }
    }

}