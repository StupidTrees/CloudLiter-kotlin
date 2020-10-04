package com.stupidtree.hichat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.UserLocal;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * 在后台使用SocketIO保持和服务器长连接的Service
 */
public class SocketIOClientService extends Service {
    /**
     * 接收到的、未读的消息
     */
    private LinkedList<ChatMessage> incomingMessage = new LinkedList<>();

    Socket socket;
    private HashMap<String, JWebSocketClientBinder> binders = new HashMap<>();
//    private JWebSocketClientBinder mBinder = new JWebSocketClientBinder();
//

    public interface OnReceivedMessageListener {
        void OnReceivedMessage(ChatMessage message);
    }

    public interface OnFriendStateChangeListener {
        void OnFriendStateChanged(String friendId, boolean online);
    }

    public interface OnUnreadFetchedListener {
        void OnUnreadFetched(List<ChatMessage> unread);
    }

    public interface OnMessageReadListener {
        void OnMessageRead(String conversationId, List<ChatMessage> toRemove);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //连接到socketIO
        try {
            socket = IO.socket("http://hita.store:3000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        socketConn();
    }


    //连接到Server
    private void socketConn() {
        Emitter.Listener onConnectError = args -> Log.e("连接错误", Arrays.toString(args));
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on("message", args -> {
            ChatMessage chatMessage = new Gson().fromJson(args[0].toString(), ChatMessage.class);
            incomingMessage.add(chatMessage);
            Log.e("收到消息", String.valueOf(chatMessage));
            for (JWebSocketClientBinder binder : binders.values()) {
                if (binder != null) {
                    binder.onReceivedMessageListener.OnReceivedMessage(chatMessage);
                }
            }
//            if(mBinder!=null&&mBinder.onReceivedMessageListener!=null){
//                mBinder.onReceivedMessageListener.OnReceivedMessage(chatMessage);
//            }
        });
        socket.on("unread_message", args -> {
            if (args.length > 0) {
                try {
                    incomingMessage.clear();
                    JsonObject obj = new Gson().fromJson(args[0].toString(), JsonObject.class);
                    JsonArray ja = obj.get("data").getAsJsonArray();
                    for (JsonElement je : ja) {
                        JsonObject jo = je.getAsJsonObject();
                        ChatMessage cm = new Gson().fromJson(jo.toString(), ChatMessage.class);
                        incomingMessage.add(cm);
                    }

                    for (JWebSocketClientBinder binder : binders.values()) {
                        if (binder != null && binder.onUnreadFetchedListener != null) {
                            binder.onUnreadFetchedListener.OnUnreadFetched(
                                    incomingMessage);
                        }
                    }
//                    if(mBinder!=null&&mBinder.onUnreadFetchedListener!=null){
//                        mBinder.onUnreadFetchedListener.OnUnreadFetched(
//                                incomingMessage
//                        );
//                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }

            }
        });
        socket.on("query_online_result", args -> {
            try {
                String friendId = args[0].toString();
                boolean isOnline = Boolean.parseBoolean(args[1].toString());
                Log.d("查询好友在线结果", friendId + ":" + isOnline);
                for (JWebSocketClientBinder binder : binders.values()) {
                    if (binder != null && binder.onFriendStateChangeListener != null) {
                        binder.onFriendStateChangeListener.OnFriendStateChanged(friendId, isOnline);
                    }
                }
//                if (mBinder != null && mBinder.onFriendStateChangeListener != null) {
//                    mBinder.onFriendStateChangeListener.OnFriendStateChanged(friendId, isOnline);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        socket.connect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        JWebSocketClientBinder binder = new JWebSocketClientBinder();
        binders.put(intent.getAction(),binder);
        Log.e("绑定服务", String.valueOf(binders));
        return binder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        binders.remove(intent.getAction());
        Log.e("解绑服务:"+intent.getAction(), String.valueOf(binders));
        return super.onUnbind(intent);
    }

    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        OnReceivedMessageListener onReceivedMessageListener;
        OnFriendStateChangeListener onFriendStateChangeListener;
        OnUnreadFetchedListener onUnreadFetchedListener;
        OnMessageReadListener onMessageReadListener;

        public void setOnMessageReadListener(OnMessageReadListener onMessageReadListener) {
            this.onMessageReadListener = onMessageReadListener;
        }

        public void setOnReceivedMessageListener(OnReceivedMessageListener onReceivedMessageListener) {
            this.onReceivedMessageListener = onReceivedMessageListener;
        }

        public void setOnFriendStateChangeListener(OnFriendStateChangeListener onFriendStateChangeListener) {
            this.onFriendStateChangeListener = onFriendStateChangeListener;
        }

        public void setOnUnreadFetchedListener(OnUnreadFetchedListener onUnreadFetchedListener) {
            this.onUnreadFetchedListener = onUnreadFetchedListener;
        }

        public SocketIOClientService getService() {
            return SocketIOClientService.this;
        }

        /**
         * 是否上线
         */
        public boolean isConnected() {
            return socket.connected();
        }

        /**
         * 向服务器声明自己已上线
         *
         * @param userLocal 本地用户
         */
        public void online(UserLocal userLocal) {
            Log.e("请求上线", String.valueOf(userLocal));
            socket.emit("login", userLocal);
        }

        /**
         * 发送信息
         *
         * @param message 消息
         */
        public void sendMessage(ChatMessage message) {
            Log.e("消息发送", String.valueOf(message));
            socket.emit("message", message);
        }


        /**
         * 将该对话下所有消息标记为已读
         *
         * @param userId         我的id
         * @param conversationId 对话id
         */
        public void markAllRead(String userId, String conversationId) {
            //从新消息队列中把该对话下的所有消息删除
            LinkedList<ChatMessage> toDelete = new LinkedList<>();
            for (ChatMessage cm : incomingMessage) {
                if (Objects.equals(cm.getConversationId(), conversationId)) {
                    toDelete.add(cm);
                }
            }

            for(JWebSocketClientBinder binder:binders.values()){
                if (binder!=null&&binder.onMessageReadListener != null) {
                    binder.onMessageReadListener.OnMessageRead(conversationId, toDelete);
                }
            }
            incomingMessage.removeAll(toDelete);
            socket.emit("mark_all_read", userId, conversationId);
            Log.e("mark_all_read", String.valueOf(incomingMessage));
        }

        /**
         * 将某消息标记为已读
         *
         * @param message 消息
         */
        public void markRead(@NonNull ChatMessage message) {
            socket.emit("mark_read", message.getId());
            incomingMessage.remove(message);
            for(JWebSocketClientBinder binder:binders.values()){
                if (binder!=null&&binder.onMessageReadListener != null) {
                    binder.onMessageReadListener.OnMessageRead(message.getConversationId(), Collections.singletonList(message));
                }
            }
        }


        /**
         * 告诉服务器自己已进入某对话
         */
        public void getIntoConversation(@NonNull String userId, @NonNull String friendId, @NonNull String conversationId) {
            socket.emit("into_conversation", userId, friendId, conversationId);
            socket.emit("query_online", friendId);
        }

        /**
         * 告诉服务器自己已退出某对话
         */
        public void leftConversation(@NonNull String userId, @NonNull String conversationId) {
            socket.emit("left_conversation", userId, conversationId);
        }
    }
}
