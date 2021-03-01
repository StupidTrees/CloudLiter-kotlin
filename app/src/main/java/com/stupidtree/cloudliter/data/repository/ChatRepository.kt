package com.stupidtree.cloudliter.data.repository

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.source.dao.ChatMessageDao
import com.stupidtree.cloudliter.data.source.websource.ChatMessageWebSource
import com.stupidtree.cloudliter.data.source.websource.SocketWebSource
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.DataState.LIST_ACTION
import com.stupidtree.cloudliter.ui.chat.FriendStateTrigger
import com.stupidtree.cloudliter.ui.chat.MessageReadNotification
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.sql.Timestamp

/**
 * 层次：Repository
 * 消息记录仓库
 */
class ChatRepository(context: Context) {
    //数据源1：网络类型数据，消息记录的网络数据源
    var chatMessageWebSource: ChatMessageWebSource = ChatMessageWebSource.instance!!

    //数据源2：和后台服务通信的Service
    private var socketWebSource: SocketWebSource = SocketWebSource()

    //数据源3：本地数据库的数据源
    private var chatMessageDao: ChatMessageDao = AppDatabase.getDatabase(context).chatMessageDao()

    // 动态数据对象：列表状态
    private var listDataState = MediatorLiveData<DataState<List<ChatMessage>?>>()
    private var localListState: LiveData<List<ChatMessage>?>? = null //本地获取数据
    private var webListState: LiveData<DataState<List<ChatMessage>?>>? = null //网络获取数据
    fun getListDataState(): MediatorLiveData<DataState<List<ChatMessage>?>> {
        listDataState.addSource(socketWebSource.newMessageState) { message: ChatMessage? ->
            if (message != null) {
                listDataState.removeSource(localListState!!)
                listDataState.value = DataState(listOf(message) as List?).setListAction(LIST_ACTION.APPEND_ONE)
                saveMessageAsync(message)
            }
        }
        listDataState.addSource(messageSentSate) { chatMessageDataState: DataState<ChatMessage> ->
            listDataState.removeSource(localListState!!)
            val sentMessage = chatMessageDataState.data
            sentMessage?.let { saveMessageAsync(it) }
        }
        listDataState.addSource(messageReadState) { messageReadNotificationDataState: DataState<MessageReadNotification> ->
            listDataState.removeSource(localListState!!)
            markMessageReadAsync(messageReadNotificationDataState.data)
        }
        return listDataState
    }

    /**
     * 动作：拉取聊天记录
     *
     * @param token          令牌
     * @param conversationId 对话id
     * @param topId          现有列表顶部的消息id
     * @param pageSize       分页大小
     * @param action         操作：全部刷新或在头部插入
     */
    fun ActionFetchMessages(token: String, conversationId: String, topId: String?, topTime: Timestamp?, pageSize: Int, action: LIST_ACTION) {
        localListState?.let { listDataState.removeSource(it) }
        Log.e("fromId", topId.toString())
        localListState = if (topId == null) {
            chatMessageDao.getMessages(conversationId, pageSize)
        } else {
            chatMessageDao.getMessages(conversationId, pageSize, topTime)
        }

        listDataState.addSource(localListState!!) { chatMessages ->
            listDataState.value = DataState(chatMessages).setListAction(action)
            if (action === LIST_ACTION.REPLACE_ALL && topId == null && chatMessages != null && chatMessages.isNotEmpty()) { //第一次获取，且本地已有消息
                //那么从本地的最早消息开始，把消息全部拉取更新
                webListState?.let { listDataState.removeSource(it) }
                webListState = chatMessageWebSource.getMessagesAfter(token, conversationId, chatMessages[chatMessages.size - 1].id, true)
                listDataState.addSource(webListState!!) { result ->
                    if (result.state === DataState.STATE.SUCCESS && result.data!!.isNotEmpty()) {
                        listDataState.removeSource(localListState!!)
                        saveMessageAsync(ArrayList(result.data))
                        listDataState.value = result.setRetry(true).setListAction(action)
                    }
                }
            } else { //本地无消息/上拉加载
                webListState?.let { listDataState.removeSource(it) }
                webListState = chatMessageWebSource.getMessages(token, conversationId, topId, pageSize)
                listDataState.addSource(webListState!!) { result ->
                    if (result.state === DataState.STATE.SUCCESS && result.data!!.isNotEmpty()) {
                        listDataState.removeSource(localListState!!)
                        saveMessageAsync(ArrayList(result.data))
                        if (chatMessages != null) {
                            listDataState.value = result.setListAction(action).setRetry(chatMessages.isNotEmpty())
                        }
                    }
                }
            }
        }
    }


