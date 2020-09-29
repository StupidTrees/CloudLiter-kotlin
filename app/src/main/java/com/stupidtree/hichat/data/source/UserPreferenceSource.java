package com.stupidtree.hichat.data.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.stupidtree.hichat.data.model.UserLocal;

import java.util.UUID;

/**
 * 层次：DataSource
 * 本地用户的数据源
 * 类型：SharedPreference
 * 数据：同步读取，异步写入
 */
public class UserPreferenceSource {
    private static final String SP_NAME_LOCAL_USER = "local_user_profile";
    @SuppressLint("StaticFieldLeak")
    private static UserPreferenceSource instance;

    private SharedPreferences sharedPreferences;
    private Context context;
    public static UserPreferenceSource getInstance(Context context) {
        if(instance==null){
            instance = new UserPreferenceSource(context.getApplicationContext());
        }
        return instance;
    }

    public UserPreferenceSource(Context context){
        this.context = context;
    }

    private SharedPreferences getPreference(){
        if(sharedPreferences==null){
            sharedPreferences = context.getSharedPreferences(SP_NAME_LOCAL_USER, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }
    public void saveLocalUser(UserLocal user) {
        SharedPreferences preferences = getPreference();
        preferences.edit()
                .putString("username", user.getUsername())
                .putString("nickname", user.getNickname())
                .putString("gender", String.valueOf(user.getGender()))
                .putString("token", user.getToken())
                .putString("avatar",user.getAvatar())
                .apply();
    }


    public void saveAvatar(String newAvatar){
        getPreference().edit()
                .putString("avatar",newAvatar)
                .apply();
        changeMyAvatarGlideSignature();
    }
    public void saveNickname(String nickname){
        getPreference().edit()
                .putString("nickname",nickname)
                .apply();
    }
    public void saveGender(String gender){
        getPreference().edit()
                .putString("gender",gender)
                .apply();
    }
    public void clearLocalUser(){
        SharedPreferences preferences = context.getSharedPreferences(SP_NAME_LOCAL_USER, Context.MODE_PRIVATE);
        preferences.edit().putString("username",null).putString("nickname",null).putString("token",null).apply();
    }
    public UserLocal getLocalUser(){
        SharedPreferences preferences = getPreference();
        UserLocal result = new UserLocal();
        result.setUsername(preferences.getString("username",null));
        result.setNickname(preferences.getString("nickname",null));
        result.setToken(preferences.getString("token",null));
        result.setGender(preferences.getString("gender","MALE"));
        result.setAvatar(preferences.getString("avatar",null));
        return result;
    }

    public void changeMyAvatarGlideSignature(){
        getPreference().edit().putString("my_avatar", UUID.randomUUID().toString()).apply();
    }

    public String getMyAvatarGlideSignature(){
        return getPreference().getString("my_avatar", UUID.randomUUID().toString());
    }



}
