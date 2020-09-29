package com.stupidtree.hichat.ui.main.me;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.MeRepository;
import com.stupidtree.hichat.ui.base.Trigger;


/**
 * 层次：ViewModel
 * "我的"页面绑定的ViewModel
 */
public class MeViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：本地用户信息
    private LiveData<UserLocal> localUserMutableLiveData;
    //Trigger：控制↑的刷新
    private MutableLiveData<Trigger> localUserViewController = new MutableLiveData<>();

    /**
     * 仓库区
     */
    //仓库：本地用户仓库
    private MeRepository meRepository;


    public MeViewModel() {
        meRepository = MeRepository.getInstance();
    }

    public LiveData<UserLocal> getLocalUser() {
        if (localUserMutableLiveData == null) {
            //当controller发生变更时，将触发localUser按如下方式赋值
            localUserMutableLiveData = Transformations.switchMap(localUserViewController, input -> {
                if (input.isActioning()) {
                    //从仓库中调取数据
                    return meRepository.getLoggedInUser();
                }
                return new MutableLiveData<>(new UserLocal());
            });

        }
        return localUserMutableLiveData;
    }


    /**
     * 控制刷新页面
     */
    public void triggerRefreshLocalUser() {
        localUserViewController.setValue(Trigger.getActioning());
    }

}