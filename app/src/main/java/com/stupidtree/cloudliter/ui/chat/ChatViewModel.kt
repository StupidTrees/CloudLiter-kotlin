package com.stupidtree.cloudliter.ui.chat

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.repository.ChatRepository
import com.stupidtree.cloudliter.data.repository.ChatRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.ConversationRepository
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.DataState.LIST_ACTION
import com.stupidtree.cloudliter.ui.base.StringTrigger
import com.stupidtree.cloudliter.utils.TextUtils
import java.sql.Timestamp

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 仓库区
     */
    private val chatRepository: ChatRepository = getInstance(application)
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance(application)
    private val conversationRepository = ConversationRepository.getInstance(application)

    /**
     * 数据区
     */
    //数据本体 朋友id
    var friendId: String? = null
        private set

    //数据本体：对话对象
    val conversation = MediatorLiveData<Conversation?>()


    /**
     * 获取聊天列表状态数据
     * 注意：并不是存放完整的聊天列表，而是动作，比如插入、删除等
     */
    val listData: LiveData<DataState<List<ChatMessage>?>> =  Transformations.map(chatRepository.getListDataState()) { input2: DataState<List<ChatMessage>?> ->
        if (input2.data != null && input2.data?.isNotEmpty()==true) {
            when (input2.listAction) {
                LIST_ACTION.APPEND_ONE -> {
                    Log.e("listAppendOne", input2.data.toString())
                    //不是这个窗口的消息
                    if (input2.data!![0].conversationId != conversationId) {
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
    private val friendStateController: LiveData<FriendStateTrigger> = Transformations.map(chatRepository.friendsStateController) { input: FriendStateTrigger ->
        Log.e("apply", input.online.toString())
        if (friendId == null) {
            return@map FriendStateTrigger.still
        }
        if (input.id == friendId) {
            return@map FriendStateTrigger.getActioning(input.id, input.online)
        }
        FriendStateTrigger.still
    }

    //状态数据：朋友在线状态
    var friendStateLiveData: LiveData<DataState<FriendState>> = Transformations.switchMap(friendStateController) { input: FriendStateTrigger ->
        if (input.isActioning) {
            when (input.online) {
                "ONLINE" -> return@switchMap MutableLiveData(DataState(FriendState.online))
                "OFFLINE" -> return@switchMap MutableLiveData(DataState(FriendState.offline))
                "YOU" -> return@switchMap MutableLiveData(DataState(FriendState.withYou))
                "OTHER" -> return@switchMap MutableLiveData(DataState(FriendState.withOther))
            }
        }
        MutableLiveData(DataState<FriendState>(DataState.STATE.NOTHING))
    }


    //状态数据：消息发送结果
    var messageSentState: LiveData<DataState<ChatMessage>> = Transformations.map(chatRepository.messageSentSate) { input: DataState<ChatMessage> ->
        input.data?.let {
            if (it.uuid == bottomUUID) {
                bottomId = it.id
            }
        }
        input
    }

    private val ttsController = MutableLiveData<ChatMessage>()
    val ttsResultLiveData:LiveData<Pair<DataState<String>,ChatMessage>> = Transformations.switchMap(ttsController){message->
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            return@switchMap Transformations.switchMap(chatRepository.startTTS(userLocal.token!!,message)){
                MutableLiveData(Pair(it,message))
            }
        }else{
            return@switchMap MutableLiveData(Pair(DataState(DataState.STATE.NOT_LOGGED_IN),message))
        }

    }

    var messageReadState: LiveData<DataState<MessageReadNotification>> = Transformations.map(chatRepository.messageReadState) { input: DataState<MessageReadNotification> -> input }
    //控制↑的刷新
    private val imageSendController = MutableLiveData<StringTrigger>()
    private val voiceSendController = MutableLiveData<VoiceMessageTrigger>()
    private val pageSize = 15
    private var topId: String? = null
    private var topTime: Timestamp? = null
    private var bottomId: String? = null
    private var bottomUUID: String? = null



    //状态数据：图片消息发送
    fun getImageSentResult(): LiveData<DataState<ChatMessage?>> {
        return Transformations.switchMap(imageSendController) { input: StringTrigger ->
            if (input.isActioning) {
                val userLocal = localUserRepository.getLoggedInUser()
                if (userLocal.isValid && friendId != null) {
                    return@switchMap chatRepository.ActionSendImageMessage(getApplication(), userLocal.token!!, userLocal.id!!, friendId!!, input.data)
                } else {
                    return@switchMap MutableLiveData(DataState<ChatMessage?>(DataState.STATE.NOT_LOGGED_IN))
                }
            }
            MutableLiveData(DataState(DataState.STATE.NOTHING))
        }
    }


    //状态数据：语音消息发送
    fun getVoiceSentResult(): LiveData<DataState<ChatMessage?>> {
        return Transformations.switchMap(voiceSendController) { input: VoiceMessageTrigger ->
            if (input.isActioning) {
                val userLocal = localUserRepository.getLoggedInUser()
                if (userLocal.isValid && friendId != null) {
                    return@switchMap chatRepository.actionSendVoiceMessage(userLocal.token!!, userLocal.id!!, friendId!!, input.path!!, input.seconds)
                } else {
                    return@switchMap MutableLiveData(DataState<ChatMessage?>(DataState.STATE.NOT_LOGGED_IN))
                }
            }
            MutableLiveData(DataState(DataState.STATE.NOTHING))
        }
    }

    /**
     * 发送消息
     *
     * @param content 消息文本
     */
    fun sendMessage(content: String?) {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid && friendId != null) {
            val message = ChatMessage(userLocal.id,
                    friendId, content)
            //通知仓库发送消息
            chatRepository.ActionSendMessage(message)
        }
    }

    /**
     * 发送图片
     */
    fun sendImageMessage(path: String) {
        imageSendController.value = StringTrigger.getActioning(path)
    }

    /**
     * 发送语音
     */
    fun sendVoiceMessage(path: String, time: Int) {
        voiceSendController.value = VoiceMessageTrigger.getActioning(path, time)
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
            conversationId?.let {
                chatRepository.ActionFetchMessages(
                        userLocal.token!!,
                        it,
                        null,
                        topTime,
                        pageSize,
                        LIST_ACTION.REPLACE_ALL
                )
            }
        }
    }

    /**
     * 手动拉取新消息
     */
    fun fetchNewData() {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            conversationId?.let {
                chatRepository.ActionFetchNewMessages(
                        userLocal.token!!,
                        it,
                        bottomId
                )
            }
        }
    }

    /**
     * 控制获取完整的消息记录列表
     */
    fun loadMore() {
        val userLocal = localUserRepository.getLoggedInUser()
        if (userLocal.isValid) {
            conversationId?.let {
                chatRepository.ActionFetchMessages(
                        userLocal.token!!,
                        it,
                        topId,
                        topTime,
                        pageSize,
                        LIST_ACTION.PUSH_HEAD
                )
            }
        }
    }

    val myAvatar: String?
        get() {
            val userLocal = localUserRepository.getLoggedInUser()
            return userLocal.avatar
        }

    val myId: String?
        get() {
            val userLocal = localUserRepository.getLoggedInUser()
            return userLocal.id
        }

    val friendAvatar: String?
        get() = if (conversation.value != null) {
            conversation.value!!.friendAvatar
        } else null

    val conversationId: String?
        get() = if (conversation.value == null) {
            null
        } else conversation.value!!.id

    /**
     * 声明进入了对话
     */
    fun getIntoConversation(context: Context) {
        if (friendId != null && conversationId != null && localUserRepository.isUserLoggedIn) {
            chatRepository.ActionGetIntoConversation(context, localUserRepository.getLoggedInUser().id!!,
                    friendId!!, conversationId!!)
        }
    }

    /**
     * 声明离开对话
     */
    fun leftConversation(context: Context) {
        if (conversationId != null && localUserRepository.isUserLoggedIn) {
            chatRepository.ActionLeftConversation(context, localUserRepository.getLoggedInUser().id!!,
                    conversationId!!)
        }
    }

    fun markAllRead(context: Context) {
        if (conversationId != null && localUserRepository.isUserLoggedIn) {
            if (topTime != null && conversationId != null && myId != null) {
                chatRepository.ActionMarkAllRead(context, myId!!, conversationId!!,
                        topTime!!, pageSize)
            }

        }
    }

    fun markRead(context: Context, chatMessage: ChatMessage) {
        chatMessage.toId?.let {
            chatMessage.conversationId?.let { it1 ->
                chatRepository.ActionMarkRead(context, it, chatMessage.id, it1)
            }
        }
    }

    fun setConversation(conversation: Conversation) {
        this.conversation.value = conversation
        friendId = conversation.friendId
    }

    private var webConversationData: LiveData<DataState<Conversation?>>? = null

    fun refreshConversation() {
        friendId?.let { id ->
            val userLocal = localUserRepository.getLoggedInUser()
            webConversationData?.let {
                conversation.removeSource(it)
            }
            webConversationData = conversationRepository?.queryConversation(userLocal.token!!, userLocal.id!!, id)
            webConversationData?.let {
                conversation.addSource(it) { data ->
                    if (data.state == DataState.STATE.SUCCESS) {
                        conversation.value = data.data
                    }
                }
            }
        }
    }

    /**
     * 开始语音识别
     */
    fun startTTS(message:ChatMessage) {
        ttsController.value = message
    }
    
}