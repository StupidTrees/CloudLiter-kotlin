package com.stupidtree.hichat.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.chat.ChatActivity;

import java.io.Serializable;
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

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * 在后台使用SocketIO保持和服务器长连接的Service
 */
public class SocketIOClientService extends Service {
    public static final String ACTION_RECEIVE_MESSAGE = "CLOUD_LITER_RECEIVE_MESSAGE";
    public static final String ACTION_FRIEND_STATE_CHANGED = "CLOUD_LITER_FRIEND_STATE_CHANGE";

    public static final String ACTION_INTO_CONVERSATION = "CLOUD_LITER_INTO_CONVERSATION";
    public static final String ACTION_LEFT_CONVERSATION = "CLOUD_LITER_LEFT_CONVERSATION";
    public static final String ACTION_ONLINE = "CLOUD_LITER_ONLINE";
    public static final String ACTION_OFFLINE = "CLOUD_LITER_OFFLINE";
    public static final String ACTION_MARK_ALL_READ = "CLOUD_LITER_MARK_ALL_READ";
    public static final String ACTION_MARK_READ = "CLOUD_LITER_MARK_READ";


    /**
     * 接收到的、未读的消息
     */
    private LinkedList<ChatMessage> incomingMessage = new LinkedList<>();

    Socket socket;
    BroadcastReceiver receiver;
    private HashMap<String, JWebSocketClientBinder> binders = new HashMap<>();


    public interface OnUnreadFetchedListener {
        void OnUnreadFetched(List<ChatMessage> unread);
    }

    public interface OnMessageReadListener {
        void OnMessageRead(String conversationId, List<ChatMessage> toRemove);
    }

