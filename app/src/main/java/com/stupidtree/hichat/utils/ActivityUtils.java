package com.stupidtree.hichat.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.ui.chat.ChatActivity;
import com.stupidtree.hichat.ui.conversation.ConversationActivity;
import com.stupidtree.hichat.ui.myprofile.MyProfileActivity;
import com.stupidtree.hichat.ui.profile.ProfileActivity;
import com.stupidtree.hichat.ui.relation.RelationActivity;
import com.stupidtree.hichat.ui.relationevent.RelationEventActivity;
import com.stupidtree.hichat.ui.search.SearchActivity;
import com.stupidtree.hichat.ui.welcome.WelcomeActivity;

/**
 * 此类整合了跳转到各个Activity的入口
 */
public class ActivityUtils {
    /**
     * 启动欢迎页
     * @param from 上下文
     */
    public static void startLoginActivity(@NonNull Context from){
        Intent i = new Intent(from, WelcomeActivity.class);
        from.startActivity(i);
    }

    /**
     * 启动搜索页
     * @param from 上下文
     */
    public static void startSearchActivity(@NonNull Context from){
        Intent i = new Intent(from, SearchActivity.class);
        from.startActivity(i);
    }

    /**
     * 启动用户资料页（他人）
     * @param from 上下文
     * @param id 用户id
     */
    public static void startProfileActivity(@NonNull Context from,@NonNull String id){
        Intent i = new Intent(from, ProfileActivity.class);
        i.putExtra("id",id);
        from.startActivity(i);
    }

    /**
     * 启动好友设置页
     * @param from 上下文
     * @param friendId 好友id
     */
    public static void startRelationActivity(@NonNull Context from,@NonNull String friendId){
        Intent intent = new Intent(from, RelationActivity.class);
        intent.putExtra("friendId",friendId);
        from.startActivity(intent);
    }

    /**
     * 启动我的资料页
     * @param from 上下文
     */
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
        i.putExtra("friendId",friendId);
        from.startActivity(i);
    }

    /**
     * 启动对话设置页面
     * @param from 上下文
     * @param friendId 朋友id
     */
    public static void startConversationActivity(@NonNull Context from,@NonNull String friendId){
        Intent i = new Intent(from, ConversationActivity.class);
        i.putExtra("friendId",friendId);
        from.startActivity(i);
    }

    /**
     * 启动好友关系事件页面
     * @param from 上下文
     */
    public static void startRelationEventActivity(@NonNull Context from){
        Intent i = new Intent(from, RelationEventActivity.class);
        from.startActivity(i);
    }
}
