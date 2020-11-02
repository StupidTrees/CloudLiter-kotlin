package com.stupidtree.cloudliter.data.model;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stupidtree.cloudliter.utils.JsonUtils;

import java.util.Objects;

/**
 * 显示在联系人列表的数据Model
 * 暂未和服务器返回数据格式匹配，需要适配函数
 */
public class UserRelation {
    //姓名
    String friendNickname;
    //头像链接
    String friendAvatar;
    //分组id
    String groupId;
    //分组名称
    String groupName;
    //性别
    UserLocal.GENDER friendGender;
    //联系人的用户id
    String friendId;
    //联系人用户的备注
    String remark;


    /**
     * 仅在本地使用的属性
     */
    boolean label = false;//是否显示为分组标签

    public static UserRelation getLabelInstance(String groupId,String groupName){
        UserRelation res = new UserRelation();
        res.label = true;
        res.friendNickname = groupName;
        res.groupId = groupId;
        return res;
    }

    public boolean isLabel() {
        return label;
    }

    public String getFriendId() {
        return friendId;
    }

    public String getFriendNickname() {
        return friendNickname;
    }


    @Nullable
    public String getGroupId() {
        return groupId;
    }

    public String getRemark(){return remark;}

    public String getFriendAvatar() {
        return friendAvatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRelation that = (UserRelation) o;
        return Objects.equals(friendNickname, that.friendNickname) &&
                Objects.equals(friendId, that.friendId);
    }


    public String getGroupName() {
        return groupName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendNickname, friendId);
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
            res.friendNickname = userInfo.get("friendNickname").getAsString();
            res.friendId = userInfo.get("friendId").getAsString();
            res.friendAvatar = JsonUtils.getStringData(userInfo,"friendAvatar");
            res.groupId = JsonUtils.getStringData(jo,"groupId");
            res.groupName = JsonUtils.getStringData(jo,"groupName");
            res.remark=JsonUtils.getStringData(jo,"remark");
            String gender = userInfo.get("gender").getAsString();
            res.friendGender = Objects.equals(gender,"MALE")? UserLocal.GENDER.MALE: UserLocal.GENDER.FEMALE;
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "UserRelation{" +
                "name='" + friendNickname + '\'' +
                ", avatar='" + friendAvatar + '\'' +
                ", group='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", gender=" + friendGender +
                ", id='" + friendId + '\'' +
                ", remark='" + remark + '\'' +
                ", label=" + label +
                '}';
    }
}
