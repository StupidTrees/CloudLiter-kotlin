package com.stupidtree.cloudliter.ui.main.contact.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.repository.FriendsRepository
import com.stupidtree.cloudliter.data.repository.FriendsRepository.Companion.instance
import com.stupidtree.cloudliter.data.repository.GroupRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.Trigger

/**
 * 层次：ViewModel
 * 联系人页面Fragment所绑定的ViewModel
 */
class ContactGroupViewModel : ViewModel() {//                        return Transformations.switchMap(groupRepository.queryMyGroups(user.getToken()), input1 -> {
//                            List<UserRelation> res = new LinkedList<>();
//                            if(input1.getState()== DataState.STATE.SUCCESS){
//                                for(RelationGroup rg: input1.getData()){
//                                    res.add(UserRelation.getLabelInstance(rg));
//                                }
//                                return new MutableLiveData<>(new DataState<>(res));
//                            }
//                            return new MutableLiveData<>(new DataState<>(DataState.STATE.FETCH_FAILED));
//                        });
//switchMap的作用是
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
                        val user = localUserRepository.loggedInUser
                        if (!user.isValid) {
                            return@switchMap MutableLiveData(DataState<List<UserRelation>?>(DataState.STATE.NOT_LOGGED_IN))
                        } else {
//                        return Transformations.switchMap(groupRepository.queryMyGroups(user.getToken()), input1 -> {
//                            List<UserRelation> res = new LinkedList<>();
//                            if(input1.getState()== DataState.STATE.SUCCESS){
//                                for(RelationGroup rg: input1.getData()){
//                                    res.add(UserRelation.getLabelInstance(rg));
//                                }
//                                return new MutableLiveData<>(new DataState<>(res));
//                            }
//                            return new MutableLiveData<>(new DataState<>(DataState.STATE.FETCH_FAILED));
//                        });
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
     * 仓库区
     */
    //仓库1：好友仓库
    private val friendsRepository: FriendsRepository? = instance

    //仓库2：本地用户仓库
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance()

    //仓库3：好友分组仓库
    private val groupRepository: GroupRepository = GroupRepository.getInstance()

    /**
     * 开始刷新列表数据
     */
    fun startFetchData() {
        listController.value = Trigger.getActioning()
    }

}