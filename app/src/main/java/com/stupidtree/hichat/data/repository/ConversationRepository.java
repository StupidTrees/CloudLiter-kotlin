package com.stupidtree.hichat.data.repository;

import android.content.Context;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.source.ConversationWebSource;
import com.stupidtree.hichat.data.source.SocketWebSource;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.List;

import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_FRIEND_STATE_CHANGED;
import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_RECEIVE_MESSAGE;

/**
 * 层次：Repository
 * 对话仓库
 */
public class ConversationRepository {
    //单例模式
    private static volatile ConversationRepository instance;

    public static ConversationRepository getInstance() {
        if(instance==null){
            instance = new ConversationRepository();
        }
        return instance;
    }

    //数据源1：网络类型数据，对话的网络数据源
    ConversationWebSource conversationWebSource;
    //数据源2：
    SocketWebSource socketWebSource;

    public ConversationRepository(){
        conversationWebSource = ConversationWebSource.getInstance();
        socketWebSource = new SocketWebSource();
    }

    public void bindService(Context context){
        IntentFilter IF = new IntentFilter();
        IF.addAction(ACTION_RECEIVE_MESSAGE);
        IF.addAction(ACTION_FRIEND_STATE_CHANGED);
        context.registerReceiver(socketWebSource,IF);
        socketWebSource.bindService("conversation",context);
    }

    public void unbindService(Context context){
        socketWebSource.unbindService(context);
        context.unregisterReceiver(socketWebSource);
    }



    /**
     * 获取某用户的对话列表
     * @param token 令牌
     * @param id 用户id（可选）
     * @return 查询结果
     */
    public LiveData<DataState<List<Conversation>>> getConversations(@NonNull String token, @Nullable String id){
        return conversationWebSource.getConversations(token,id);
    }

    public LiveData<DataState<List<ChatMessage>>> getUnreadMessages(){
        return socketWebSource.getUnreadMessages();
    }

    public LiveData<DataState<Conversation>> queryConversation(@NonNull String token, @Nullable String userId,@NonNull String friendId){
        return conversationWebSource.queryConversation(token,userId,friendId);
    }

    public void callOnline(@NonNull Context context,@NonNull UserLocal userLocal){
        socketWebSource.callOnline(context,userLocal);
    }
}
