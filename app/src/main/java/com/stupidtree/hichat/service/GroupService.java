package com.stupidtree.hichat.service;

import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.RelationGroup;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 层次：Service
 * 好友分组网络服务
 */
public interface GroupService {

    /**
     * 查询我的所有好友分组
     * @param token 令牌
     * @return 对话列表
     */
    @GET("/group/get")
    LiveData<ApiResponse<List<RelationGroup>>> queryMyGroups(@Header("token") String token);


    /**
     * 为好友分配分组
     * @param token 令牌
     * @param friendId 朋友id
     * @param groupId 分组Id
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("group/assign")
    LiveData<ApiResponse<Object>> assignGroup(@Header("token")String token, @Field("friendId")String friendId,@Field("groupId") String groupId);

    /**
     * 添加好友分组
     * @param token 令牌
     * @return 对话列表
     */
    @FormUrlEncoded
    @POST("/group/add")
    LiveData<ApiResponse<String>> addMyGroups(@Header("token") String token, @Field("groupName") String groupName);


    /**
     * 删除好友分组
     * @param token 令牌
     * @return 对话列表
     */
    @FormUrlEncoded
    @POST("/group/delete")
    LiveData<ApiResponse<String>> deleteMyGroups(@Header("token") String token, @Field("groupId") String groupName);

}
