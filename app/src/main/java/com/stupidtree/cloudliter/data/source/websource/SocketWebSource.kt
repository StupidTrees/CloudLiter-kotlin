package com.stupidtree.cloudliter.data.source.websource

import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.cloudliter.service.socket.SocketIOClientService.JWebSocketClientBinder
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.chat.FriendStateTrigger
import com.stupidtree.cloudliter.ui.chat.MessageReadNotification
import java.sql.Timestamp
import java.util.*

/**
 * 实时聊天网络资源
 * 数据来自socketio连接
 */
class SocketWebSource : BroadcastReceiver() {
    var newMessageState = MutableLiveData<ChatMessage>()
    var friendStateController = MutableLiveData<FriendStateTrigger>()
    var unreadMessageState = MutableLiveData<DataState<HashMap<String, Int>>>()

    //消息发送结果
    var messageSentSate = MutableLiveData<DataState<ChatMessage>>()

    //消息已读通知
    var messageReadState = MutableLiveData<DataState<MessageReadNotification>>()
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) return
        when (intent.action) {
            SocketIOClientService.ACTION_RECEIVE_MESSAGE -> if (intent.extras != null) {
                val message = intent.extras!!.getSerializable("message") as ChatMessage?
                Log.e("unreadMessaged.add", message.toString())
                if (message != null) {
                    newMessageState.value = message
                    //                        unreadMessages.setValue(new DataState<>(Collections.singletonList(message)).setListAction(DataState.LIST_ACTION.APPEND));
                    val map = HashMap<String, Int>()
                    message.conversationId?.let {
                        map[it] = 1
                    }
                    unreadMessageState.value = DataState(map).setListAction(DataState.LIST_ACTION.APPEND)
                }
            }
            SocketIOClientService.ACTION_FRIEND_STATE_CHANGED -> if (intent.hasExtra("id") && intent.hasExtra("online")) {
                friendStateController.value = FriendStateTrigger.getActioning(
                        intent.getStringExtra("id"), intent.getStringExtra("online")
                )
            }
            SocketIOClientService.ACTION_MESSAGE_SENT -> if (intent.extras != null) {
                val message = intent.extras!!.getSerializable("message") as ChatMessage?
                Log.e("SocketWebSource-消息已发送", message.toString())
                if (message != null) {
                    messageSentSate.value = DataState(message)
                }
            }
            SocketIOClientService.ACTION_MESSAGE_READ -> if (intent.extras != null) {
                val notification = intent.extras!!.getSerializable("read") as MessageReadNotification?
                Log.e("SocketWebSource-消息被读", notification.toString())
                if (notification != null) {
                    messageReadState.value = DataState(notification)
                }
            }
        }
    }

    /**
     * 和后台服务通信
     */
    private var binder: JWebSocketClientBinder? = null
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            //服务与活动成功绑定
            Log.e("ChatActivity", "服务与活动成功绑定")
            binder = iBinder as JWebSocketClientBinder
            binder!!.onUnreadFetchedListener = object : SocketIOClientService.OnUnreadFetchedListener {
                override fun onUnreadFetched(unread: HashMap<String, Int>) {
                    Log.e("获取未读消息", unread.toString())
                    unreadMessageState.postValue(DataState(unread).setListAction(DataState.LIST_ACTION.REPLACE_ALL))

                }
            }
            binder!!.onMessageReadListener = object :SocketIOClientService.OnMessageReadListener{
                override fun onMessageRead(map: HashMap<String, Int>) {
                    Log.e("已读更新", map.toString())
                    unreadMessageState.postValue(DataState(map).setListAction(DataState.LIST_ACTION.DELETE))

                }
            }

        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            //服务与活动断开
            Log.e("ChatActivity", "服务与活动成功断开")
        }
    }

    /**
     * 绑定服务
     *
     * @param from activity
     */
    fun bindService(action: String?, from: Context) {
        val bindIntent = Intent(from, SocketIOClientService::class.java)
        bindIntent.action = action
        from.bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * 断开服务
     */
    fun unbindService(from: Context) {
        from.unbindService(serviceConnection)
    }

    fun sendMessage(message: ChatMessage) {
        if (binder != null) {
            binder!!.sendMessage(message)
        }
    }

    fun callOnline(context: Context, user: UserLocal) {
        val i = Intent(SocketIOClientService.ACTION_ONLINE)
        i.putExtra("userId", user.id)
        context.sendBroadcast(i)
    }

    fun markAllRead(context: Context, userId: String?, conversationId: String?, topTime: Timestamp, num: Int) {
        val i = Intent(SocketIOClientService.ACTION_MARK_ALL_READ)
        i.putExtra("userId", userId)
        i.putExtra("topTime", topTime.time)
        i.putExtra("num", num)
        i.putExtra("conversationId", conversationId)
        context.sendBroadcast(i)
    }

    fun markRead(context: Context, userId: String, messageId: String, conversationId: String) {
        val i = Intent(SocketIOClientService.ACTION_MARK_READ)
        i.putExtra("userId", userId)
        i.putExtra("messageId", messageId)
        i.putExtra("conversationId", conversationId)
        context.sendBroadcast(i)
    }

    fun getIntoConversation(context: Context, userId: String?, friendId: String?, conversationId: String?) {
        val i = Intent(SocketIOClientService.ACTION_INTO_CONVERSATION)
        i.putExtra("userId", userId)
        i.putExtra("friendId", friendId)
        i.putExtra("conversationId", conversationId)
        context.sendBroadcast(i)
    }

    fun leftConversation(context: Context, userId: String?, conversationId: String?) {
        val i = Intent(SocketIOClientService.ACTION_LEFT_CONVERSATION)
        i.putExtra("userId", userId)
        i.putExtra("conversationId", conversationId)
        context.sendBroadcast(i)
    }

}