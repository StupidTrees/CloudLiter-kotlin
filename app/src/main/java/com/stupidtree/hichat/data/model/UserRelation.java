package com.stupidtree.hichat.data.model;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stupidtree.hichat.utils.JsonUtils;

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
    Integer group;
    //性别
    UserLocal.GENDER gender;
    //联系人的用户id
    String id;
    //联系人用户的备注
    String remark;


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
}