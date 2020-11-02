package com.stupidtree.cloudliter.ui.welcome.login;

import com.stupidtree.cloudliter.ui.base.Trigger;


/**
 * 包含用户名和密码的登录Trigger
 */
public class LoginTrigger extends Trigger {

    private String username;//用户名
    private String password;//密码



    public static LoginTrigger getRequestState(String username, String password){
        LoginTrigger ls =  new LoginTrigger();
        ls.username = username;
        ls.password = password;
        ls.setActioning();
        return ls;
    }
    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }

}
