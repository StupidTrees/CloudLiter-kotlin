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
import com.stupidtree.hichat.socket.SocketIOClientService;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.chat.FriendStateTrigger;
import com.stupidtree.hichat.ui.chat.MessageReadNotification;

import java.sql.Timestamp;
import java.util.HashMap;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_FRIEND_STATE_CHANGED;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_INTO_CONVERSATION;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_LEFT_CONVERSATION;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_MARK_ALL_READ;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_MARK_READ;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_MESSAGE_READ;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_MESSAGE_SENT;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_ONLINE;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_RECEIVE_MESSAGE;

/**
 * 实时聊天网络资源
 * 数据来自socketio连接
 */
public class SocketWebSource extends BroadcastReceiver {


    MutableLiveData<ChatMessage> newMessageState = new MutableLiveData<>();
    MutableLiveData<FriendStateTrigger> friendStateController = new MutableLiveData<>();
    MutableLiveData<DataState<HashMap<String, Integer>>> unreadMessageState = new MutableLiveData<>();
    //消息发送结果
    MutableLiveData<DataState<ChatMessage>> messageSentState = new MutableLiveData<>();
    //消息已读通知
    MutableLiveData<DataState<MessageReadNotification>> messageReadState = new MutableLiveData<>();

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
                        newMessageState.setValue(message);
//                        unreadMessages.setValue(new DataState<>(Collections.singletonList(message)).setListAction(DataState.LIST_ACTION.APPEND));
                        HashMap<String,Integer> map = new HashMap<>();
                        map.put(message.getConversationId(),1);
                        unreadMessageState.setValue(new DataState<>(map).setListAction(DataState.LIST_ACTION.APPEND));
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

            case ACTION_MESSAGE_SENT:
                if(intent.getExtras() != null){
                    ChatMessage message = (ChatMessage) intent.getExtras().getSerializable("message");
                    Log.e("SocketWebSource-消息已发送", String.valueOf(message));
                    if(message!=null){
                        messageSentState.setValue(new DataState<>(message));
                    }
                }
                break;
            case ACTION_MESSAGE_READ:
                if(intent.getExtras()!=null){
                    MessageReadNotification notification = (MessageReadNotification) intent.getExtras().getSerializable("read");
                    Log.e("SocketWebSource-消息被读", String.valueOf(notification));
                    if(notification!=null){
                        messageReadState.setValue(new DataState<>(notification));
                    }
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
            binder.setOnUnreadFetchedListener(unread -> {
                Log.e("获取未读消息", String.valueOf(unread));
                unreadMessageState.postValue(new DataState<>(unread).setListAction(DataState.LIST_ACTION.REPLACE_ALL));
            });
            binder.setOnMessageReadListener(map -> {
                Log.e("已读更新", String.valueOf(map));
                unreadMessageState.postValue(new DataState<>(map).setListAction(DataState.LIST_ACTION.DELETE));
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


    public MutableLiveData<ChatMessage> getNewMessageState() {
        return newMessageState;
    }

    public MutableLiveData<FriendStateTrigger> getFriendStateController() {
        return friendStateController;
    }


    public MutableLiveData<DataState<HashMap<String, Integer>>> getUnreadMessageState() {
        return unreadMessageState;
    }

    public void callOnline(@NonNull Context context, @NonNull UserLocal user) {
        Intent i = new Intent(ACTION_ONLINE);
        i.putExtra("userId",user.getId());
        context.sendBroadcast(i);
    }

    public void markAllRead(@NonNull Context context, String userId, String conversationId, Timestamp topTime, int num) {
        Intent i = new Intent(ACTION_MARK_ALL_READ);
        i.putExtra("userId", userId);
        i.putExtra("topTime",topTime.getTime());
        i.putExtra("num",num);
        i.putExtra("conversationId", conversationId);
        context.sendBroadcast(i);
    }

    public void markRead(@NonNull Context context,@NonNull String userId, @NonNull String messageId, @NonNull String conversationId) {
        Intent i = new Intent(ACTION_MARK_READ);
        i.putExtra("userId",userId);
        i.putExtra("messageId", messageId);
        i.putExtra("conversationId", conversationId);
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

    public MutableLiveData<DataState<ChatMessage>> getMessageSentSate(){
        return messageSentState;
    }

    public MutableLiveData<DataState<MessageReadNotification>> getMessageReadState() {
        return messageReadState;
    }
}
