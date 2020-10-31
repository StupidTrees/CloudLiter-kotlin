package com.stupidtree.hichat.ui.main.contact.group;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.RelationGroup;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserRelation;
import com.stupidtree.hichat.data.repository.FriendsRepository;
import com.stupidtree.hichat.data.repository.GroupRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.Trigger;

import java.util.LinkedList;
import java.util.List;

/**
 * 层次：ViewModel
 * 联系人页面Fragment所绑定的ViewModel
 */
public class ContactGroupViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体:联系人列表
    private LiveData<DataState<List<UserRelation>>> listData;
    //Trigger：控制↑的刷新动作
    private final MutableLiveData<Trigger> listController = new MutableLiveData<>();


    /**
     * 仓库区
     */
    //仓库1：好友仓库
    private final FriendsRepository friendsRepository;
    //仓库2：本地用户仓库
    private final LocalUserRepository localUserRepository;
    //仓库3：好友分组仓库
    private final GroupRepository groupRepository;

    public ContactGroupViewModel() {
        friendsRepository = FriendsRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
        groupRepository = GroupRepository.getInstance();
    }


    /**
     * 获取联系人列表的LiveData
     *
     * @return 结果呗
     */
    public LiveData<DataState<List<UserRelation>>> getListData() {
        if (listData == null) {
            //switchMap的作用是
            //当ListController发生数据变更时，将用如下定义的方式更新listData的value
            listData = Transformations.switchMap(listController, input -> {
                if (input.isActioning()) {
                    UserLocal user = localUserRepository.getLoggedInUser();
                    if (!user.isValid()) {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
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
                        return friendsRepository.getFriends(user.getToken(), null);
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return listData;
    }




    /**
     * 开始刷新列表数据
     */
    public void startFetchData() {
        listController.setValue(Trigger.getActioning());
    }








}