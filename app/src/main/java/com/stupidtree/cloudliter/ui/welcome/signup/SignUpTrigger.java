package com.stupidtree.cloudliter.ui.welcome.signup;

import com.stupidtree.cloudliter.data.model.UserLocal;
import com.stupidtree.cloudliter.ui.base.Trigger;

public class SignUpTrigger extends Trigger {

    private String username;
    private String password;
    private UserLocal.GENDER gender;
    private String nickname;

    public static SignUpTrigger getRequestState(String username, String password,
                                                UserLocal.GENDER gender, String nickname
                                              ){
        SignUpTrigger ls =  new SignUpTrigger();
        ls.username = username;
        ls.password = password;
        ls.gender = gender;
        ls.nickname = nickname;
        ls.setActioning();
        return ls;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getGender() {
        return gender== UserLocal.GENDER.MALE?"MALE":"FEMALE";
    }

    public String getNickname() {
        return nickname;
    }
}
