package com.stupidtree.hichat.data.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.stupidtree.hichat.data.model.UserLocal;

import java.util.UUID;

import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_ONLINE;

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
        Intent i = new Intent(ACTION_ONLINE);
        Bundle b = new Bundle();
        b.putSerializable("user",user);
        i.putExtras(b);
        context.sendBroadcast(i);
        Log.e("save_local_user", String.valueOf(user));
        getPreference().edit()
                .putString("id",user.getId())
                .putString("username", user.getUsername())
                .putString("nickname", user.getNickname())
                .putString("gender", String.valueOf(user.getGender()))
                .putString("signature",user.getSignature()) //获取签名
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
    public void saveSignature(String signature){
        getPreference().edit()
                .putString("signature",signature)
                .apply();
    }

    public void clearLocalUser(){
        SharedPreferences preferences = context.getSharedPreferences(SP_NAME_LOCAL_USER, Context.MODE_PRIVATE);
        preferences.edit().putString("username",null).putString("nickname",null).putString("token",null).apply();
    }

    @NonNull
    public UserLocal getLocalUser(){
        SharedPreferences preferences = getPreference();
        UserLocal result = new UserLocal();
        result.setId(preferences.getString("id",null));
        result.setUsername(preferences.getString("username",null));
        result.setNickname(preferences.getString("nickname",null));
        result.setSignature(preferences.getString("signature",null));
        result.setToken(preferences.getString("token",null));
        result.setGender(preferences.getString("gender","MALE"));
        result.setAvatar(preferences.getString("avatar",null));
        Log.e("get_local_user", String.valueOf(result));
        return result;
    }

    public void changeMyAvatarGlideSignature(){
        getPreference().edit().putString("my_avatar", UUID.randomUUID().toString()).apply();
    }

    public String getMyAvatarGlideSignature(){
        String signature = getPreference().getString("my_avatar", null);
        if(signature==null){
            signature = UUID.randomUUID().toString();
            getPreference().edit().putString("my_avatar",signature).apply();
        }
        return signature;
    }



}
