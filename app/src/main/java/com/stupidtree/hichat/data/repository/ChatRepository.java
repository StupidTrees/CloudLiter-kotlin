package com.stupidtree.hichat.data.repository;

import android.content.Context;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.source.ChatMessageWebSource;
import com.stupidtree.hichat.data.source.SocketWebSource;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.chat.ChatListTrigger;
import com.stupidtree.hichat.ui.chat.FriendStateTrigger;

import java.util.List;

import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_FRIEND_STATE_CHANGED;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_MESSAGE_SENT;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_RECEIVE_MESSAGE;

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
    public LiveData<DataState<List<ChatMessage>>> getMessages(@NonNull String token, @Nullable String conversationId, int pageSize, int pageNum){
        return chatMessageWebSource.getMessages(token,conversationId,pageSize,pageNum);
    }

    public void bindService(Context context){
        IntentFilter IF = new IntentFilter();
        IF.addAction(ACTION_RECEIVE_MESSAGE);
        IF.addAction(ACTION_MESSAGE_SENT);
        IF.addAction(ACTION_FRIEND_STATE_CHANGED);
        context.registerReceiver(socketWebSource,IF);
        socketWebSource.bindService("Chat",context);
    }
    public void unbindService(Context context){
        context.unregisterReceiver(socketWebSource);
        socketWebSource.unbindService(context);
    }

    public void sendMessage(ChatMessage message){
        socketWebSource.sendMessage(message);
    }

    public void getIntoConversation(Context context,@NonNull String userId,@NonNull String friendId,@NonNull String conversationId){
        socketWebSource.getIntoConversation(context,userId,friendId,conversationId);
    }

    public void leftConversation(@NonNull Context context,@NonNull String userId, @NonNull String conversationId){
        socketWebSource.leftConversation(context,userId,conversationId);
    }

    public void markAllRead(@NonNull Context context,@NonNull String userId,@NonNull String conversationId){
        socketWebSource.markAllRead(context,userId,conversationId);
    }

    public void markRead(@NonNull Context context,@NonNull String messageId,@NonNull String conversationId){
        socketWebSource.markRead(context,messageId,conversationId);
    }

    public MutableLiveData<ChatListTrigger> getChatListController(){
        return socketWebSource.getListController();
    }

    public MutableLiveData<FriendStateTrigger> getFriendsStateController(){
        return socketWebSource.getFriendStateController();
    }

    public MutableLiveData<DataState<ChatMessage>> getMessageSentSate(){
        return socketWebSource.getMessageSentSate();
    }



}
