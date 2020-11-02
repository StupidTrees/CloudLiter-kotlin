package com.stupidtree.cloudliter.ui.welcome.login;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.cloudliter.R;
import com.stupidtree.cloudliter.data.repository.UserRepository;
import com.stupidtree.cloudliter.utils.TextUtils;

/**
 * 层次：ViewModel
 * 登录界面的ViewModel
 */
public class LoginViewModel extends AndroidViewModel {


    /**
     * 数据区
     */
    //数据本体：登录表单
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    //状态数据：登录章台
    private final MutableLiveData<LoginTrigger> loginState = new MutableLiveData<>();

    /**
     * 仓库区
     */
    //用户仓库
    private final UserRepository userRepository;


    public LoginViewModel(Application application) {
        super(application);
        userRepository = UserRepository.getInstance(application);
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return Transformations.switchMap(loginState, input -> {
            if (input.isActioning()) {
                return userRepository.login(input.getUsername(), input.getPassword());
            }
            return new MutableLiveData<>(null);
        });
    }

    /**
     * 登录操作
     *
     * @param username 用户名
     * @param password 密码
     */
    public void login(final String username, final String password) {
        loginState.setValue(LoginTrigger.getRequestState(username, password));
    }


    /**
     * 当文本框信息改变时，调用本函数
     *
     * @param username 用户名
     * @param password 密码
     */
    public void loginDataChanged(String username, String password) {
        //检查输入合法性，若合法则更新登录表单
        if (!TextUtils.isUsernameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!TextUtils.isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

}