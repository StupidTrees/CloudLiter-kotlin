package com.stupidtree.cloudliter.ui.main.contact

import android.app.Application
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.repository.FriendsRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.RelationRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.Trigger
import java.util.*

/**
 * 层次：ViewModel
 * 联系人页面Fragment所绑定的ViewModel
 */
class ContactViewModel(application: Application) : AndroidViewModel(application) {//switchMap的作用是
    //当ListController发生数据变更时，将用如下定义的方式更新listData的value
    /**
     * 获取联系人列表的LiveData
     *
     * @return 结果呗
     */
    /**
     * 数据区
     */
    //数据本体:联系人列表
    var listData: LiveData<DataState<List<UserRelation>?>>? = null
        get() {
            if (field == null) {
                //switchMap的作用是
                //当ListController发生数据变更时，将用如下定义的方式更新listData的value
                field = Transformations.switchMap(listController) { input: Trigger ->
                    if (input.isActioning) {
                        val user = localUserRepository.getLoggedInUser()
                        if (!user.isValid) {
                            return@switchMap MutableLiveData(DataState<List<UserRelation>?>(DataState.STATE.NOT_LOGGED_IN))
                        } else {
                            return@switchMap friendsRepository!!.getFriends(user.token!!)
                        }
                    }
                    MutableLiveData(DataState<List<UserRelation>?>(DataState.STATE.NOTHING))
                }
            }
            return field
        }
        private set

    //Trigger：控制↑的刷新动作
    private val listController = MutableLiveData<Trigger>()

    /**
     * 未读好友事件数
     */
    //数据本体：未读好友事件数
    var unReadLiveData: LiveData<DataState<Int?>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(unReadController) {
                    val userLocal = localUserRepository.getLoggedInUser()
                    if (userLocal.isValid) {
                        return@switchMap relationRepository!!.countUnread(userLocal.token!!)
                    } else {
                        return@switchMap MutableLiveData(DataState<Int?>(DataState.STATE.NOT_LOGGED_IN))
                    }
                }
            }
            return field
        }
        private set

    //Trigger：控制↑的获取
    private val unReadController = MutableLiveData<Trigger>()

    /**
     * 仓库区
     */
    //仓库1：好友仓库
    private val friendsRepository: FriendsRepository?

    //仓库2：关系仓库
    private val relationRepository: RelationRepository?

    //仓库2：本地用户仓库
    private val localUserRepository: LocalUserRepository

    /**
     * 开始刷新列表数据
     */
    fun startFetchData() {
        listController.value = Trigger.actioning
    }

    /**
     * 开始获取未读事件数目
     */
    fun startFetchUnread() {
        unReadController.value = Trigger.actioning
    }

    init {
        friendsRepository = FriendsRepository.instance
        localUserRepository = LocalUserRepository.getInstance(application)
        relationRepository = RelationRepository.getInstance(application)
    }
}