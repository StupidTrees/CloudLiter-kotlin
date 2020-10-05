package com.stupidtree.hichat.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.ui.chat.ChatActivity;
import com.stupidtree.hichat.ui.myprofile.MyProfileActivity;
import com.stupidtree.hichat.ui.profile.ProfileActivity;
import com.stupidtree.hichat.ui.relation.RelationActivity;
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

    public static void startRelationActivity(@NonNull Context from,@NonNull String friendId){
        Intent intent = new Intent(from, RelationActivity.class);
        intent.putExtra("friendId",friendId);
        from.startActivity(intent);
    }

    public static void startMyProfileActivity(@NonNull Context from){
        Intent i = new Intent(from, MyProfileActivity.class);
        from.startActivity(i);
    }

    /**
     * 启动聊天Activity
     * @param from 调用者
     * @param friendId 朋友id
     */
    public static void startChatActivity(@NonNull Context from, @NonNull String friendId){
        Intent i = new Intent(from, ChatActivity.class);
//        Bundle b = new Bundle();
//        b.putSerializable("conversation",conversation);
//        i.putExtras(b);
        i.putExtra("friendId",friendId);
        from.startActivity(i);
    }

}
