package com.stupidtree.hichat.service;

import androidx.lifecycle.LiveData;

import com.google.gson.JsonObject;
import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.model.UserSearched;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


/**
 * 层次：Service
 * 用户网络服务
 */
public interface UserService {


    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return retrofit的Call，其中response里包含的是登录成功发放的token
     */
    @FormUrlEncoded
    @POST("/user/login")
    LiveData<ApiResponse<JsonObject>> login(@Field("username") String username,
                                            @Field("password") String password);

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param gender 性别
     * @param nickname 昵称
     * @return call，其中response里包含token
     */
    @FormUrlEncoded
    @POST("/user/sign_up")
    LiveData<ApiResponse<JsonObject>> signUp(@Field("username") String username,
                           @Field("password") String password,
                            @Field("gender") String gender,
                            @Field("nickname") String nickname);

    /**
     * 用户资料获取
     * @param id 用户id
     * @param token 登录状态的token
     * @return call，返回的会是用户基本资料
     */
    @GET("/user/profile/get")
    LiveData<ApiResponse<UserProfile>> getUserProfile(@Query("id") String id,
                                                  @Header("token") String token);


    /**
     * 根据语句搜索用户
     * @param text 语句
     * @param token 登录状态的token
     * @return 搜索结果
     */
    @GET("/user/search")
    LiveData<ApiResponse<List<UserSearched>>> searchUser(@Query("text") String text,
                                                     @Header("token") String token);

    /**
     * 上传头像
     * @param file 头像体
     * @param token 登录状态的token
     * @return 返回操作结果和文件名
     */
    @Multipart
    @POST("/user/profile/upload_avatar")
    LiveData<ApiResponse<JsonObject>> uploadAvatar(@Part MultipartBody.Part file,@Header("token") String token);

    /**
     * 更换昵称
     * @param nickname 新昵称
     * @param token 登录状态的token（表征了用户身份）
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_nickname")
    LiveData<ApiResponse<Object>> changeNickname(@Field("nickname")String nickname,@Header("token")String token);


    /**
     * 更换性别
     * @param gender 新性别 MALE/FEMALE
     * @param token 登录状态的token
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_gender")
    LiveData<ApiResponse<Object>> changeGender(@Field("gender")String gender,@Header("token")String token);

    /**
     * 更换颜色
     * @param color 新颜色 红橙黄绿青蓝紫
     * @param token 登录状态的token
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_color")
    LiveData<ApiResponse<Object>> changeColor(@Field("color")String color,@Header("token")String token);


    /**
     * 更换签名
     * @param signature 新签名
     * @param token 登录状态的token（表征了用户身份）
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_signature")
    LiveData<ApiResponse<Object>> changeSignature(@Field("signature")String signature,@Header("token")String token);



}


