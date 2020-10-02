package com.stupidtree.hichat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * 在后台使用SocketIO保持和服务器长连接的Service
 */
public class SocketIOClientService extends Service {
    public static String ACTION_RECEIVE_MESSAGE = "CLOUD_LITER_RECEIVE_MESSAGE";

    /**
     * 接收到的、未读的消息
     */
    private LinkedList<ChatMessage> incomingMessage = new LinkedList<>();

    Socket socket;
    private JWebSocketClientBinder mBinder = new JWebSocketClientBinder();


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
        socket.on(Socket.EVENT_CONNECT_TIMEOUT,onConnectError);
        socket.on("message", args -> {
            Log.e("message","收到消息");
            Intent i = new Intent(ACTION_RECEIVE_MESSAGE);
            i.putExtra("message",args[0].toString());
            incomingMessage.add(new Gson().fromJson(args[0].toString(),ChatMessage.class));
            //LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            sendBroadcast(i);
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
        return mBinder;
    }



    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        public SocketIOClientService getService() {
            return SocketIOClientService.this;
        }

        /**
         * 是否上线
         * @return
         */
        public boolean isConnected(){
            return socket.connected();
        }

        /**
         * 向服务器声明自己已上线
         * @param userLocal 本地用户
         */
        public void online(UserLocal userLocal){
            Log.e("请求上线", String.valueOf(userLocal));
            socket.emit("login",userLocal);
        }

        /**
         * 发送信息
         * @param message 消息
         */
        public void sendMessage(ChatMessage message){
            Log.e("消息发送", String.valueOf(message));
            socket.emit("message",message);
        }

        /**
         * 将该对话下所有消息标记为已读
         * @param conversationId
         */
        public void markAllRead(String conversationId){
            //从新消息队列中把该对话下的所有消息删除
            LinkedList<ChatMessage> toDelete = new LinkedList<>();
            for(ChatMessage cm:incomingMessage){
                if(Objects.equals(cm.getConversationId(),conversationId)){
                    toDelete.add(cm);
                }
            }
            incomingMessage.removeAll(toDelete);
            Log.e("mark_read", String.valueOf(incomingMessage));
        }

        /**
         * 将某消息标记为已读
         * @param message
         */
        public void markRead(ChatMessage message){
            incomingMessage.remove(message);
        }

        /**
         * 判断某对话是否存在未读消息
         * @param conversation
         * @return
         */
        public int getUnread(Conversation conversation){
            Log.e("get_unread", String.valueOf(incomingMessage));
            int res = 0;
            for(ChatMessage cm:incomingMessage){
                if(Objects.equals(cm.getConversationId(),conversation.getId())){
                    res++;
                }
            }
            return res;
        }
    }
}
