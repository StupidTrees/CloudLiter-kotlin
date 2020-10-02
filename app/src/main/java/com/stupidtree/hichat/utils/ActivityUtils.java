package com.stupidtree.hichat.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.ui.chat.ChatBaseActivity;
import com.stupidtree.hichat.ui.myprofile.MyProfileActivity;
import com.stupidtree.hichat.ui.profile.ProfileActivity;
import com.stupidtree.hichat.ui.search.SearchActivity;
import com.stupidtree.hichat.ui.welcome.WelcomeActivity;

/**
 * 此类整合了跳转到各个Activity的入口
 */
public class ActivityUtils {
    public static void startLoginActivity(@NonNull Context from){
        Intent i = new Intent(from, WelcomeActivity.class);
        from.startActivity(i);
    }

    public static void startSearchActivity(@NonNull Context from){
        Intent i = new Intent(from, SearchActivity.class);
        from.startActivity(i);
    }

    public static void startProfileActivity(@NonNull Context from,@NonNull String id){
        Intent i = new Intent(from, ProfileActivity.class);
        i.putExtra("id",id);
        from.startActivity(i);
    }

    public static void startMyProfileActivity(@NonNull Context from){
        Intent i = new Intent(from, MyProfileActivity.class);
        from.startActivity(i);
    }

    /**
     * 启动聊天Activity
     * @param from 调用者
     * @param conversation 聊天对象
     */
    public static void startChatActivity(@NonNull Context from, @NonNull Conversation conversation){
        Intent i = new Intent(from, ChatBaseActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("conversation",conversation);
        i.putExtras(b);
        from.startActivity(i);
    }

}
