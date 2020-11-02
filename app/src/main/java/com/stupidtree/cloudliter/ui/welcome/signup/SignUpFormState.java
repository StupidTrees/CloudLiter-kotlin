package com.stupidtree.cloudliter.ui.welcome.signup;

import androidx.annotation.Nullable;

/**
 * 登录表单View的数据封装
 */
class SignUpFormState {
    @Nullable
    private Integer usernameError; //用户名错误
    @Nullable
    private Integer passwordError; //密码错误
    @Nullable
    private Integer passwordConfirmError; //用户名错误
    @Nullable
    private Integer nicknameError;//昵称错误

    private boolean formValid;

    public SignUpFormState(@Nullable Integer usernameError, @Nullable Integer passwordError, @Nullable Integer passwordConfirmError, @Nullable Integer nicknameError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.passwordConfirmError = passwordConfirmError;
        this.nicknameError = nicknameError;
    }

    public SignUpFormState(boolean formValid) {
        if(formValid){
            usernameError = null;
            passwordError = null;
            passwordConfirmError = null;
            nicknameError = null;
        }
        this.formValid = formValid;
    }

    @Nullable
    public Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    public Integer getPasswordConfirmError() {
        return passwordConfirmError;
    }

    @Nullable
    public Integer getNicknameError() {
        return nicknameError;
    }

    public boolean isFormValid() {
        return formValid;
    }
}