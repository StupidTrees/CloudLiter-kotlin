package com.stupidtree.hichat.ui.welcome.login;

import androidx.annotation.StringRes;

/**
 * 封装了登录结果，预计要弃用
 */
public class LoginResult {
    
    public enum STATES {SUCCESS,WRONG_USERNAME,WRONG_PASSWORD,ERROR,REQUEST_FAILED}
    STATES state;
    int message;
    String token;

    public LoginResult() {
    }

    public LoginResult(STATES state, int message) {
        this.state = state;
        this.message = message;
    }

    public STATES getState() {
        return state;
    }

    public void setState(STATES state) {
        this.state = state;
    }

    public void set(STATES state,@StringRes int message) {
        this.state = state;
        this.message = message;
    }
    @StringRes
    public int getMessage() {
        return message;
    }

    public void setMessage(@StringRes int message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LoginResult{" +
                "state=" + state +
                ", message=" + message +
                ", token='" + token + '\'' +
                '}';
    }
}
