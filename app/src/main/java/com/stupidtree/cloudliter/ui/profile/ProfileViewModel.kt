package com.stupidtree.cloudliter.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.RelationGroup
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.repository.GroupRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.data.repository.ProfileRepository
import com.stupidtree.cloudliter.data.repository.ProfileRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.RelationRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.StringTrigger
import java.util.*

/**
 * 层次：ViewModel
 * 其他用户（好友、搜索结果等）的资料页面绑定的ViewModel
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */
    //用户资料仓库
    private val repository: ProfileRepository = getInstance(application)

    //用户关系仓库
    private val relationRepository: RelationRepository = RelationRepository.getInstance(application)

    //本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)

    //好友分组仓库
    private val groupRepository: GroupRepository = GroupRepository.instance!!

    /**
     * 数据区
     */
    //数据本体：我和这个用户的好友关系
    var relationLiveData: LiveData<DataState<UserRelation?>>? = null
        get() {
            if (field == null) {
                relationLiveData = Transformations.switchMap(profileController) { input: StringTrigger ->
                    val user = localUserRepository.getLoggedInUser()
                    if (input.isActioning) {
                        if (user.isValid) {

                            //如果就是自己
                            if (user.id == input.data) {
                                return@switchMap MutableLiveData(DataState<UserRelation?>(DataState.STATE.SPECIAL))
                            } else {
                                //通知用户资料仓库进行好友判别
                                return@switchMap relationRepository.queryRelation(user.token!!, input.data!!)
                            }
                        } else {
                            return@switchMap MutableLiveData(DataState<UserRelation?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData(DataState(DataState.STATE.NOTHING))
                }
            }
            return field!!
        }

    //数据本体：用户词云
    var wordCloudLiveData: LiveData<DataState<HashMap<String, Float?>?>>? = null
        get() {
            if (field == null) {
                wordCloudLiveData = Transformations.switchMap(profileController) { input: StringTrigger ->
                    val user = localUserRepository.getLoggedInUser()
                    if (input.isActioning) {
                        if (user.isValid) {
                            return@switchMap repository.getUserWordCloud(user.token!!, input.data)
                        } else {
                            return@switchMap MutableLiveData(DataState<HashMap<String, Float?>?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData(DataState(DataState.STATE.NOTHING))
                }
            }
            return field!!
        }

    //Trigger：控制↑三个的刷新
    private var profileController = MutableLiveData<StringTrigger>()

    //状态数据：添加好友的结果
    var makeFriendsResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                makeFriendsResult = Transformations.switchMap(makeFriendsController) { input: StringTrigger ->
                    val user = localUserRepository.getLoggedInUser()
                    if (input.isActioning) {
                        if (user.isValid) {
                            //也是通过这个仓库进行好友建立
                            return@switchMap relationRepository.sendFriendRequest(user.token!!, input.data)
                            // return relationRepository.makeFriends(Objects.requireNonNull(user.getToken()),input.getData());
                        } else {
                            return@switchMap MutableLiveData<DataState<String?>>(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<String?>>()
                }
            }
            return field!!
        }

    //Trigger：控制添加好友的请求
    private var makeFriendsController = MutableLiveData<StringTrigger>()

    //状态数据：更改签名的结果
    var changeRemarkResult: LiveData<DataState<String?>>? = null
        get() {
            if (field == null) {
                //也是一样的
                changeRemarkResult = Transformations.switchMap(changeRemarkController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid && relationLiveData!!.value != null) {
                            // System.out.println("friend id is" + relationLiveData.getValue().getData().getFriendId());
                            return@switchMap relationRepository.changeRemark(userLocal.token!!, input.data, relationLiveData!!.value!!.data!!.friendId!!)
                        } else {
                            return@switchMap MutableLiveData(DataState<String?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData()
                }
            }
            return field!!
        }

    //Trigger：控制更改签名请求的发送，其中携带了新昵称字符串
    var changeRemarkController = MutableLiveData<StringTrigger>()

    //状态数据：删除好友的结果
    var deleteFriendResult: LiveData<DataState<*>>? = null
        get() {
            if (field == null) {
                deleteFriendResult = Transformations.switchMap(deleteFriendController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid) {
                            return@switchMap relationRepository.deleteFriend(userLocal.token!!, input.data)
                        } else {
                            return@switchMap MutableLiveData<DataState<*>>(DataState<Any>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData(DataState<Any>(DataState.STATE.NOTHING))
                }
            }
            return field!!
        }

    //Trigger：控制删除好友
    var deleteFriendController = MutableLiveData<StringTrigger>()

    //状态数据：分配好友分组的结果
    var assignGroupResult: LiveData<DataState<*>>? = null
        get() {
            if (field == null) {
                assignGroupResult = Transformations.switchMap(assignGroupController) { input: StringTrigger ->
                    if (input.isActioning) {
                        val userLocal = localUserRepository.getLoggedInUser()
                        if (userLocal.isValid && userId != null) {
                            return@switchMap groupRepository.assignGroup(userLocal.token!!, userId!!, input.data)
                        } else {
                            return@switchMap MutableLiveData<DataState<*>>(DataState<Any>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData<DataState<*>>(DataState<Any>(DataState.STATE.NOTHING))
                }
            }
            return field!!
        }

    //Trigger:控制分配好友分组
    private var assignGroupController = MutableLiveData<StringTrigger>()


    //从用户资料仓库中拉取数据
    var userProfileLiveData: LiveData<DataState<UserProfile?>>? = null
        get() {
            if (field == null) {
                userProfileLiveData = Transformations.switchMap(profileController) { input: StringTrigger ->
                    val user = localUserRepository.getLoggedInUser()
                    if (input.isActioning) {
                        if (user.isValid) {
                            //从用户资料仓库中拉取数据
                            return@switchMap repository.getUserProfile(input.data, user.token!!)
                        } else {
                            return@switchMap MutableLiveData(DataState<UserProfile?>(DataState.STATE.NOT_LOGGED_IN))
                        }
                    }
                    MutableLiveData(DataState(DataState.STATE.NOTHING))
                }
            }
            return field
        }

    /**
     * 开始页面刷新
     *
     * @param id 这个页面是谁的资料
     */
    fun startRefresh(id: String) {
        profileController.value = StringTrigger.getActioning(id)
    }

    /**
     * 开始建立朋友关系
     *
     * @param id 这个页面是谁的
     */
    fun startMakingFriends(id: String) {
        makeFriendsController.value = StringTrigger.getActioning(id)
    }

    /**
     * 开始删除好友
     *
     * @param id 好友id
     */
    fun startDeletingFriend(id: String) {
        deleteFriendController.value = StringTrigger.getActioning(id)
    }

    /**
     * 获取该用户id
     *
     * @return
     */
    val userId: String?
        get() {
            userProfileLiveData?.let {
                it.value?.let { it1 ->
                    return it1.data?.id
                }
            }
            return null
        }

    /**
     * 请求更换备注
     *
     * @param newRemark 备注
     */
    fun startChangeRemark(newRemark: String) {
        if (relationLiveData!!.value != null) {
            changeRemarkController.value = StringTrigger.getActioning(newRemark)
        }
    }

    /**
     * 请求设置分组
     * @param group 分组
     */
    fun startAssignGroup(group: RelationGroup) {
        group.id?.let {
            assignGroupController.value = StringTrigger.getActioning(it)
        }
    }

    fun logout(context: Context) {
        localUserRepository.logout(context)
    }

    fun getUserRelation(): UserRelation? {

        return if (relationLiveData!!.value != null) {
            relationLiveData!!.value!!.data
        } else {
            null
        }
    }

    fun getUserProfile(): UserProfile? {
        if (userProfileLiveData != null && userProfileLiveData!!.value != null) {
            return userProfileLiveData!!.value!!.data
        }
        return null
    }

    fun getUserLocal(): UserLocal?{
            val userLocal = localUserRepository.getLoggedInUser()
            return if (userLocal.isValid) userLocal else null
        }

}