    /**
     * 动作：获取新消息
     *
     * @param token          令牌
     * @param conversationId 对话id
     * @param afterId        现有列表底部的消息id
     */
    fun ActionFetchNewMessages(token: String, conversationId: String, afterId: String?) {
        webListState?.let{listDataState.removeSource(it)}
        webListState = chatMessageWebSource.getMessagesAfter(token, conversationId, afterId, false)
        listDataState.addSource(webListState!!) { result ->
            Log.e("手动拉取本地未存新消息", "$afterId-$result")
            if (result.state === DataState.STATE.SUCCESS && result.data!!.isNotEmpty()) {
                listDataState.removeSource(localListState!!)
                saveMessageAsync(ArrayList(result.data!!))
                listDataState.value = DataState(result.data).setListAction(LIST_ACTION.APPEND)
            }
        }
    }

    /**
     * 动作：发消息
     *
     * @param message 消息
     */
    fun ActionSendMessage(message: ChatMessage) {
        socketWebSource.sendMessage(message)
        listDataState.value = DataState(listOf(message) as List?).setListAction(LIST_ACTION.APPEND_ONE)
    }

    /**
     * 动作：标记对话全部已读
     *
     * @param context        上下文，为了发广播
     * @param userId         用户id
     * @param conversationId 对话id
     */
    fun ActionMarkAllRead(context: Context, userId: String, conversationId: String, topTime: Timestamp, num: Int) {
        socketWebSource.markAllRead(context, userId, conversationId, topTime, num)
    }

    /**
     * 动作：标记某消息已读
     *
     * @param context        上下文
     * @param messageId      消息id
     * @param conversationId 对话id
     */
    fun ActionMarkRead(context: Context, userId: String, messageId: String, conversationId: String) {
        socketWebSource.markRead(context, userId, messageId, conversationId)
    }

    /**
     * 动作：进入对话
     *
     * @param context        上下文
     * @param userId         用户id
     * @param friendId       朋友id
     * @param conversationId 对话id
     */
    fun ActionGetIntoConversation(context: Context, userId: String, friendId: String, conversationId: String) {
        socketWebSource.getIntoConversation(context, userId, friendId, conversationId)
    }

    /**
     * 动作：退出对话
     *
     * @param context        上下文
     * @param userId         用户id
     * @param conversationId 对话id
     */
    fun ActionLeftConversation(context: Context, userId: String, conversationId: String) {
        socketWebSource.leftConversation(context, userId, conversationId)
    }

