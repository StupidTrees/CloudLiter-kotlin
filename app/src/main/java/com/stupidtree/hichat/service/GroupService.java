package com.stupidtree.hichat.service;

import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.RelationGroup;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Header;
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

}
