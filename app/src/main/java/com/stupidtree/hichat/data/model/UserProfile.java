package com.stupidtree.hichat.data.model;


import org.jetbrains.annotations.NotNull;

/**
 * 显示在用户资料页的用户资料Model
 * 和服务器返回数据匹配，无需适配函数
 */
public class UserProfile {
    Integer id; //用户id
    String username; //用户名
    String nickname; //昵称
    UserLocal.GENDER gender; //性别
    String signature; //签名
    String avatar; //头像

    public Integer getId() {
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

    @NotNull
    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gender=" + gender +
                ", signature='" + signature + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
