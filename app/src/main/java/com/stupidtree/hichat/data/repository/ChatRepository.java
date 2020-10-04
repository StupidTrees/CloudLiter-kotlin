package com.stupidtree.hichat.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.source.ChatMessageWebSource;
import com.stupidtree.hichat.data.source.SocketWebSource;
import com.stupidtree.hichat.data.source.UserWebSource;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.chat.ChatListTrigger;
import com.stupidtree.hichat.ui.chat.FriendStateTrigger;

import java.util.List;

/**
 * 层次：Repository
 * 消息记录仓库
 */
public class ChatRepository {
    //单例模式
    private static volatile ChatRepository instance;

    public static ChatRepository getInstance() {
        if(instance==null){
            instance = new ChatRepository();
        }
        return instance;
    }

    //数据源1：网络类型数据，消息记录的网络数据源
    ChatMessageWebSource chatMessageWebSource;
    //数据源2：和后台服务通信的Service
    SocketWebSource socketWebSource;
    //数据源3：网络类型数据源，用户网络操作
    private UserWebSource userWebSource;

    public ChatRepository(){
        chatMessageWebSource = ChatMessageWebSource.getInstance();
        socketWebSource = new SocketWebSource();
    }

    /**
     * 获取某对话的消息记录
     * @param token 令牌
     * @param conversationId 对话id
     * @return 消息记录
     */
    public LiveData<DataState<List<ChatMessage>>> getMessages(@NonNull String token, @Nullable String conversationId){
        return chatMessageWebSource.getMessages(token,conversationId);
    }

    public void bindService(Context context){
        socketWebSource.bindService("Chat",context);
    }
    public void unbindService(Context context){
        socketWebSource.unbindService(context);
    }

    public void sendMessage(ChatMessage message){
        socketWebSource.sendMessage(message);
    }

    public void getIntoConversation(@NonNull String userId,@NonNull String friendId,@NonNull String conversationId){
        socketWebSource.getIntoConversation(userId,friendId,conversationId);
    }

    public void leftConversation(@NonNull String userId, @NonNull String conversationId){
        socketWebSource.leftConversation(userId,conversationId);
    }

    public void markAllRead(@NonNull String userId,@NonNull String conversationId){
        socketWebSource.markAllRead(userId,conversationId);
    }

    public void markRead(@NonNull ChatMessage message){
        socketWebSource.markRead(message);
    }

    public MutableLiveData<ChatListTrigger> getChatListController(){
        return socketWebSource.getListController();
    }

    public MutableLiveData<FriendStateTrigger> getFriendsStateController(){
        return socketWebSource.getFriendStateController();
    }


}
