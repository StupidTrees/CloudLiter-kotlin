package com.stupidtree.cloudliter.data.model;


import androidx.annotation.StringRes;

import com.stupidtree.cloudliter.R;

import org.jetbrains.annotations.NotNull;

/**
 * 显示在用户资料页的用户资料Model
 * 和服务器返回数据匹配，无需适配函数
 */
public class UserProfile {
    public enum COLOR{RED,ORANGE,YELLOW,GREEN,CYAN,BLUE,PURPLE}
    String id; //用户id
    String username; //用户名
    String nickname; //昵称
    UserLocal.GENDER gender; //性别
    String signature; //签名
    String avatar; //头像
    UserProfile.COLOR color; //颜色

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public UserLocal.GENDER getGender() {
        return gender;
    }

    public String getSignature() {
        return signature;
    }

    public String getAvatar() {
        return avatar;
    }

    public UserProfile.COLOR getColor() {
        return color;
    }

    @StringRes
    public int getColorName() {
        switch (color){
            case RED:
                return R.string.red;
            case ORANGE:
                return R.string.orange;
            case YELLOW:
                return R.string.yellow;
            case GREEN:
                return R.string.green;
            case CYAN:
                return R.string.cyan;
            case PURPLE:
                return R.string.purple;
            default:
                return R.string.blue;
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "UserProfile{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gender=" + gender +
                ", signature='" + signature + '\'' +
                ", avatar='" + avatar + '\'' +
                ", color=" + color +
                '}';
    }
}
