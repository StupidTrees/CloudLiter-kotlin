package com.stupidtree.cloudliter.ui.relation

import android.app.Application
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.RelationRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.StringTrigger

class RelationViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 数据区
     */
    //数据本体：用户关系
    var relationData: LiveData<DataState<UserRelation?>>? = null
        get() {
            if (field == null) {
                relationData = Transformations.switchMap(relationQueryController) { input: RelationQueryTrigger ->
                    if (input.isActioning) {
                        if (localUserRepository.isUserLoggedIn) {
                            return@switchMap relationRepository.queryRelation(localUserRepository.getLoggedInUser().token!!,
                                    input.friendId)
                        } else {
                            return@switchMap MutableLiveData(DataState<UserRelation?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData(DataState<UserRelation?>(DataState.STATE.NOTHING))
                }
            }
            return field!!
        }

    //Trigger：控制↑的刷新
    var relationQueryController = MutableLiveData<RelationQueryTrigger>()

    //状态数据：更改签名的结果
    var changeRemarkResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                //也是一样的
                changeRemarkResult = Transformations.switchMap(changeRemarkController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid && relationData!!.value != null) {
                            println("friend id is" + relationData!!.value!!.data!!.friendId)
                            return@switchMap relationRepository.changeRemark(userLocal.token!!, input.data, relationData!!.value!!.data!!.friendId!!)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制更改签名请求的发送，其中携带了新昵称字符串
    var changeRemarkController = MutableLiveData<StringTrigger>()

    /**
     * 仓库区
     */
    var relationRepository: RelationRepository
    var localUserRepository: LocalUserRepository


    fun startFetchRelationData(friendId: String) {
        relationQueryController.value = RelationQueryTrigger.getActioning(friendId)
    }

    fun startChangeRemark(newRemark: String) {
        changeRemarkController.value = StringTrigger.getActioning(newRemark)
    }

    init {
        localUserRepository = LocalUserRepository.getInstance(application)
        relationRepository = RelationRepository.instance!!
    }
}