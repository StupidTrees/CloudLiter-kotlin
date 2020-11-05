package com.stupidtree.cloudliter.service.socket

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.request.target.NotificationTarget
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.ApiResponse
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.chat.MessageReadNotification
import com.stupidtree.cloudliter.ui.widgets.EmoticonsTextView
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException
import java.sql.Timestamp
import java.util.*

/**
 * 在后台使用SocketIO保持和服务器长连接的Service
 */
class SocketIOClientService : Service() {
    //正在与谁进行对话
    private var currentFriendId: String? = null

    /**
     * 各个对话的未读消息数记录
     */
    private val incomingMessage = HashMap<String, Int>()
    var socket: Socket? = null
    var receiver: BroadcastReceiver? = null
    private val binders = HashMap<String?, JWebSocketClientBinder>()

    interface OnUnreadFetchedListener {
        fun onUnreadFetched(unread: HashMap<String, Int>)
    }

    interface OnMessageReadListener {
        fun onMessageRead(map: HashMap<String, Int>)
    }

    private fun initReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == null) return
                when (intent.action) {
                    ACTION_INTO_CONVERSATION -> {
                        val userId = intent.getStringExtra("userId")
                        val friendId = intent.getStringExtra("friendId")
                        val conversationId = intent.getStringExtra("conversationId")
                        socket!!.emit("into_conversation", userId, friendId, conversationId)
                        socket!!.emit("query_online", userId, friendId)
                        currentFriendId = friendId
                    }
                    ACTION_LEFT_CONVERSATION -> {
                        val conversationId = intent.getStringExtra("conversationId")
                        val userId = intent.getStringExtra("userId")
                        socket!!.emit("left_conversation", userId, conversationId)
                        currentFriendId = null
                    }
                    ACTION_ONLINE -> if (intent.getStringExtra("userId") != null) {
                        val id = intent.getStringExtra("userId")
                        Log.e("请求上线", id.toString())
                        socket!!.emit("login", id)
                    }
                    ACTION_OFFLINE -> {
                        if (intent.getStringExtra("userId") != null) {
                            socket!!.emit("logout", intent.getStringExtra("userId"))
                        }
                    }
                    ACTION_MARK_ALL_READ -> {
                        //从新消息队列中把该对话下的所有消息删除
                        val conversationId = intent.getStringExtra("conversationId")
                        val topTime = intent.getLongExtra("topTime", -1)
                        val num = intent.getIntExtra("num", -1)
                        val userId = intent.getStringExtra("userId")
                        Log.e("mark_all_read", incomingMessage.toString())
                        for (binder in binders.values) {
                            if (binder.onMessageReadListener != null) {
                                val map = HashMap<String, Int>()
                                map[conversationId!!] = num
                                binder.onMessageReadListener!!.onMessageRead(map)
                            }
                        }
                        val oldCount0 = incomingMessage[conversationId]
                        if (oldCount0 != null && oldCount0 <= num) {
                            incomingMessage.remove(conversationId)
                        } else if (oldCount0 != null) {
                            incomingMessage[conversationId!!] = oldCount0 - num
                        }
                        incomingMessage.remove(conversationId)
                        if (topTime > 0) {
                            socket!!.emit("mark_all_read", userId, conversationId, topTime)
                        }
                    }
                    ACTION_MARK_READ -> {
                        val messageId = intent.getStringExtra("messageId")
                        val userId = intent.getStringExtra("userId")
                        val conversationId = intent.getStringExtra("conversationId")
                        Log.e("emit_mark_read", userId + "," + conversationId + "," + messageId)
                        socket!!.emit("mark_read", userId, conversationId, messageId)
                        val oldCount = incomingMessage[conversationId]
                        if (oldCount != null && oldCount <= 1) {
                            incomingMessage.remove(conversationId)
                        } else if (oldCount != null) {
                            incomingMessage[conversationId!!] = oldCount - 1
                        }
                        for (binder in binders.values) {
                            if (binder.onMessageReadListener != null) {
                                val map = HashMap<String, Int>()
                                map[conversationId!!] = 1
                                binder.onMessageReadListener!!.onMessageRead(map)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerReceiver() {
        val IF = IntentFilter()
        IF.addAction(ACTION_INTO_CONVERSATION)
        IF.addAction(ACTION_ONLINE)
        IF.addAction(ACTION_OFFLINE)
        IF.addAction(ACTION_LEFT_CONVERSATION)
        IF.addAction(ACTION_MARK_ALL_READ)
        IF.addAction(ACTION_MARK_READ)
        registerReceiver(receiver, IF)
    }

    override fun onCreate() {
        super.onCreate()
        initReceiver()
        registerReceiver()
        //连接到socketIO
        socket = try {
            IO.socket("http://hita.store:3000")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
        socketConn()
        initNotification()

        //开启心跳检测
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE)
    }

    //连接到Server
    private fun socketConn() {
        val onConnectError = Emitter.Listener { args: Array<Any?>? -> Log.e("连接错误", Arrays.toString(args)) }
        socket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socket!!.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        socket!!.on("message") { args: Array<Any> ->
            val chatMessage = Gson().fromJson(args[0].toString(), ChatMessage::class.java)
            val oldCount = incomingMessage[chatMessage.conversationId]
            if (oldCount == null) {
                incomingMessage[chatMessage.conversationId!!] = 1
            } else {
                incomingMessage[chatMessage.conversationId!!] = oldCount + 1
            }
            Log.e("收到消息", chatMessage.toString())
            val i = Intent(ACTION_RECEIVE_MESSAGE)
            val b = Bundle()
            b.putSerializable("message", chatMessage)
            i.putExtras(b)
            sendBroadcast(i)
            //当前聊天的新消息，不发送通知
            if (currentFriendId != chatMessage.fromId) {
                sendNotification_NewMessage(chatMessage)
            }
        }
        //消息发送成功
        socket!!.on("message_sent") { args: Array<Any> ->
            val chatMessage = Gson().fromJson(args[0].toString(), ChatMessage::class.java)
            Log.e("sent", chatMessage.toString())
            val i = Intent(ACTION_MESSAGE_SENT)
            val b = Bundle()
            b.putSerializable("message", chatMessage)
            i.putExtras(b)
            sendBroadcast(i)
        }
        socket!!.on("friend_read_all") { args: Array<Any> ->
            if (args.size == 3) {
                try {
                    val userId = args[0].toString()
                    val conversationId = args[1].toString()
                    val fromTime = Timestamp(args[2].toString().toLong())
                    Log.e("messageAllRead", "$userId-$fromTime")
                    val i = Intent(ACTION_MESSAGE_READ)
                    val b = Bundle()
                    b.putSerializable("read", MessageReadNotification(userId, conversationId, fromTime))
                    i.putExtras(b)
                    sendBroadcast(i)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        socket!!.on("friend_read_one") { args: Array<Any> ->
            if (args.size == 3) {
                try {
                    val userId = args[0].toString()
                    val conversationId = args[1].toString()
                    val id = args[2].toString()
                    Log.e("messageOneRead", "$userId-$id")
                    val i = Intent(ACTION_MESSAGE_READ)
                    val b = Bundle()
                    b.putSerializable("read", MessageReadNotification(userId, conversationId, id))
                    i.putExtras(b)
                    sendBroadcast(i)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        socket!!.on("unread_message") { args: Array<Any> ->
            if (args.size > 0) {
                try {
                    try {
                        incomingMessage.clear()
                        val jo: ApiResponse<*> = Gson().fromJson<ApiResponse<*>>(args[0].toString(), ApiResponse::class.java)
                        Log.e("推送未读消息", jo.toString())
                        val m: HashMap<String,*> = Gson().fromJson<HashMap<String, *>>(jo.data.toString(), HashMap::class.java)
                        for ((key, value) in m) {
                            incomingMessage[key] = value.toString().toFloat().toInt()
                        }
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                    for (binder in binders.values) {
                        if (binder.onUnreadFetchedListener != null) {
                            binder.onUnreadFetchedListener!!.onUnreadFetched(
                                    incomingMessage)
                        }
                    }
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
        }
        socket!!.on("query_online_result") { args: Array<Any> ->
            try {
                val friendId = args[0].toString()
                val isOnline = args[1].toString()
                Log.d("查询好友在线结果", "$friendId:$isOnline")
                val i = Intent(ACTION_FRIEND_STATE_CHANGED)
                i.putExtra("id", friendId)
                i.putExtra("online", isOnline)
                sendBroadcast(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        socket!!.connect()
    }

    private var notificationManager: NotificationManager? = null
    fun initNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("cloudLiterMessageChanel", "云升", NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true) //是否在桌面icon右上角展示小红点    channel.setLightColor(Color.RED); //小红点颜色    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知    notificationManager.createNotificationChannel(channel);}
            channel.importance = NotificationManager.IMPORTANCE_DEFAULT
            channel.enableVibration(false)
            channel.vibrationPattern = longArrayOf(0)
            channel.setSound(null, null)
            notificationManager!!.createNotificationChannels(listOf(channel))
            // notificationManager.createNotificationChannels();
        }
    }

    /**
     * 发送新消息到来的系统通知
     *
     * @param message 消息对象
     */
    private fun sendNotification_NewMessage(message: ChatMessage) {
        val rv = RemoteViews(packageName, R.layout.remote_notification)
        val notificationId = System.currentTimeMillis().toInt()
        rv.setImageViewResource(R.id.logo, R.drawable.logo)
        //将消息中的表情替换为文字
        var newContent = message.content
        val pattern = EmoticonsTextView.buildPattern()
        val matcher = pattern.matcher(message.content)
        while (matcher.find()) {
            val faceText = matcher.group()
            newContent = newContent!!.replace(faceText, getString(R.string.place_holder_yunmoji))
        }
        if (message.getType() === ChatMessage.TYPE.IMG) {
            newContent = getString(R.string.place_holder_image)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationBuilder = Notification.Builder(this, "cloudLiterMessageChanel")
            notificationBuilder.setSmallIcon(R.drawable.ic_logo_notification)
                    .setLargeIcon(Icon.createWithResource(applicationContext, R.drawable.logo))
                    .setAutoCancel(true)
            notificationBuilder.setCustomContentView(rv)
            rv.setTextViewText(R.id.content, message.friendRemark)
            rv.setTextViewText(R.id.title, newContent)
            val ul = LocalUserRepository.getInstance(application).getLoggedInUser()
            if (ul.isValid) {
                val i = ActivityUtils.getIntentForChatActivity(this, message)
                notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT))
                notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT), true)
            }
            val n = notificationBuilder.build()
            val notificationTarget = NotificationTarget(this, R.id.avatar, rv, n, notificationId)
            ImageUtils.loadAvatarIntoNotification(this, message.friendAvatar!!, notificationTarget)
            notificationManager!!.notify(notificationId, n)
        } else {
            val notificationBuilder = NotificationCompat.Builder(this, "cloudLiterMessageChanel")
            notificationBuilder.setSmallIcon(R.drawable.ic_logo_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo)) //设置通知的大图标
                    .setAutoCancel(true) //设置通知被点击一次是否自动取消
            notificationBuilder.setContent(rv)
            rv.setTextViewText(R.id.title, newContent)
            rv.setTextViewText(R.id.content, message.friendRemark)
            notificationBuilder.priority = Notification.PRIORITY_HIGH
            val ul = LocalUserRepository.getInstance(application).getLoggedInUser()
            if (ul.isValid) {
                val i = ActivityUtils.getIntentForChatActivity(this, message)
                notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT))
                notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT), true)
            }
            val n = notificationBuilder.build()
            val notificationTarget = NotificationTarget(this, R.id.avatar, rv, n, notificationId)
            ImageUtils.loadAvatarIntoNotification(this, message.friendAvatar!!, notificationTarget)
            notificationManager!!.notify(notificationId, n)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket!!.disconnect()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent): IBinder? {
        val binder = JWebSocketClientBinder()
        binders[intent.action] = binder
        Log.e("绑定服务", binders.toString())
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onUnbind(intent: Intent): Boolean {
//        binders.remove(intent.getAction());
//        Log.e("解绑服务:"+intent.getAction(), String.valueOf(binders));
        return super.onUnbind(intent)
    }

    //用于Activity和service通讯
    inner class JWebSocketClientBinder : Binder() {
        var onUnreadFetchedListener: OnUnreadFetchedListener? = null
        var onMessageReadListener: OnMessageReadListener? = null


        val service: SocketIOClientService
            get() = this@SocketIOClientService

        /**
         * 是否上线
         */
        val isConnected: Boolean
            get() = socket!!.connected()

        /**
         * 发送信息
         *
         * @param message 消息
         */
        fun sendMessage(message: ChatMessage) {
            Log.e("消息发送", message.toString())
            socket!!.emit("message", message)
        }
    }

    //重连调用可以在主线程中进行
    private val mHandler = Handler(Looper.getMainLooper())
    private val heartBeatRunnable: Runnable = object : Runnable {
        override fun run() {
            if (socket != null) {
                if (!socket!!.connected()) {
                    socket!!.connect()
                }
            }
            //定时对长连接进行心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE)
        }
    }

    companion object {
        const val ACTION_RECEIVE_MESSAGE = "CLOUD_LITER_RECEIVE_MESSAGE"
        const val ACTION_FRIEND_STATE_CHANGED = "CLOUD_LITER_FRIEND_STATE_CHANGE"
        const val ACTION_MESSAGE_SENT = "CLOUD_LITER_MESSAGE_SENT"
        const val ACTION_MESSAGE_READ = "CLOUD_LITER_MESSAGE_READ"
        const val ACTION_INTO_CONVERSATION = "CLOUD_LITER_INTO_CONVERSATION"
        const val ACTION_LEFT_CONVERSATION = "CLOUD_LITER_LEFT_CONVERSATION"
        const val ACTION_ONLINE = "CLOUD_LITER_ONLINE"
        const val ACTION_OFFLINE = "CLOUD_LITER_OFFLINE"
        const val ACTION_MARK_ALL_READ = "CLOUD_LITER_MARK_ALL_READ"
        const val ACTION_MARK_READ = "CLOUD_LITER_MARK_READ"
        private const val HEART_BEAT_RATE = 10 * 1000 //每隔10秒进行一次对长连接的心跳检测
                .toLong()
    }
}