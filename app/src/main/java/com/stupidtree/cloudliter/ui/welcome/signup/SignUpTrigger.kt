package com.stupidtree.cloudliter.ui.welcome.signup

import com.stupidtree.cloudliter.data.model.UserLocal.GENDER
import com.stupidtree.component.data.Trigger

class SignUpTrigger : Trigger() {
    var username: String? = null
        private set
    var password: String? = null
        private set
    private var gender: GENDER? = null
    var nickname: String? = null
        private set

    fun getGender(): String {
        return if (gender === GENDER.MALE) "MALE" else "FEMALE"
    }

    companion object {
        @JvmStatic
        fun getRequestState(username: String?, password: String?,
                            gender: GENDER?, nickname: String?
        ): SignUpTrigger {
            val ls = SignUpTrigger()
            ls.username = username
            ls.password = password
            ls.gender = gender
            ls.nickname = nickname
            ls.setActioning()
            return ls
        }
    }
}