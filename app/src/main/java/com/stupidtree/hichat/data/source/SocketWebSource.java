package com.stupidtree.hichat.data.source;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.service.SocketIOClientService;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.chat.ChatListTrigger;
import com.stupidtree.hichat.ui.chat.FriendStateTrigger;

import java.util.Collections;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_FRIEND_STATE_CHANGED;
import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_INTO_CONVERSATION;
import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_LEFT_CONVERSATION;
import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_MARK_ALL_READ;
import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_MARK_READ;
import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_ONLINE;
import static com.stupidtree.hichat.service.SocketIOClientService.ACTION_RECEIVE_MESSAGE;

/**
 * 实时聊天网络资源
 * 数据来自socketio连接
 */
public class SocketWebSource extends BroadcastReceiver {


    MutableLiveData<ChatListTrigger> chatListController = new MutableLiveData<>();
    MutableLiveData<FriendStateTrigger> friendStateController = new MutableLiveData<>();
    MutableLiveData<DataState<List<ChatMessage>>> unreadMessages = new MutableLiveData<>();


    public SocketWebSource() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        switch (intent.getAction()) {
            case ACTION_RECEIVE_MESSAGE:
                if (intent.getExtras() != null) {
                    ChatMessage message = (ChatMessage) intent.getExtras().getSerializable("message");
                    Log.e("unreadMessaged.add", String.valueOf(message));
                    if (message != null) {
                        chatListController.setValue(ChatListTrigger.getActioning(message.getConversationId(), message));
                        unreadMessages.setValue(new DataState<>(Collections.singletonList(message)).setListAction(DataState.LIST_ACTION.APPEND));
                    }
                }
                break;
            case ACTION_FRIEND_STATE_CHANGED:
                if (intent.hasExtra("id") && intent.hasExtra("online")) {
                    friendStateController.setValue(FriendStateTrigger.getActioning(
                            intent.getStringExtra("id"), intent.getStringExtra("online")
                    ));
                }
                break;

        }
    }

    /**
     * 和后台服务通信
     */
    private SocketIOClientService.JWebSocketClientBinder binder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.e("ChatActivity", "服务与活动成功绑定");
            binder = (SocketIOClientService.JWebSocketClientBinder) iBinder;
            binder.setOnUnreadFetchedListener(unread -> unreadMessages.postValue(new DataState<>(unread).setListAction(DataState.LIST_ACTION.REPLACE_ALL)));
            binder.setOnMessageReadListener((conversationId, toRemove) -> {
                Log.e("已读更新", String.valueOf(toRemove));
                unreadMessages.postValue(new DataState<>(toRemove).setListAction(DataState.LIST_ACTION.DELETE));
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //服务与活动断开
            Log.e("ChatActivity", "服务与活动成功断开");
        }
    };


    /**
     * 绑定服务
     *
     * @param from activity
     */
    public void bindService(String action, Context from) {
        Intent bindIntent = new Intent(from, SocketIOClientService.class);
        bindIntent.setAction(action);
        from.bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 断开服务
     */
    public void unbindService(Context from) {
        from.unbindService(serviceConnection);
    }


    public void sendMessage(ChatMessage message) {
        if (binder != null) {
            binder.sendMessage(message);
        }
    }


    public MutableLiveData<ChatListTrigger> getListController() {
        return chatListController;
    }

    public MutableLiveData<FriendStateTrigger> getFriendStateController() {
        return friendStateController;
    }

    public MutableLiveData<DataState<List<ChatMessage>>> getUnreadMessages() {
        return unreadMessages;
    }

    public void callOnline(@NonNull Context context, @NonNull UserLocal user) {
        Intent i = new Intent(ACTION_ONLINE);
        Bundle b = new Bundle();
        b.putSerializable("user", user);
        i.putExtras(b);
        context.sendBroadcast(i);
//        if(binder!=null){
//            binder.online(user);
//        }else{
//            cachePool.put("callOnline",user);
//        }
    }

    public void markAllRead(@NonNull Context context, String userId, String conversationId) {
        Intent i = new Intent(ACTION_MARK_ALL_READ);
        i.putExtra("userId", userId);
        i.putExtra("conversationId", conversationId);
        context.sendBroadcast(i);
    }

    public void markRead(@NonNull Context context, @NonNull String messageId) {
        Intent i = new Intent(ACTION_MARK_READ);
        i.putExtra("messageId", messageId);
        context.sendBroadcast(i);
    }

    public void getIntoConversation(Context context, String userId, String friendId, String conversationId) {
        Intent i = new Intent(ACTION_INTO_CONVERSATION);
        i.putExtra("userId", userId);
        i.putExtra("friendId", friendId);
        i.putExtra("conversationId", conversationId);
        context.sendBroadcast(i);
    }

    public void leftConversation(@NonNull Context context, String userId, String conversationId) {
        Intent i = new Intent(ACTION_LEFT_CONVERSATION);
        i.putExtra("userId", userId);
        i.putExtra("conversationId", conversationId);
        context.sendBroadcast(i);
    }
}
