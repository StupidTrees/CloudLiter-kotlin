package com.stupidtree.hichat.data.model;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stupidtree.hichat.ui.welcome.signup.SignUpTrigger;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 显示在搜索页面的搜索结果Model
 * 和服务器返回数据匹配，无需适配函数
 */
public class UserSearched {
    String username;//用户名
    String nickname;//昵称
    String avatar;//头像
    UserLocal.GENDER gender;//性别
    Integer id;//id

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSearched that = (UserSearched) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, id);
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

    public Integer getId() {
        return id;
    }

    public String getAvatar() {
        return avatar;
    }

    @NotNull
    @Override
    public String toString() {
        return "UserSearched{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", gender=" + gender +
                ", id=" + id +
                '}';
    }
}
