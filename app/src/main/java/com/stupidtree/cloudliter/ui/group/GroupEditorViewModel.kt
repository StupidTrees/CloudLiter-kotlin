package com.stupidtree.cloudliter.ui.group

import android.app.Application
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.data.repository.GroupRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.StringTrigger
import com.stupidtree.cloudliter.ui.base.Trigger
import java.util.*

class GroupEditorViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 数据区
     */
    //数据本体
    var listData: LiveData<DataState<List<RelationGroup>?>>? = null
        get() {
            if (field == null) {
                listData = Transformations.switchMap(listDataController) { input: Trigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap groupRepository.queryMyGroups(userLocal.token!!)
                        } else {
                            return@switchMap MutableLiveData(DataState<List<RelationGroup>?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData(DataState<List<RelationGroup>?>(DataState.STATE.NOTHING))
                }
            }
            return field
        }

    //Trigger:控制↑的刷新
    var listDataController = MutableLiveData<Trigger>()

    //状态数据：添加分组的结果
    var addGroupResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                //也是一样的
                addGroupResult = Transformations.switchMap(addGroupController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap groupRepository.addMyGroups(userLocal.token!!, input.data!!)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制添加请求的发送
    var addGroupController = MutableLiveData<StringTrigger>()

    //状态数据：删除分组的结果
    var deleteGroupResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                //也是一样的
                deleteGroupResult = Transformations.switchMap(deleteGroupController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            //System.out.println("viewmodel stage:func: getDeleteGroupResult return is "+groupRepository.deleteMyGroups(userLocal.getToken(),input.getValue()));
                            return@switchMap groupRepository.deleteMyGroups(userLocal.token!!, input.data!!)
                        } else {
                            //System.out.println("viewmodel stage: no log in,func: getDeleteGroupResult return is "+groupRepository.deleteMyGroups(userLocal.getToken(),input.getValue()));
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制删除请求的发送
    var deleteGroupController = MutableLiveData<StringTrigger>()


    //状态数据：重命名分组的结果
    var renameGroupResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                renameGroupResult = Transformations.switchMap(renameGroupController) { input ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            //System.out.println("viewmodel stage:func: getrenameGroupResult return is "+groupRepository.renameMyGroups(userLocal.getToken(),input.getValue()));
                            return@switchMap groupRepository.renameMyGroups(userLocal.token!!, input.groupId!!,input.name!!)
                        } else {
                            //System.out.println("viewmodel stage: no log in,func: getrenameGroupResult return is "+groupRepository.renameMyGroups(userLocal.getToken(),input.getValue()));
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData()
                }
            }
            return field!!
        }

    //Trigger：控制重命名请求的发送
    var renameGroupController = MutableLiveData<RenameGroupTrigger>()


    /**
     * 仓库区
     */
    //好友分组
    var groupRepository: GroupRepository

    //本地用户
    var localUserRepository: LocalUserRepository


    /**
     * 发起添加好友分组请求
     * @param group 新组名
     */
    fun startAddGroup(group: String) {
        addGroupController.value = StringTrigger.getActioning(group)
    }

    /**
     * 发起删除好友分组请求
     * @param group 新组名
     */
    fun startDeleteGroup(group: String) {
        deleteGroupController.value = StringTrigger.getActioning(group)
    }

    /**
     * 发起重命名好友分组请求
     * @param group 新组名
     */
    fun startRenameGroup(groupId: String,newName:String) {
        renameGroupController.value = RenameGroupTrigger.getActioning(groupId,newName)
    }


    /**
     * 开始刷新页面
     */
    fun startRefresh() {
        listDataController.value = Trigger.actioning
    }



    init {
        groupRepository = GroupRepository.instance!!
        localUserRepository = LocalUserRepository.getInstance(application)
    }
}