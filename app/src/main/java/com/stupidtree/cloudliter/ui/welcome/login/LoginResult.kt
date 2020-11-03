package com.stupidtree.cloudliter.ui.welcome.login

import androidx.annotation.StringRes
import com.stupidtree.cloudliter.data.model.UserLocal

/**
 * 封装了登录结果，预计要弃用
 */
class LoginResult {
    enum class STATES {
        SUCCESS, WRONG_USERNAME, WRONG_PASSWORD, ERROR, REQUEST_FAILED
    }

    var state: STATES? = null

    @get:StringRes
    var message = 0
    var token: String? = null
    var userLocal: UserLocal? = null

    constructor() {}
    constructor(state: STATES?, message: Int) {
        this.state = state
        this.message = message
    }

    operator fun set(state: STATES?, @StringRes message: Int) {
        this.state = state
        this.message = message
    }

    override fun toString(): String {
        return "LoginResult{" +
                "state=" + state +
                ", message=" + message +
                ", token='" + token + '\'' +
                '}'
    }
}