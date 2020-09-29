package com.stupidtree.hichat.ui.welcome.signup;

import androidx.annotation.StringRes;

public class SignUpResult {

    public enum STATES {SUCCESS,ERROR,REQUEST_FAILED,USER_EXISTS}
    STATES state;
    int message;
    String token;

    public SignUpResult() {
    }

    public SignUpResult(STATES state, int message) {
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
