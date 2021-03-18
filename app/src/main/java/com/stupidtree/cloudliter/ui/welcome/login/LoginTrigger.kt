package com.stupidtree.cloudliter.ui.welcome.login

import com.stupidtree.component.data.Trigger

/**
 * 包含用户名和密码的登录Trigger
 */
class LoginTrigger : Trigger() {
    var username //用户名
            : String? = null
        private set
    var password //密码
            : String? = null
        private set

    companion object {
        @JvmStatic
        fun getRequestState(username: String?, password: String?): LoginTrigger {
            val ls = LoginTrigger()
            ls.username = username
            ls.password = password
            ls.setActioning()
            return ls
        }
    }
}