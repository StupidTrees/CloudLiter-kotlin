package com.stupidtree.hichat.ui.chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.ChatMessageRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.service.SocketIOClientService;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.Context.BIND_AUTO_CREATE;

public class ChatViewModel extends ViewModel {
    /**
     * 数据区
     */
    private MutableLiveData<Conversation> conversationMutableLiveData = new MutableLiveData<>();
    //数据本体：消息列表
    private LiveData<DataState<List<ChatMessage>>> listData;
    //trigger:控制器
    private MutableLiveData<ChatListTrigger> listDataController = new MutableLiveData<>();

    /**
     * 仓库区
     */
    private ChatMessageRepository chatMessageRepository;
    private LocalUserRepository localUserRepository;


    /**
     * 和后台服务通信
     */
    private SocketIOClientService.JWebSocketClientBinder binder;
    private SocketIOClientService jWebSClientService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.e("ChatActivity", "服务与活动成功绑定");
            binder = (SocketIOClientService.JWebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
            markAllRead();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //服务与活动断开
            Log.e("MainActivity", "服务与活动成功断开");
        }
    };


    public ChatViewModel() {
        chatMessageRepository = ChatMessageRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
    }


    /**
     * 绑定服务
     *
     * @param from activity
     */
    public void bindService(Context from) {
        Intent bindIntent = new Intent(from, SocketIOClientService.class);
        from.bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 断开服务
     * @param from
     */
    public void unbindService(Context from){
        from.unbindService(serviceConnection);
    }

    /**
     * 初始化聊天对象
     *
     * @param conversationData 聊天对象
     */
    public void setConversationData(Conversation conversationData) {
        conversationMutableLiveData.setValue(conversationData);
    }

    /**
     * 获取页面对应的Conversation对象
     *
     * @return 结果
     */
    public MutableLiveData<Conversation> getConversationMutableLiveData() {
        return conversationMutableLiveData;
    }

    /**
     * 获取聊天列表状态数据
     * 注意：并不是存放完整的聊天列表，而是某一时刻的列表状态
     *
     * @return 状态数据
     */
    public LiveData<DataState<List<ChatMessage>>> getListData() {
        if (listData == null) {
            listData = Transformations.switchMap(listDataController, input -> {
                UserLocal local = localUserRepository.getLoggedInUserDirect();
                if (!local.isValid()) {
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                }
                if (input.getMode() == ChatListTrigger.MODE.ADD_MESSAGE) {
                    return new MutableLiveData<>(new DataState<>(Collections.singletonList(input.getNewMessage())).setListAction(DataState.LIST_ACTION.APPEND));
                } else {
                    return chatMessageRepository.getMessages(Objects.requireNonNull(local.getToken()), input.getConversationId());
                }
            });
        }
        return listData;
    }


    /**
     * 发送消息
     * @param content 消息文本
     */
    public void sendMessage(String content) {
        UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
        if (userLocal.isValid() && conversationMutableLiveData.getValue() != null) {
            ChatMessage message = new ChatMessage(userLocal.getId(),
                    conversationMutableLiveData.getValue().getFriendId(), content);
            //通知服务，发送消息
            binder.sendMessage(message);
            if (conversationMutableLiveData.getValue() != null) {
                String conId = conversationMutableLiveData.getValue().getId();
                listDataController.setValue(ChatListTrigger.getActioning(conId, message));
            }
        }
    }

    /**
     * 接受消息
     * @param message 消息
     */
    public void receiveMessage(ChatMessage message) {
        markRead(message);
        Log.e("ChatActivity","received");
        if (conversationMutableLiveData.getValue() != null) {
            if (conversationMutableLiveData.getValue() != null) {
                String conId = conversationMutableLiveData.getValue().getId();
                listDataController.setValue(ChatListTrigger.getActioning(conId, message));
            }
        }
    }

    /**
     * 控制获取完整的消息记录列表
     */
    public void fetchHistoryData() {
        if (conversationMutableLiveData.getValue() != null) {
            String conId = conversationMutableLiveData.getValue().getId();
            listDataController.setValue(ChatListTrigger.getActioning(conId));
        }
    }

    @Nullable
    public String getMyAvatar() {
        UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
        return userLocal.getAvatar();
    }

    @Nullable
    public String getMyId() {
        UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
        return userLocal.getId();
    }

    @Nullable
    public String getFriendAvatar() {
        if (conversationMutableLiveData.getValue() != null) {
            return conversationMutableLiveData.getValue().getFriendAvatar();
        }
        return null;
    }

    @Nullable
    public String getFriendId() {
        if (conversationMutableLiveData.getValue() != null) {
            return conversationMutableLiveData.getValue().getFriendId();
        }
        return null;
    }

    public void markAllRead(){
        if (conversationMutableLiveData.getValue() != null && binder!=null) {
            binder.markAllRead(conversationMutableLiveData.getValue().getId());
        }
    }

    private void markRead(ChatMessage chatMessage){
        if (binder!=null) {
            binder.markRead(chatMessage);
        }
    }
}