    /**
     * 发送图片
     *
     * @param token    令牌
     * @param toId     朋友id
     * @param filePath 文件
     * @return 返回结果
     */
    fun ActionSendImageMessage(context: Context, token: String, fromId: String, toId: String, filePath: String): LiveData<DataState<ChatMessage?>> {
        val result = MediatorLiveData<DataState<ChatMessage?>>()
        //读取图片文件
        val f = File(filePath)
        val tempMsg = ChatMessage(fromId, toId, filePath)
        tempMsg.setType(ChatMessage.TYPE.IMG)
        listDataState.value = DataState(listOf(tempMsg) as List?).setListAction(LIST_ACTION.APPEND_ONE)
        Luban.with(context)
                .setTargetDir(context.getExternalFilesDir("image")!!.absolutePath)
                .load(f)
                .setCompressListener(object : OnCompressListener {
                    //设置回调
                    override fun onStart() {
                        Log.e("luban", "开始压缩")
                    }

                    override fun onSuccess(file: File) {
                        Log.e("luban", "压缩成功")
                        // MutableLiveData<DataState<String>> result = new MutableLiveData<>();
                        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                        //构造一个图片格式的POST表单
                        val body = MultipartBody.Part.createFormData("upload", file.name, requestFile)
                        val sendResult = chatMessageWebSource.sendImageMessage(token, toId, body, tempMsg.uuid)
                        result.addSource(sendResult) { value ->
                            result.setValue(value)
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e("luban", "压缩失败")
                    }
                }).launch() //启动压缩
        return result
    }


    /**
     * 发送图片
     *
     * @param token    令牌
     * @param toId     朋友id
     * @param filePath 文件
     * @return 返回结果
     */
    fun actionSendVoiceMessage(token: String, fromId: String, toId: String, filePath: String, voiceSeconds: Int): LiveData<DataState<ChatMessage?>> {
        val result = MediatorLiveData<DataState<ChatMessage?>>()
        //读取图片文件
        val f = File(filePath)
        val tempMsg = ChatMessage(fromId, toId, filePath)
        tempMsg.setType(ChatMessage.TYPE.VOICE)
        tempMsg.extra = voiceSeconds.toString()
        listDataState.value = DataState(listOf(tempMsg) as List?).setListAction(LIST_ACTION.APPEND_ONE)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f)
        //构造一个图片格式的POST表单
        val body = MultipartBody.Part.createFormData("upload", f.name, requestFile)
        val sendResult = chatMessageWebSource.sendVoiceMessage(token, toId, body, tempMsg.uuid, voiceSeconds)
        result.addSource(sendResult) { value ->
            result.setValue(value)
        }
        return result
    }


    /**
     * 开始语音合成
     */
    fun startTTS(token:String,message:ChatMessage):LiveData<DataState<String>>{
        return chatMessageWebSource.startTTS(token,message.id)
    }

    fun bindService(context: Context) {
        val IF = IntentFilter()
        IF.addAction(SocketIOClientService.ACTION_RECEIVE_MESSAGE)
        IF.addAction(SocketIOClientService.ACTION_MESSAGE_SENT)
        IF.addAction(SocketIOClientService.ACTION_FRIEND_STATE_CHANGED)
        IF.addAction(SocketIOClientService.ACTION_MESSAGE_READ)
        context.registerReceiver(socketWebSource, IF)
        socketWebSource.bindService("Chat", context)
    }

    fun unbindService(context: Context) {
        context.unregisterReceiver(socketWebSource)
        socketWebSource.unbindService(context)
    }

    private fun saveMessageAsync(chatMessage: ChatMessage) {
        Thread { chatMessageDao.saveMessage(listOf(chatMessage)) }.start()
    }

    private fun markMessageReadAsync(notification: MessageReadNotification?) {
        if (notification!!.type == MessageReadNotification.TYPE.ALL) {
            Thread { chatMessageDao.messageAllRead(notification.conversationId, notification.fromTime) }.start()
        } else {
            Thread { notification.id?.let { chatMessageDao.messageRead(it) } }.start()
        }
        //new Thread(() -> chatMessageDao.saveMessage(Collections.singletonList(chatMessage))).start();
    }

    private fun saveMessageAsync(chatMessages: List<ChatMessage>?) {
        Thread { chatMessages?.let { chatMessageDao.saveMessage(it) } }.start()
    }

    val messageSentSate: MutableLiveData<DataState<ChatMessage>>
        get() = socketWebSource.messageSentSate

    val friendsStateController: MutableLiveData<FriendStateTrigger>
        get() = socketWebSource.friendStateController

    val messageReadState: MutableLiveData<DataState<MessageReadNotification>>
        get() = socketWebSource.messageReadState


    companion object {
        //不采用单例模式
        @JvmStatic
        fun getInstance(context: Context): ChatRepository {
            return ChatRepository(context.applicationContext)
        }
    }

}