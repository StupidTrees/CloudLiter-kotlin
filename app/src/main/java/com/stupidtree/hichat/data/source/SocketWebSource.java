package com.stupidtree.hichat.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import java.util.HashMap;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class SocketWebSource {

    MutableLiveData<ChatListTrigger> chatListController = new MutableLiveData<>();
    MutableLiveData<FriendStateTrigger> friendStateController = new MutableLiveData<>();
    MutableLiveData<DataState<List<ChatMessage>>> unreadMessages = new MutableLiveData<>();
    //在服务未连接时调用函数的传入参数缓存
    HashMap<String,Object> cachePool = new HashMap<>();


    public SocketWebSource(){

    }


    /**
     * 和后台服务通信
     */
    private SocketIOClientService.JWebSocketClientBinder binder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.e("ChatActivity", "服务与活动成功绑定");
            binder = (SocketIOClientService.JWebSocketClientBinder) iBinder;
            binder.setOnReceivedMessageListener(message -> {
                chatListController.postValue(ChatListTrigger.getActioning(message.getConversationId(), message));
                Log.e("unreadMessaged.add", String.valueOf(message));
                unreadMessages.postValue(new DataState<>(Collections.singletonList(message)).setListAction(DataState.LIST_ACTION.APPEND));
            });
            binder.setOnFriendStateChangeListener((friendId, online) -> friendStateController.postValue(FriendStateTrigger.getActioning(friendId,online)));
            binder.setOnUnreadFetchedListener(unread -> unreadMessages.postValue(new DataState<>(unread).setListAction(DataState.LIST_ACTION.REPLACE_ALL)));
            binder.setOnMessageReadListener((conversationId, toRemove) -> {
                Log.e("已读更新", String.valueOf(toRemove));
                unreadMessages.postValue(new DataState<>(toRemove).setListAction(DataState.LIST_ACTION.DELETE));
            });

            if(cachePool.containsKey("getIntoConversation")){
                HashMap<String, String> params = (HashMap<String, String>) cachePool.remove("getIntoConversation");
                if (params != null) {
                    getIntoConversation(params.remove("userId"), params.remove("friendId"),
                            params.remove("conversationId"));
                }
            }
            if(cachePool.containsKey("leftConversation")){
                HashMap<String, String> params = (HashMap<String, String>) cachePool.remove("leftConversation");
                if (params != null) {
                    leftConversation(params.remove("userId"),
                           params.remove("conversationId"));
                }
            }
            if (cachePool.containsKey("callOnline")){
                binder.online((UserLocal) cachePool.remove("callOnline"));
            }
            if(cachePool.containsKey("sendMessage")){
                ChatMessage cm = (ChatMessage) cachePool.remove("chatMessage");
                sendMessage(cm);
            }
            if(cachePool.containsKey("markAllRead")){
                HashMap<String, String> params = (HashMap<String, String>) cachePool.remove("markAllRead");
                if (params != null) {
                    markAllRead(params.get("userId"),
                            params.get("conversationId"));
                }
            }
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
    public void bindService(String action,Context from) {
        Intent bindIntent = new Intent(from, SocketIOClientService.class);
        bindIntent.setAction(action);
        from.bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 断开服务
     */
    public void unbindService(Context from){
        from.unbindService(serviceConnection);
    }



    public void sendMessage(ChatMessage message){
        if(binder!=null){
            binder.sendMessage(message);
        }else{
            cachePool.put("sendMessage",message);
        }
    }

    public void getIntoConversation(String userId,String friendId, String conversationId){
        if(binder!=null){
            binder.getIntoConversation(userId,friendId,conversationId);
        }else{
            HashMap<String, String> params = new HashMap<>();
            params.put("userId",userId);
            params.put("friendId",friendId);
            params.put("conversationId",conversationId);
            cachePool.put("getIntoConversation",params);
        }
    }

    public void leftConversation(String userId, String conversationId){
        if(binder!=null){
            binder.leftConversation(userId,conversationId);
        }else{
            HashMap<String, String> params = new HashMap<>();
            params.put("userId",userId);
            params.put("conversationId",conversationId);
            cachePool.put("leftConversation",params);
        }
    }

    public void markAllRead(String userId,String conversationId){
        if(binder!=null){
            binder.markAllRead(userId,conversationId);
        }else{
            HashMap<String, String> params = new HashMap<>();
            params.put("userId",userId);
            params.put("conversationId",conversationId);
            cachePool.put("markAllRead",params);
        }
    }

    public void markRead(@NonNull ChatMessage message){
        if(binder!=null){
            binder.markRead(message);
        }
    }


    public MutableLiveData<ChatListTrigger> getListController(){
        return chatListController;
    }
    public MutableLiveData<FriendStateTrigger> getFriendStateController(){
        return friendStateController;
    }

    public MutableLiveData<DataState<List<ChatMessage>>> getUnreadMessages(){
        return unreadMessages;
    }

    public void callOnline(@NonNull UserLocal user){
        if(binder!=null){
            binder.online(user);
        }else{
            cachePool.put("callOnline",user);
        }
    }
}
