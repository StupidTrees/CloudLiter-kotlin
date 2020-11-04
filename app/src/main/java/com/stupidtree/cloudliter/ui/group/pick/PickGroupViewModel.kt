package com.stupidtree.cloudliter.ui.group.pick

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.data.repository.GroupRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.Trigger

class PickGroupViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 数据区
     */
    var listData: LiveData<DataState<List<RelationGroup>?>>? = null
        get() {
            if (field == null) {
                listData = Transformations.switchMap(listController) {
                    val userLocal = localUserRepository.getLoggedInUser()
                    if (userLocal.isValid) {
                        return@switchMap groupRepository.queryMyGroups(userLocal.token!!)
                    }
                    MutableLiveData(DataState<List<RelationGroup>?>(DataState.STATE.NOT_LOGGED_IN))
                }
            }
            return field
        }
    var listController = MutableLiveData<Trigger>()

    /**
     * 仓库区
     */
    var groupRepository: GroupRepository
    var localUserRepository: LocalUserRepository

    /**
     * 开始刷新
     */
    fun startRefresh() {
        listController.value = Trigger.actioning
    }

    init {
        groupRepository = GroupRepository.instance!!
        localUserRepository = LocalUserRepository.getInstance(application)
    }
}