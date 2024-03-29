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
import com.stupidtree.component.web.ApiResponse
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.chat.MessageReadNotification
import com.stupidtree.cloudliter.ui.widgets.EmoticonsTextView
import com.stupidtree.cloudliter.ui.wordcloud.WordCloudEntity
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import java.net.URISyntaxException
import java.sql.Timestamp
import java.util.*

/**
 * 在后台使用SocketIO保持和服务器长连接的Service
 */
class SocketIOClientService : Service() {
    //正在与谁进行对话
    private var currentConversation: String? = null

    /**
     * 各个对话的未读消息数记录
     */
    private val incomingMessage = HashMap<String, Int>()
    var socket: Socket? = null
    var receiver: BroadcastReceiver? = null

    private fun initReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == null) return
                when (intent.action) {
                    ACTION_INTO_CONVERSATION -> {
                        val userId = intent.getStringExtra("userId")
                        val conversationId = intent.getStringExtra("conversationId")
                        socket?.emit("into_conversation", userId, conversationId)
                        socket?.emit("query_online", userId, conversationId)
                        currentConversation = conversationId
                    }
                    ACTION_LEFT_CONVERSATION -> {
                        val conversationId = intent.getStringExtra("conversationId")
                        val userId = intent.getStringExtra("userId")
                        socket?.emit("left_conversation", userId, conversationId)
                        currentConversation = null
                    }
                    ACTION_ONLINE -> if (intent.getStringExtra("userId") != null) {
                        val id = intent.getStringExtra("userId")
                        loggedInUserId = id
                        Log.e("请求上线", id.toString())
                        socket?.emit("login", id)
                    }
                    ACTION_OFFLINE -> {
                        loggedInUserId = null
                        if (intent.getStringExtra("userId") != null) {
                            socket?.emit("logout", intent.getStringExtra("userId"))
                        }
                    }
                    ACTION_MARK_ALL_READ -> {
                        //从新消息队列中把该对话下的所有消息删除
                        val type = intent.getStringExtra("type")
                        val conversationId = intent.getStringExtra("conversationId")
                        val topTime = intent.getLongExtra("topTime", -1)
                        val num = intent.getIntExtra("num", -1)
                        val userId = intent.getStringExtra("userId")
                        val oldCount0 = incomingMessage[conversationId]
                        if (oldCount0 != null && oldCount0 <= num) {
                            incomingMessage.remove(conversationId)
                        } else if (oldCount0 != null) {
                            incomingMessage[conversationId ?: ""] = oldCount0 - num
                        }
                        incomingMessage.remove(conversationId)
                        if (topTime > 0) {
                            socket?.emit("mark_all_read", type, userId, conversationId, topTime)
                        }
                    }
                    ACTION_MARK_READ -> {
                        val type = intent.getStringExtra("type")
                        val messageId = intent.getStringExtra("messageId")
                        val userId = intent.getStringExtra("userId")
                        val conversationId = intent.getStringExtra("conversationId")
                        Log.e("emit_mark_read", "$userId,$conversationId,$messageId")
                        socket?.emit("mark_read", type, userId, conversationId, messageId)
                        val oldCount = incomingMessage[conversationId]
                        if (oldCount != null && oldCount <= 1) {
                            incomingMessage.remove(conversationId)
                        } else if (oldCount != null) {
                            incomingMessage[conversationId ?: ""] = oldCount - 1
                        }
                    }
                }
            }
        }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_INTO_CONVERSATION)
        intentFilter.addAction(ACTION_ONLINE)
        intentFilter.addAction(ACTION_OFFLINE)
        intentFilter.addAction(ACTION_LEFT_CONVERSATION)
        intentFilter.addAction(ACTION_MARK_ALL_READ)
        intentFilter.addAction(ACTION_MARK_READ)
        registerReceiver(receiver, intentFilter)
    }

    override fun onCreate() {
        super.onCreate()
        initReceiver()
        //连接到socketIO
        socket = try {
            IO.socket("http://hita.store:3000")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
        socketConn()
        initNotification()
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.e("startCommand", this.toString())
        registerReceiver()
        mHeartbeatHandler.removeCallbacks(heartBeatRunnable)
        mHeartbeatHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("service_destroy", "DS!")
        socket?.disconnect()
        unregisterReceiver(receiver)
    }


    //连接到Server
    private fun socketConn() {
        val onConnectError = Emitter.Listener { args: Array<Any?>? -> Log.e("连接错误", Arrays.toString(args)) }
        socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socket?.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        socket?.on("message") { args: Array<Any> ->
            val chatMessage = Gson().fromJson(args[0].toString(), ChatMessage::class.java)
            val oldCount = incomingMessage[chatMessage.conversationId]
            if (oldCount == null) {
                incomingMessage[chatMessage.conversationId] = 1
            } else {
                incomingMessage[chatMessage.conversationId] = oldCount + 1
            }
            Log.e("收到消息", "$chatMessage,current:$currentConversation")
            val i = Intent(RECEIVE_RECEIVE_MESSAGE)
            val b = Bundle()
            b.putSerializable("message", chatMessage)
            i.putExtras(b)
            sendBroadcast(i)
            //当前聊天的新消息，不发送通知
            if (currentConversation != chatMessage.conversationId) {
                sendNotificationNewMessage(chatMessage)
            }
        }
        socket?.on("friend_read_all") { args: Array<Any> ->
            if (args.size == 4) {
                try {
                    val userId = args[0].toString()
                    val conversationId = args[1].toString()
                    val fromTime = Timestamp(args[2].toString().toLong())
                    val info = JSONArray(args[3].toString())
                    Log.e("messageAllRead", "$userId-$fromTime")
                    val i = Intent(RECEIVE_MESSAGE_READ)
                    val b = Bundle()
                    val notify = MessageReadNotification(userId, conversationId, fromTime)
                    for (idx in 0 until info.length()) {
                        val obj = info.optJSONObject(idx)
                        notify.messageInfo[obj.optString("messageId")] = obj.optInt("read")
                    }
                    b.putSerializable("read", notify)
                    i.putExtras(b)
                    sendBroadcast(i)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        socket?.on("friend_read_one") { args: Array<Any> ->
            if (args.size == 4) {
                try {
                    val userId = args[0].toString()
                    val conversationId = args[1].toString()
                    val id = args[2].toString()
                    val info = JSONArray(args[3].toString())
                    Log.e("messageOneRead", "$userId-$id")
                    val i = Intent(RECEIVE_MESSAGE_READ)
                    val b = Bundle()
                    val notify = MessageReadNotification(userId, conversationId, id)
                    for (idx in 0 until info.length()) {
                        val obj = info.optJSONObject(idx)
                        notify.messageInfo[obj.optString("messageId")] = obj.optInt("read")
                    }
                    b.putSerializable("read", notify)
                    i.putExtras(b)
                    sendBroadcast(i)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        socket?.on("relation_event") { _: Array<Any> ->
            val i = Intent(RECEIVE_RELATION_EVENT)
            sendBroadcast(i)
        }
        socket?.on("unread_message") { args: Array<Any> ->
            if (args.isNotEmpty()) {
                try {
                    incomingMessage.clear()
                    val jo: ApiResponse<*> = Gson().fromJson(args[0].toString(), ApiResponse::class.java)
                    Log.e("推送未读消息", jo.toString())
                    val m: HashMap<String, *> = Gson().fromJson<HashMap<String, *>>(jo.data.toString(), HashMap::class.java)
                    for ((key, value) in m) {
                        incomingMessage[key] = value.toString().toFloat().toInt()
                    }
                    val i = Intent(RECEIVE_UNREAD_MESSAGE)
                    val b = Bundle()
                    b.putSerializable("map", incomingMessage)
                    i.putExtras(b)
                    sendBroadcast(i)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }
        socket?.on("query_online_result") { args: Array<Any> ->
            try {
                val conversationId = args[0].toString()
                val isOnline = args[1].toString()
                Log.d("查询好友在线结果", "$conversationId:$isOnline")
                val i = Intent(RECEIVE_FRIEND_STATE_CHANGED)
                i.putExtra("conversationId", conversationId)
                i.putExtra("online", isOnline)
                sendBroadcast(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        socket?.on("conversation_topic") { args ->
            try {
                val conversationId = args[0].toString()
                val topTopic = args[1].toString()
                Log.d("对话话题更新", "$conversationId:$topTopic")
                val i = Intent(RECEIVE_CONVERSATION_TOPIC)
                i.putExtra("conversationId", conversationId)
                i.putExtra("topTopics", topTopic)
                sendBroadcast(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        socket?.connect()
    }

    private var notificationManager: NotificationManager? = null
    private fun initNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("cloudLiterMessageChanel", "云升", NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true) //是否在桌面icon右上角展示小红点    channel.setLightColor(Color.RED); //小红点颜色    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知    notificationManager.createNotificationChannel(channel);}
            channel.importance = NotificationManager.IMPORTANCE_DEFAULT
            channel.enableVibration(false)
            channel.vibrationPattern = longArrayOf(0)
            channel.setSound(null, null)
            notificationManager?.createNotificationChannels(listOf(channel))
            // notificationManager.createNotificationChannels();
        }
    }

    /**
     * 发送新消息到来的系统通知
     *
     * @param message 消息对象
     */
    private fun sendNotificationNewMessage(message: ChatMessage) {
        val rv = RemoteViews(packageName, R.layout.remote_notification)
        val notificationId = System.currentTimeMillis().toInt()
        rv.setImageViewResource(R.id.logo, R.drawable.logo)
        //将消息中的表情替换为文字
        var newContent = message.content
        val pattern = EmoticonsTextView.buildPattern()
        val matcher = pattern.matcher(message.content ?: "")
        while (matcher.find()) {
            val faceText = matcher.group()
            newContent = newContent?.replace(faceText, getString(R.string.place_holder_yunmoji))
        }
        if (message.getTypeEnum() === ChatMessage.TYPE.IMG) {
            newContent = getString(R.string.place_holder_image)
        } else if (message.getTypeEnum() == ChatMessage.TYPE.VOICE) {
            newContent = getString(R.string.place_holder_voice)
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
                val i = ActivityUtils.getIntentForChatActivity(this, message.conversationId)
                notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT))
                notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT), true)
            }
            val n = notificationBuilder.build()
            val notificationTarget = NotificationTarget(this, R.id.avatar, rv, n, notificationId)
            message.fromId?.let { ImageUtils.loadAvatarIntoNotification(this, it, notificationTarget) }
            notificationManager?.notify(notificationId, n)
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
                val i = ActivityUtils.getIntentForChatActivity(this, message.conversationId)
                notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT))
                notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT), true)
            }
            val n = notificationBuilder.build()
            val notificationTarget = NotificationTarget(this, R.id.avatar, rv, n, notificationId)
            ImageUtils.loadAvatarIntoNotification(this, message.fromId
                    ?: "", notificationTarget)
            notificationManager?.notify(notificationId, n)
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    //重连调用可以在主线程中进行
    private val mHandlerThread = HandlerThread("heartbeat_thread")

    init {
        mHandlerThread.start()
    }

    private var mHeartbeatHandler = Handler(mHandlerThread.looper)
    private val heartBeatRunnable = object : Runnable {
        override fun run() {
            socket?.connect()
            Log.e("心跳重连", "--")
            if (loggedInUserId != null) {
                socket?.emit("login", loggedInUserId)
            }
            mHeartbeatHandler.postDelayed(this, HEART_BEAT_RATE)
        }

    }

    companion object {
        const val RECEIVE_RECEIVE_MESSAGE = "CLOUD_LITER_RECEIVE_MESSAGE"
        const val RECEIVE_FRIEND_STATE_CHANGED = "CLOUD_LITER_FRIEND_STATE_CHANGE"
        const val RECEIVE_MESSAGE_READ = "CLOUD_LITER_MESSAGE_READ"
        const val RECEIVE_RELATION_EVENT = "CLOUD_LITER_RELATION_EVENT"
        const val RECEIVE_UNREAD_MESSAGE = "CLOUD_LITER_UNREAD_FETCHED"
        const val RECEIVE_CONVERSATION_TOPIC = "CLOUD_LITER_CONVERSATION_STATE"


        const val ACTION_INTO_CONVERSATION = "CLOUD_LITER_INTO_CONVERSATION"
        const val ACTION_LEFT_CONVERSATION = "CLOUD_LITER_LEFT_CONVERSATION"
        const val ACTION_ONLINE = "CLOUD_LITER_ONLINE"
        const val ACTION_OFFLINE = "CLOUD_LITER_OFFLINE"
        const val ACTION_MARK_ALL_READ = "CLOUD_LITER_MARK_ALL_READ"
        const val ACTION_MARK_READ = "CLOUD_LITER_MARK_READ"
        private const val HEART_BEAT_RATE = 30 * 1000.toLong() //每隔30秒进行一次对长连接的心跳检测
        private var loggedInUserId: String? = null;
    }
}