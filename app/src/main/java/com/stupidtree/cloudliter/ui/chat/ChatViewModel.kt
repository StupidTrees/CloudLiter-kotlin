package com.stupidtree.cloudliter.ui.chat

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.repository.AiRepository
import com.stupidtree.cloudliter.data.repository.ChatRepository
import com.stupidtree.cloudliter.data.repository.ChatRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.DataState.LIST_ACTION
import com.stupidtree.cloudliter.utils.TextUtils
import java.sql.Timestamp

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */
    private val chatRepository: ChatRepository = getInstance(application)
    private val aiRepository: AiRepository = AiRepository(application)
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)
    private val conversationRepository = ConversationRepository.getInstance(application)

    /**
     * 数据区
     */

    private val conversationIdLiveData = MutableLiveData<String>()

    //数据本体：对话对象
    val conversation = Transformations.switchMap(conversationIdLiveData) {
        val localUser = localUserRepository.getLoggedInUser()
        if (localUser.isValid) {
            return@switchMap conversationRepository.queryConversation(localUser.token!!, it)
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }


    /**
     * 获取聊天列表状态数据
     * 注意：并不是存放完整的聊天列表，而是动作，比如插入、删除等
     */
    val listData: LiveData<DataState<List<ChatMessage>?>> = Transformations.map(chatRepository.getListDataState()) { input2: DataState<List<ChatMessage>?> ->
        if (input2.data != null && input2.data?.isNotEmpty() == true) {
            when (input2.listAction) {
                LIST_ACTION.APPEND_ONE -> {
                    Log.e("listAppendOne", input2.data.toString())
                    //不是这个窗口的消息
                    if (input2.data!![0].conversationId != getConversationId()) {
                        return@map DataState<List<ChatMessage>?>(DataState.STATE.NOTHING)
                    }
                    if (!TextUtils.isEmpty(input2.data!![0].id)) {
                        bottomId = input2.data!![0].id
                    }
                    bottomUUID = input2.data!![0].uuid
                }
                LIST_ACTION.APPEND -> {
                    //拉取一堆新消息
                    Log.e("listAppend", input2.data.toString())
                    bottomId = input2.data!![0].id
                    bottomUUID = input2.data!![0].uuid
                    markAllRead(application)
                }
                LIST_ACTION.REPLACE_ALL -> {
                    //初次进入
                    topId = input2.data!![input2.data!!.size - 1].id
                    topTime = input2.data!![input2.data!!.size - 1].createdAt
                    bottomId = input2.data!![0].id
                    bottomUUID = input2.data!![0].uuid
                    markAllRead(application)
                }
                LIST_ACTION.PUSH_HEAD -> {
                    //下拉加载
                    topId = input2.data!![input2.data!!.size - 1].id
                    topTime = input2.data!![input2.data!!.size - 1].createdAt
                    markAllRead(application)
                }
            }
        }
        input2
    }

    //trigger：控制↓的刷新
    private val friendStateController: LiveData<FriendStateTrigger> = Transformations.map(chatRepository.onlineStateController) { input: FriendStateTrigger ->
        if (input.conversationId == getConversationId()) {
            return@map FriendStateTrigger.getActioning(input.conversationId, input.online, input.num)
        }
        FriendStateTrigger.still
    }

    //状态数据：朋友在线状态
    var onlineStateLiveData: LiveData<DataState<OnlineState>> = Transformations.switchMap(friendStateController) { input: FriendStateTrigger ->
        if (input.isActioning) {
            when (input.online) {
                "ONLINE" -> return@switchMap MutableLiveData(DataState(OnlineState.online))
                "OFFLINE" -> return@switchMap MutableLiveData(DataState(OnlineState.offline))
                "YOU" -> return@switchMap MutableLiveData(DataState(OnlineState.withYou))
                "OTHER" -> return@switchMap MutableLiveData(DataState(OnlineState.withOther))
            }
        }
        MutableLiveData(DataState<OnlineState>(DataState.STATE.NOTHING))
    }


    //状态数据：消息发送结果
    //first为成功后的message，second为uuid
    private var messageSentLiveData = MediatorLiveData<Pair<DataState<ChatMessage?>, String>>()
    var messageSentState: LiveData<Pair<DataState<ChatMessage?>, String>> = Transformations.map(messageSentLiveData) { input ->
        input.first.data?.let {
            if (input.second == bottomUUID) {
                bottomId = it.id
            }
        }
        input
    }

    val ttsResultLiveData: MediatorLiveData<Pair<DataState<ChatMessage?>, String>> = MediatorLiveData()
    var messageReadState: LiveData<DataState<MessageReadNotification>> = Transformations.map(chatRepository.messageReadState) { input: DataState<MessageReadNotification>
        ->
        if (input.data?.conversationId == getConversationId()) {
            input
        } else {
            DataState(DataState.STATE.NOTHING)
        }
    }

    var accessibilityInfo = Transformations.switchMap(conversation) {
        val localUser = localUserRepository.getLoggedInUser()
        if (localUser.isValid) {
            it.data?.let { conversation ->
                return@switchMap conversationRepository.getAccessibilityInfo(localUser.token!!, conversation.id, conversation.type)
            }
            return@switchMap MutableLiveData(DataState(DataState.STATE.FETCH_FAILED))
        } else {
            return@switchMap MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }
    private val pageSize = 15
    private var topId: String? = null
    private var topTime: Timestamp? = null
    private var bottomId: String? = null
    private var bottomUUID: String? = null


    /**
     * 发送消息
     *
     * @param content 消息文本
     */
    fun sendMessage(content: String?) {
        val userLocal = localUserRepository.getLoggedInUser()
        val liveData = if (userLocal.isValid) {
            chatRepository.sendTextMessage(userLocal.token!!, userLocal.id!!, getConversationId(), content
                    ?: "")
        } else {
            MutableLiveData(Pair(DataState(DataState.STATE.NOT_LOGGED_IN), ""))
        }
        messageSentLiveData.addSource(liveData) {
            if (it.first.state == DataState.STATE.SUCCESS) {
                it.first.data?.let { it1 -> chatRepository.saveMessageAsync(it1) }
            }
            messageSentLiveData.value = it
            messageSentLiveData.removeSource(liveData)
        }
    }

    /**
     * 发送图片
     */
    fun sendImageMessage(path: String) {
        val userLocal = localUserRepository.getLoggedInUser()
        val liveData = if (userLocal.isValid) {
            chatRepository.sendImageMessage(getApplication(), userLocal.token!!, userLocal.id!!, getConversationId(), path)
        } else {
            MutableLiveData(Pair(DataState(DataState.STATE.NOT_LOGGED_IN), ""))
        }
        messageSentLiveData.addSource(liveData) {
            if (it.first.state == DataState.STATE.SUCCESS) {
                it.first.data?.let { it1 -> chatRepository.saveMessageAsync(it1) }
            }
            messageSentLiveData.value = it
            messageSentLiveData.removeSource(liveData)
        }

    }


    /**
     * 发送语音
     */
    fun sendVoiceMessage(path: String, time: Int) {
        val userLocal = localUserRepository.getLoggedInUser()
        val liveData = if (userLocal.isValid) {
            chatRepository.actionSendVoiceMessage(userLocal.token!!, userLocal.id!!, getConversationId(), path, time)
        } else {
            MutableLiveData(Pair(DataState(DataState.STATE.NOT_LOGGED_IN), ""))
        }
        messageSentLiveData.addSource(liveData) {
            if (it.first.state == DataState.STATE.SUCCESS) {
                it.first.data?.let { it1 -> chatRepository.saveMessageAsync(it1) }
            }
            messageSentLiveData.value = it
            messageSentLiveData.removeSource(liveData)
        }
    }

    fun voiceTTSDirect(path: String) {
        val userLocal = localUserRepository.getLoggedInUser()
        val liveData = if (userLocal.isValid) {
            aiRepository.voiceTTSDirect(userLocal.token!!, path)
        } else {
            MutableLiveData(DataState(DataState.STATE.FETCH_FAILED))
        }
    }

    fun bindService(context: Context?) {
        chatRepository.bindService(context!!)
    }

    fun unbindService(context: Context?) {
        chatRepository.unbindService(context!!)
    }

    /**
     * 第一次进入获取聊天记录
     */
    fun fetchHistoryData() {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            chatRepository.actionFetchMessages(
                    userLocal.token!!,
                    getConversationId(),
                    null,
                    topTime,
                    pageSize,
                    LIST_ACTION.REPLACE_ALL
            )
        }
    }

    /**
     * 手动拉取新消息
     */
    fun fetchNewData() {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            chatRepository.actionFetchNewMessages(
                    userLocal.token!!,
                    getConversationId(),
                    bottomId
            )
        }
    }

    /**
     * 控制获取完整的消息记录列表
     */
    fun loadMore() {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            chatRepository.actionFetchMessages(
                    userLocal.token!!,
                    getConversationId(),
                    topId,
                    topTime,
                    pageSize,
                    LIST_ACTION.PUSH_HEAD
            )
        }
    }

    val myId: String?
        get() {
            val userLocal = localUserRepository.getLoggedInUser()
            return userLocal.id
        }

    fun getConversationType(): Conversation.TYPE {
        return conversation.value?.data?.type ?: Conversation.TYPE.FRIEND
    }

    fun getConversationId(): String {
        return conversationIdLiveData.value ?: ""
    }

    /**
     * 声明进入了对话
     */
    fun getIntoConversation(context: Context) {
        if (localUserRepository.isUserLoggedIn) {
            chatRepository.actionGetIntoConversation(context, localUserRepository.getLoggedInUser().id!!, getConversationId())
        }
    }

    /**
     * 声明离开对话
     */
    fun leftConversation(context: Context) {
        if (localUserRepository.isUserLoggedIn) {
            chatRepository.actionLeftConversation(context, localUserRepository.getLoggedInUser().id!!,
                    getConversationId())
        }
    }

    fun markAllRead(context: Context) {
        if (localUserRepository.isUserLoggedIn) {
            if (topTime != null && myId != null) {
                chatRepository.actionMarkAllRead(context, getConversationType(), myId!!, getConversationId(),
                        topTime!!, pageSize)
            }

        }
    }

    fun markRead(context: Context, chatMessage: ChatMessage) {
        localUserRepository.getLoggedInUser().id?.let {
            chatMessage.conversationId?.let { it1 ->
                chatRepository.actionMarkRead(context, getConversationType(), it, chatMessage.id, it1)
            }
        }
    }

    fun startRefreshConversationInfo(conversationId: String) {
        conversationIdLiveData.value = conversationId
    }


    /**
     * 开始语音识别
     */
    fun startTTS(message: ChatMessage) {
        val dt = getTTSOneTaskLiveData(message)
        ttsResultLiveData.addSource(dt) {
            it.data?.id = message.id
            ttsResultLiveData.value = Pair(it, message.id)
            ttsResultLiveData.removeSource(dt) //移除任务
        }
    }

    /**
     * 获取某个语音合成任务的liveData
     */
    private fun getTTSOneTaskLiveData(message: ChatMessage): LiveData<DataState<ChatMessage?>> {
        val userLocal = localUserRepository.getLoggedInUser()
        return if (userLocal.isValid) {
            chatRepository.startTTS(userLocal.token!!, message)
        } else {
            MutableLiveData(DataState(DataState.STATE.NOT_LOGGED_IN))
        }
    }

}