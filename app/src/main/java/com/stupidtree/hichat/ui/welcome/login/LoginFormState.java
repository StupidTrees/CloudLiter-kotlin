package com.stupidtree.hichat.ui.welcome.login;

import androidx.annotation.Nullable;

/**
 * 登录表单View的数据封装
 */
class LoginFormState {
    @Nullable
    private Integer usernameError; //用户名错误文本提示
    @Nullable
    private Integer passwordError; //密码错误提示
    private boolean isDataValid;


    LoginFormState(@Nullable Integer usernameError, @Nullable Integer passwordError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    LoginFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}