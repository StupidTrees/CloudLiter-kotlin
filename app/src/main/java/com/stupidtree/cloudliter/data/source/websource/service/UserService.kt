package com.stupidtree.cloudliter.data.source.websource.service

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.data.model.UserSearched
import okhttp3.MultipartBody
import retrofit2.http.*
import java.util.*

/**
 * 层次：Service
 * 用户网络服务
 */
interface UserService {
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return retrofit的Call，其中response里包含的是登录成功发放的token
     */
    @FormUrlEncoded
    @POST("/user/login")
    fun login(@Field("username") username: String?,
              @Field("password") password: String?): LiveData<ApiResponse<JsonObject?>?>

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
    fun signUp(@Field("username") username: String?,
               @Field("password") password: String?,
               @Field("gender") gender: String?,
               @Field("nickname") nickname: String?): LiveData<ApiResponse<JsonObject?>?>

    /**
     * 用户资料获取
     * @param id 用户id
     * @param token 登录状态的token
     * @return call，返回的会是用户基本资料
     */
    @GET("/user/profile/get")
    fun getUserProfile(@Query("id") id: String?,
                       @Header("token") token: String?): LiveData<ApiResponse<UserProfile?>?>

    /**
     * 根据语句搜索用户
     * @param text 语句
     * @param token 登录状态的token
     * @return 搜索结果
     */
    @GET("/user/search")
    fun searchUser(@Query("text") text: String?,
                   @Header("token") token: String?): LiveData<ApiResponse<List<UserSearched>?>?>

    /**
     * 根据词云搜索用户
     * @param text 关键词
     * @param token 登录状态的token
     * @return 搜索结果
     */
    @GET("/user/search/word_cloud")
    fun searchUserByWordCloud(@Query("word") text: String?,
                   @Header("token") token: String?): LiveData<ApiResponse<List<UserSearched>?>?>

    /**
     * 上传头像
     * @param file 头像体
     * @param token 登录状态的token
     * @return 返回操作结果和文件名
     */
    @Multipart
    @POST("/user/profile/upload_avatar")
    fun uploadAvatar(@Part file: MultipartBody.Part?, @Header("token") token: String?): LiveData<ApiResponse<JsonObject?>?>

    /**
     * 更换昵称
     * @param nickname 新昵称
     * @param token 登录状态的token（表征了用户身份）
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_nickname")
    fun changeNickname(@Field("nickname") nickname: String?, @Header("token") token: String?): LiveData<ApiResponse<Any?>?>

    /**
     * 更换性别
     * @param gender 新性别 MALE/FEMALE
     * @param token 登录状态的token
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_gender")
    fun changeGender(@Field("gender") gender: String?, @Header("token") token: String?): LiveData<ApiResponse<Any?>?>

    /**
     * 更换颜色
     * @param color 新颜色 红橙黄绿青蓝紫
     * @param token 登录状态的token
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_accessibility")
    fun changeAccessibility(@Field("accessibility") accessibility: String?, @Header("token") token: String?): LiveData<ApiResponse<Any?>?>

    /**
     * 更换无障碍状态
     * @param type 用户类型
     * @param subType 无障碍二级分类
     * @param typePermission 无障碍隐私类型
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_type")
    fun changeType(@Field("type") type: Int,
                   @Field("subType") subType: String?,
                   @Field("typePermission") typePermission: String?,
                   @Header("token") token: String?): LiveData<ApiResponse<Any?>?>

    /**
     * 更换签名
     * @param signature 新签名
     * @param token 登录状态的token（表征了用户身份）
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/change_signature")
    fun changeSignature(@Field("signature") signature: String?, @Header("token") token: String?): LiveData<ApiResponse<Any?>?>

    /**
     * 设置词云可见性
     * @param private 是否可见
     * @param token 登录状态的token（表征了用户身份）
     * @return 操作结果
     */
    @FormUrlEncoded
    @POST("/user/profile/word_cloud_private")
    fun setWordCloudAccessibility(@Field("private") private:Boolean, @Header("token") token: String?): LiveData<ApiResponse<Any?>?>


    /**
     * 获取用户词云
     * @param token 登录状态的token
     * @return 搜索结果
     */
    @GET("/user/profile/word_cloud")
    fun getWordCloud(@Header("token") token: String?, @Query("userId") userId: String?): LiveData<ApiResponse<HashMap<String, Float?>?>?>

    /**
     * 删除用户词云
     * @param token 登录状态的token
     * @param wordCloud 词云数据
     * @return 搜索结果
     */
    @FormUrlEncoded
    @POST("/conversation/delete_wordcloud")
    fun deleteWordCloud(@Header("token") token: String?, @Field("cloudId") wordId: String?,@Field("wordId") cloudId:String?): LiveData<ApiResponse<Any>>
}