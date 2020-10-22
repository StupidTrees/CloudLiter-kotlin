package com.stupidtree.hichat.data.model;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stupidtree.hichat.utils.JsonUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 显示在联系人列表的数据Model
 * 暂未和服务器返回数据格式匹配，需要适配函数
 */
public class UserRelation {
    //姓名
    String name;
    //头像链接
    String avatar;
    //分组id
    String group;
    //性别
    UserLocal.GENDER gender;
    //联系人的用户id
    String id;
    //联系人用户的备注
    String remark;

    /**
     * 仅在本地使用的属性
     */
    boolean label = false;//是否显示为分组标签

    public static UserRelation getLabelInstance(@NotNull RelationGroup relationGroup){
        UserRelation res = new UserRelation();
        res.label = true;
        res.name = relationGroup.getGroupName();
        res.group = relationGroup.getId();
        return res;
    }

    public boolean isLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRemark(){return remark;}

    public String getAvatar() {
        return avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRelation that = (UserRelation) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(id, that.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }


    /**
     * 从网络请求返回的JsonElement中解析出一个FriendContact对象
     * @param je JsonElement对象
     * @return FriendContact对象
     */
    @Nullable
    public static UserRelation getInstanceFromJsonObject(JsonElement je){
        try {
            JsonObject jo = je.getAsJsonObject();
            JsonObject userInfo = jo.get("user").getAsJsonObject();
            UserRelation res = new UserRelation();
            //res.group = jo.get("group").getAsInt();
            res.name = userInfo.get("nickname").getAsString();
            res.id = userInfo.get("id").getAsString();
            res.avatar = JsonUtils.getStringData(userInfo,"avatar");
            res.remark=JsonUtils.getStringData(jo,"remark");
            String gender = userInfo.get("gender").getAsString();
            res.gender = Objects.equals(gender,"MALE")? UserLocal.GENDER.MALE: UserLocal.GENDER.FEMALE;
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "UserRelation{" +
                "name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", group='" + group + '\'' +
                ", gender=" + gender +
                ", id='" + id + '\'' +
                ", remark='" + remark + '\'' +
                ", label=" + label +
                '}';
    }
}
