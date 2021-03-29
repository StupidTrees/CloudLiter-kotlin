package com.stupidtree.cloudliter.data.source.websource

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.component.data.DataState
import com.stupidtree.cloudliter.ui.chat.FriendStateTrigger
import com.stupidtree.cloudliter.ui.chat.MessageReadNotification
import java.sql.Timestamp

/**
 * 实时聊天网络资源
 * 数据来自socketio连接
 */
class SocketWebSource : BroadcastReceiver() {
    var newMessageState = MutableLiveData<ChatMessage>()
    var onlineStateController = MutableLiveData<FriendStateTrigger>()

    //消息已读通知
    var messageReadState = MutableLiveData<DataState<MessageReadNotification>>()
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) return
        when (intent.action) {
            SocketIOClientService.RECEIVE_RECEIVE_MESSAGE -> if (intent.extras != null) {
                val message = intent.extras!!.getSerializable("message") as ChatMessage?
                if (message != null) {
                    newMessageState.value = message
                     val map = HashMap<String, Int>()
                    message.conversationId.let {
                        map[it] = 1
                    }
                    unreadMessageState.value = DataState(map).setListAction(DataState.LIST_ACTION.APPEND)
                }
            }
            SocketIOClientService.RECEIVE_FRIEND_STATE_CHANGED -> if (intent.hasExtra("conversationId") && intent.hasExtra("online")) {
                onlineStateController.value = FriendStateTrigger.getActioning(
                        intent.getStringExtra("conversationId"), intent.getStringExtra("online"),intent.getIntExtra("num",0)
                )
            }
            SocketIOClientService.RECEIVE_MESSAGE_READ -> if (intent.extras != null) {
                val notification = intent.extras!!.getSerializable("read") as MessageReadNotification?
                if (notification != null) {
                    messageReadState.value = DataState(notification)
                }
            }
            SocketIOClientService.RECEIVE_UNREAD_MESSAGE -> {
                //获得未读消息推送
                val map = intent.extras?.getSerializable("map") as HashMap<*, *>?
                map?.let {
                    val m = HashMap<String,Int>()
                    for(e in map.entries){
                        m[e.key.toString()] = e.value as Int
                    }
                    unreadMessageState.value = (DataState(m).setListAction(DataState.LIST_ACTION.REPLACE_ALL))
                }
            }
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
        val map = HashMap<String, Int>()
        map[conversationId ?: ""] = num
        unreadMessageState.value = DataState(map).setListAction(DataState.LIST_ACTION.DELETE)

    }

    fun markRead(context: Context, userId: String, messageId: String, conversationId: String) {
        val i = Intent(SocketIOClientService.ACTION_MARK_READ)
        i.putExtra("userId", userId)
        i.putExtra("messageId", messageId)
        i.putExtra("conversationId", conversationId)
        context.sendBroadcast(i)
        val map = HashMap<String, Int>()
        map[conversationId] = 1
        unreadMessageState.value = DataState(map).setListAction(DataState.LIST_ACTION.DELETE)
    }

    fun getIntoConversation(context: Context, userId: String?, conversationId: String?) {
        val i = Intent(SocketIOClientService.ACTION_INTO_CONVERSATION)
        i.putExtra("userId", userId)
        i.putExtra("conversationId", conversationId)
        context.sendBroadcast(i)
    }

    fun leftConversation(context: Context, userId: String?, conversationId: String?) {
        val i = Intent(SocketIOClientService.ACTION_LEFT_CONVERSATION)
        i.putExtra("userId", userId)
        i.putExtra("conversationId", conversationId)
        context.sendBroadcast(i)
    }

    companion object {
        var unreadMessageState = MutableLiveData<DataState<HashMap<String, Int>>>()
    }
}