    private void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == null) return;
                switch (intent.getAction()) {
                    case ACTION_INTO_CONVERSATION:
                        String userId = intent.getStringExtra("userId");
                        String friendId = intent.getStringExtra("friendId");
                        String conversationId = intent.getStringExtra("conversationId");
                        socket.emit("into_conversation", userId, friendId, conversationId);
                        socket.emit("query_online", friendId);
                        break;
                    case ACTION_LEFT_CONVERSATION:
                        conversationId = intent.getStringExtra("conversationId");
                        userId = intent.getStringExtra("userId");
                        socket.emit("left_conversation", userId, conversationId);
                        break;
                    case ACTION_ONLINE:
                        if (intent.getExtras() != null) {
                            UserLocal userLocal = (UserLocal) intent.getExtras().getSerializable("user");
                            Log.e("请求上线", String.valueOf(userLocal));
                            socket.emit("login", userLocal);
                        }
                        break;
                    case ACTION_OFFLINE:
                        if(intent.getStringExtra("userId")!=null){
                            socket.emit("logout",intent.getStringExtra("userId"));
                        }
                    case ACTION_MARK_ALL_READ:
                        //从新消息队列中把该对话下的所有消息删除
                        conversationId = intent.getStringExtra("conversationId");
                        userId = intent.getStringExtra("userId");
                        LinkedList<ChatMessage> toDelete = new LinkedList<>();
                        for (ChatMessage cm : incomingMessage) {
                            if (Objects.equals(cm.getConversationId(), conversationId)) {
                                toDelete.add(cm);
                            }
                        }
                        for (JWebSocketClientBinder binder : binders.values()) {
                            if (binder != null && binder.onMessageReadListener != null) {
                                binder.onMessageReadListener.OnMessageRead(conversationId, toDelete);
                            }
                        }
                        incomingMessage.removeAll(toDelete);
                        socket.emit("mark_all_read", userId, conversationId);
                        Log.e("mark_all_read", String.valueOf(incomingMessage));
                        break;
                    case ACTION_MARK_READ:
                        String messageId = intent.getStringExtra("messageId");
                        socket.emit("mark_read", messageId);
                        ChatMessage toRemove = null;
                        for (ChatMessage cm : incomingMessage) {
                            if (Objects.equals(cm.getId(), messageId)) {
                                toRemove = cm;
                            }
                        }
                        incomingMessage.remove(toRemove);
                        for (JWebSocketClientBinder binder : binders.values()) {
                            if (toRemove != null && binder != null && binder.onMessageReadListener != null) {
                                binder.onMessageReadListener.OnMessageRead(toRemove.getConversationId(), Collections.singletonList(toRemove));
                            }
                        }
                }
            }
        };
    }


    private void registerReceiver() {
        IntentFilter IF = new IntentFilter();
        IF.addAction(ACTION_INTO_CONVERSATION);
        IF.addAction(ACTION_ONLINE);
        IF.addAction(ACTION_OFFLINE);
        IF.addAction(ACTION_LEFT_CONVERSATION);
        IF.addAction(ACTION_MARK_ALL_READ);
        IF.addAction(ACTION_MARK_READ);
        registerReceiver(receiver, IF);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        initReceiver();
        registerReceiver();
        //连接到socketIO
        try {
            socket = IO.socket("http://hita.store:3000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        socketConn();
        initNotification();

        //开启心跳检测
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
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
            Intent i = new Intent(ACTION_RECEIVE_MESSAGE);
            Bundle b = new Bundle();
            b.putSerializable("message", chatMessage);
            i.putExtras(b);
            sendBroadcast(i);
            sendNotification_NewMessage(chatMessage);
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
                Intent i = new Intent(ACTION_FRIEND_STATE_CHANGED);
                i.putExtra("id", friendId);
                i.putExtra("online", isOnline);
                sendBroadcast(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        socket.connect();
    }

    private NotificationManager notificationManager;

    void initNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("cloudLiterMessageChanel", "云升", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点    channel.setLightColor(Color.RED); //小红点颜色    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知    notificationManager.createNotificationChannel(channel);}
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            notificationManager.createNotificationChannels(Collections.singletonList(channel));
            // notificationManager.createNotificationChannels();
        }

    }

    private void sendNotification_NewMessage(ChatMessage message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder notificationBuilder = new Notification.Builder(this, "cloudLiterMessageChanel");
            notificationBuilder.setSmallIcon(R.drawable.ic_logo_notification)
                    .setLargeIcon(Icon.createWithResource(getApplicationContext(), R.drawable.logo))
                    .setAutoCancel(true);

            notificationBuilder.setContentTitle(message.getContent());
            notificationBuilder.setContentText("新消息");
//            notificationBuilder.setSubText("新消息");
            UserLocal ul = LocalUserRepository.getInstance().getLoggedInUserDirect();
            if (ul.isValid()) {
                Intent i = new Intent(this, ChatActivity.class);
                i.putExtra("friendId",message.getFromId());
                notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT));
                notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT), true);
            }
            Notification n = notificationBuilder.build();
            notificationManager.notify((int) System.currentTimeMillis(), n);
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "cloudLiterMessageChanel");
            notificationBuilder.setSmallIcon(R.drawable.ic_logo_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo)) //设置通知的大图标
                    .setAutoCancel(false);//设置通知被点击一次是否自动取消
            notificationBuilder.setContentTitle(message.getContent());
            notificationBuilder.setContentText("新消息");
//            notificationBuilder.setSubText("新消息");
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

            UserLocal ul = LocalUserRepository.getInstance().getLoggedInUserDirect();
            if (ul.isValid()) {
                Intent i = new Intent(this, ChatActivity.class);
                i.putExtra("friendId",message.getFromId());
                notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT));
                notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT), true);
            }
           Notification n = notificationBuilder.build();
            notificationManager.notify((int) System.currentTimeMillis(), n);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        JWebSocketClientBinder binder = new JWebSocketClientBinder();
        binders.put(intent.getAction(), binder);
        Log.e("绑定服务", String.valueOf(binders));
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        binders.remove(intent.getAction());
//        Log.e("解绑服务:"+intent.getAction(), String.valueOf(binders));
        return super.onUnbind(intent);
    }

    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        OnUnreadFetchedListener onUnreadFetchedListener;
        OnMessageReadListener onMessageReadListener;

        public void setOnMessageReadListener(OnMessageReadListener onMessageReadListener) {
            this.onMessageReadListener = onMessageReadListener;
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
         * 发送信息
         *
         * @param message 消息
         */
        public void sendMessage(ChatMessage message) {
            Log.e("消息发送", String.valueOf(message));
            socket.emit("message", message);
        }


    }



    private static final long HEART_BEAT_RATE = 10 * 1000;//每隔10秒进行一次对长连接的心跳检测
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (socket!=null) {
                if (!socket.connected()) {
                    socket.connect();
                }
            }
            //定时对长连接进行心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

}