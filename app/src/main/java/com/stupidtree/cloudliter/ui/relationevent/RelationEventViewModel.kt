package com.stupidtree.cloudliter.ui.relationevent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.stupidtree.cloudliter.data.model.RelationEvent
import com.stupidtree.cloudliter.data.model.RelationEvent.ACTION
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.RelationRepository
import com.stupidtree.cloudliter.data.repository.RelationRepository.Companion.instance
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.Trigger
import java.util.*

class RelationEventViewModel : ViewModel() {
    /**
     * 数据区
     */
    //数据本体：列表数据
    var listData: LiveData<DataState<List<RelationEvent>?>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(listDataController) { input: Trigger ->
                    if (!input.isActioning) {
                        return@switchMap MutableLiveData(DataState<List<RelationEvent>?>(DataState.STATE.NOTHING))
                    }
                    val userLocal = localUserRepository.loggedInUser
                    if (userLocal.isValid) {
                        return@switchMap relationRepository!!.queryMine(userLocal.token!!)
                    } else {
                        return@switchMap MutableLiveData(DataState<List<RelationEvent>?>(DataState.STATE.NOT_LOGGED_IN))
                    }
                }
            }
            return field
        }
        private set

    //Trigger：控制↑的刷新
    private val listDataController = MutableLiveData<Trigger>()

    //状态数据：同意好友请求
    var responseResult: LiveData<DataState<*>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(responseFriendTrigger) { input: ResponseFriendTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.loggedInUser
                        if (userLocal.isValid) {
                            return@switchMap relationRepository!!.responseFriendRequest(userLocal.token!!, input.eventId, input.action)
                        } else {
                            return@switchMap MutableLiveData<DataState<*>>(DataState<Any>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<*>>(DataState<Any>(DataState.STATE.NOTHING))
                }
            }
            return field
        }
        private set
    private val responseFriendTrigger = MutableLiveData<ResponseFriendTrigger>()

    //状态数据：标记已读的结果
    val markReadResult: LiveData<DataState<Any?>>? = null
        get() = field
                ?: Transformations.switchMap(markReadController) { input: Trigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.loggedInUser
                        if (userLocal.isValid) {
                            return@switchMap relationRepository!!.markRead(userLocal.token!!)
                        } else {
                            return@switchMap MutableLiveData(DataState<Any?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    return@switchMap MutableLiveData(DataState<Any?>(DataState.STATE.NOTHING))
                }

    //Trigger：控制↑的进行
    private val markReadController = MutableLiveData<Trigger>()

    /**
     * 仓库区
     */
    var localUserRepository: LocalUserRepository
    var relationRepository: RelationRepository?

    /**
     * 开始标记已读
     */
    fun startMarkRead() {
        markReadController.value = Trigger.getActioning()
    }

    fun startRefresh() {
        listDataController.value = Trigger.getActioning()
    }

    val localUserId: String?
        get() = localUserRepository.loggedInUser.id

    fun responseFriendRequest(eventId: String, action: ACTION) {
        responseFriendTrigger.value = ResponseFriendTrigger.getActioning(eventId, action)
    }

    init {
        localUserRepository = LocalUserRepository.getInstance()
        relationRepository = instance
    }
}