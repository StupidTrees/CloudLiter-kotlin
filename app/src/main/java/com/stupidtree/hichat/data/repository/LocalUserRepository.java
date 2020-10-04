package com.stupidtree.hichat.data.repository;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.stupidtree.hichat.HiApplication;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.source.UserPreferenceSource;

import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_OFFLINE;


/**
 * 层次：Repository
 * ”我的“页面的Repository
 */
public class LocalUserRepository {

    //也是单例模式
    private static LocalUserRepository instance;

    //数据源：SharedPreference性质的本地状态数据源
    UserPreferenceSource mePreferenceSource;

    //将已登录用户缓存在内存里
    UserLocal loggedInUser = null;



    LocalUserRepository(){
        //初始化数据源
        mePreferenceSource = new UserPreferenceSource(HiApplication.getContext());
    }

    public static LocalUserRepository getInstance(){
        if(null == instance){
            instance = new LocalUserRepository();
        }
        return instance;
    }


    /**
     * 登出
     */
    public void logout(@NonNull Context context){
        if(loggedInUser!=null){
            Intent i = new Intent(ACTION_OFFLINE);
            i.putExtra("userId",loggedInUser.getId());
            context.sendBroadcast(i);
        }
        loggedInUser = null;
        mePreferenceSource.clearLocalUser();
    }

    /**
     * 获得当时本地缓存的已登录用户
     * @return LiveData形式
     */
    public MutableLiveData<UserLocal> getLoggedInUser(){
       final MutableLiveData<UserLocal> result = new MutableLiveData<>();
       loggedInUser = mePreferenceSource.getLocalUser();
       result.setValue(loggedInUser);
       return result;
    }


    /**
     * 更改该本地缓存的头像地址
     * @param newAvatar 头像地址
     */
    public void ChangeLocalAvatar(String newAvatar){
        mePreferenceSource.saveAvatar(newAvatar);
        loggedInUser = mePreferenceSource.getLocalUser();
        // getThis().getSharedPreferences("Glide", Context.MODE_PRIVATE).edit().
    }

    /**
     * 更改本地缓存的昵称
     * @param nickname 新昵称
     */
    public void ChangeLocalNickname(String nickname){
        mePreferenceSource.saveNickname(nickname);
        loggedInUser = mePreferenceSource.getLocalUser();
    }



    /**
     * 更改本地缓存的用户性别
     * @param gender 性别/MALE/FEMALE
     */
    public void ChangeLocalGender(String gender){
        mePreferenceSource.saveGender(gender);
        loggedInUser = mePreferenceSource.getLocalUser();
    }

    /**
     * 更改本地缓存的签名
     * @param signature 新签名
     */
    public void ChangeLocalSignature(String signature){ mePreferenceSource.saveSignature(signature);
    }

    /**
     * 直接获取本地已登陆的用户对象
     * 同步获取
     * @return 本地用户对象
     */
    @NonNull
    public UserLocal getLoggedInUserDirect(){
        //Log.e("get_local_user", String.valueOf(loggedInUser));
        if(loggedInUser==null){
            loggedInUser = mePreferenceSource.getLocalUser();
        }
        return loggedInUser;
    }

    /**
     * 用户是否登录
     * @return 是否登陆
     */
    public boolean isUserLoggedIn(){
        return loggedInUser!=null&&loggedInUser.isValid();
    }
}
