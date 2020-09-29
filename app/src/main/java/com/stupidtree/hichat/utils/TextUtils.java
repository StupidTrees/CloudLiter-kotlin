package com.stupidtree.hichat.utils;

/**
 * 此类整合了一些文本处理有关的函数
 */
public class TextUtils {
    public static boolean isEmpty(String text){
        return text==null||text.isEmpty();
    }
    static public boolean isUsernameValid(String username){
        return !isEmpty(username) && username.length()>3;
    }

    static public boolean isPasswordValid(String password){
        return !isEmpty(password)&&password.length()>=8;
    }
}
