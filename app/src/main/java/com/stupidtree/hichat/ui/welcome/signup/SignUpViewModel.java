package com.stupidtree.hichat.ui.welcome.signup;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.data.repository.UserRepository;
import com.stupidtree.hichat.utils.TextUtils;

import java.util.Objects;

/**
 * 层次：ViewModel
 * 登录界面的ViewModel
 */
public class SignUpViewModel extends AndroidViewModel {


    /**
     * 数据区
     */
    //数据本体：登录表单
    private MutableLiveData<SignUpFormState> signUpFormState = new MutableLiveData<>();

    //Trigger：控制登录的进行
    private MutableLiveData<SignUpTrigger> signUpController = new MutableLiveData<>();


    /**
     * 仓库区
     */
    //用户仓库
    private UserRepository userRepository;
    private LocalUserRepository localUserRepository;

    public SignUpViewModel(Application application) {
        super(application);
        this.userRepository = UserRepository.getInstance(application);
        this.localUserRepository = LocalUserRepository.getInstance();
    }

    @NonNull
    LiveData<SignUpFormState> getLoginFormState() {
        return signUpFormState;
    }

    @NonNull
    LiveData<SignUpResult> getSignUpResult() {
       return Transformations.switchMap(signUpController, input -> {
           if(input.isActioning()){
               //通知用户仓库进行注册，并从中获取返回结果的liveData
               return userRepository.signUp(input.getUsername(),input.getPassword(),input.getGender(),input.getNickname());
           }
           return new MutableLiveData<>(null);
       });
    }

    /**
     * 进行注册请求
     * @param username 用户名
     * @param password 密码
     * @param gender 性别
     * @param nickname 昵称
     */
    public void signUp(final String username, final String password,
                       UserLocal.GENDER gender, String nickname
                       ) {
        signUpController.setValue(SignUpTrigger.getRequestState(
                username,password,gender,nickname));
    }


    /**
     * 当注册信息表变更时，通知viewModel变更数据
     * @param username 用户名
     * @param password 密码
     * @param passwordConfirm 确认密码
     * @param nickname 昵称
     */
    public void signUpDataChanged(String username, String password, String passwordConfirm,String nickname) {
        //检查输入合法性，若合法则更新登录表单
        if (!TextUtils.isUsernameValid(username)) {
            signUpFormState.setValue(
                    new SignUpFormState(R.string.invalid_username, null,null,null));
        } else if (!TextUtils.isPasswordValid(password)) {
            signUpFormState.setValue(
                    new SignUpFormState(null,R.string.invalid_password, null,null));
        } else if (!Objects.equals(password,passwordConfirm)) {
            signUpFormState.setValue(
                    new SignUpFormState(null,null, R.string.inconsistent_password,null));
        }else if(TextUtils.isEmpty(nickname)){
            signUpFormState.setValue(
                    new SignUpFormState(null,null,null,R.string.empty_nickname));
        }else{
            signUpFormState.setValue(new SignUpFormState(true));
        }
    }

}