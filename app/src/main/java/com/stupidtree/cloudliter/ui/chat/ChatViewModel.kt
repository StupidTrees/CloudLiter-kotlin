package com.stupidtree.cloudliter.ui.chat

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stupidtree.cloudliter.data.model.ChatMessage
import com.stupidtree.cloudliter.data.model.Conversation
import com.stupidtree.cloudliter.data.repository.ChatRepository
import com.stupidtree.cloudliter.data.repository.ChatRepository.Companion.getInstance
import com.stupidtree.cloudliter.data.repository.LocalUserRepository
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.base.DataState.LIST_ACTION
import com.stupidtree.cloudliter.ui.base.StringTrigger
import java.sql.Timestamp
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 数据区
     */
    //数据本体 朋友id
    var friendId: String? = null
        private set

    //数据本体：对话对象
    val conversation = MutableLiveData<Conversation?>()

    //数据本体：消息列表
    private var listData: LiveData<DataState<List<ChatMessage>?>>? = null

    //状态数据：朋友在线状态
    var friendStateLiveData: LiveData<DataState<FriendState>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(friendStateController) { input: FriendStateTrigger ->
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
            }
            return field
        }
        private set

    //trigger：控制↑的刷新
    private val friendStateController: LiveData<FriendStateTrigger>

    //状态数据：消息发送结果
    var messageSentState: LiveData<DataState<ChatMessage>>? = null
        get() {
            if (field == null) {
                field = Transformations.map(chatRepository.messageSentSate) { input: DataState<ChatMessage> -> input }
            }
            return field
        }
        private set
    var messageReadState: LiveData<DataState<MessageReadNotification>>? = null
        get() {
            if (field == null) {
                field = Transformations.map(chatRepository.messageReadState) { input: DataState<MessageReadNotification> -> input }
            }
            return field
        }
        private set


    //控制↑的刷新
    private val imageSendController = MutableLiveData<StringTrigger>()
    private val pageSize = 15
    private var topId: String? = null
    private var topTime: Timestamp? = null
    private var bottomId: String? = null

    /**
     * 仓库区
     */
    private val chatRepository: ChatRepository = getInstance(application)
    private val localUserRepository: LocalUserRepository = LocalUserRepository.getInstance()
    fun setConversation(conversation: Conversation) {
        this.conversation.value = conversation
        friendId = conversation.friendId
    }

    /**
     * 获取聊天列表状态数据
     * 注意：并不是存放完整的聊天列表，而是动作，比如插入、删除等
     *
     * @return 状态数据
     */
    fun getListData(context: Context): LiveData<DataState<List<ChatMessage>?>> {
        if (listData == null) {
            listData = Transformations.map<DataState<List<ChatMessage>?>, DataState<List<ChatMessage>?>>(chatRepository.getListDataState()) { input2: DataState<List<ChatMessage>?> ->
                if (input2.data != null && input2.data!!.size > 0) {
                    when (input2.listAction) {
                        LIST_ACTION.APPEND_ONE -> {
                            Log.e("listAppendOne", input2.data.toString())
                            //不是这个窗口的消息
                            if (input2.data!![0].getConversationId() != conversationId) {
                                return@map DataState<List<ChatMessage>?>(DataState.STATE.NOTHING)
                            }
                            bottomId = input2.data!![0].getId()
                        }
                        LIST_ACTION.APPEND -> {
                            //拉取一堆新消息
                            Log.e("listAppend", input2.data.toString())
                            bottomId = input2.data!![0].getId()
                            markAllRead(context.applicationContext, input2.data!![input2.data!!.size - 1].getId())
                        }
                        LIST_ACTION.REPLACE_ALL -> {
                            //初次进入
                            topId = input2.data!![input2.data!!.size - 1].getId()
                            topTime = input2.data!![input2.data!!.size - 1].createdTime
                            bottomId = input2.data!![0].getId()
                            markAllRead(context.applicationContext, topId!!)
                        }
                        LIST_ACTION.PUSH_HEAD -> {
                            //下拉加载
                            topId = input2.data!![input2.data!!.size - 1].getId()
                            topTime = input2.data!![input2.data!!.size - 1].createdTime
                            markAllRead(context.applicationContext, topId!!)
                        }
                    }
                }
                input2
            }
        }
        return listData!!
    }


    //状态数据：图片消息发送
    fun getImageSentResult(): LiveData<DataState<ChatMessage?>> {
        return Transformations.switchMap<StringTrigger, DataState<ChatMessage?>>(imageSendController) { input: StringTrigger ->
            if (input.isActioning) {
                val userLocal = localUserRepository.loggedInUser
                if (userLocal.isValid && friendId != null) {
                    return@switchMap chatRepository.ActionSendImageMessage(getApplication(), userLocal.token!!, userLocal.id!!, friendId!!, input.data)
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
        val userLocal = localUserRepository.loggedInUser
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
    fun sendImageMessage(path: String?) {
        imageSendController.value = StringTrigger.getActioning(path)
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
        val userLocal = localUserRepository.loggedInUser
        if (userLocal.isValid && conversationId != null) {
            chatRepository.ActionFetchMessages(
                    userLocal.token!!,
                    conversationId,
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
        val userLocal = localUserRepository.loggedInUser
        if (userLocal.isValid && conversationId != null) {
            chatRepository.ActionFetchNewMessages(
                    userLocal.token!!,
                    conversationId,
                    bottomId
            )
        }
    }

    /**
     * 控制获取完整的消息记录列表
     */
    fun loadMore() {
        val userLocal = localUserRepository.loggedInUser
        if (userLocal.isValid && conversationId != null) {
            chatRepository.ActionFetchMessages(
                    userLocal.token!!,
                    conversationId,
                    topId,
                    topTime,
                    pageSize,
                    LIST_ACTION.PUSH_HEAD
            )
        }
    }

    val myAvatar: String?
        get() {
            val userLocal = localUserRepository.loggedInUser
            return userLocal.avatar
        }

    val myId: String?
        get() {
            val userLocal = localUserRepository.loggedInUser
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
            chatRepository.ActionGetIntoConversation(context, localUserRepository.loggedInUser.id!!,
                    friendId!!, conversationId!!)
        }
    }

    /**
     * 声明离开对话
     */
    fun leftConversation(context: Context) {
        if (conversationId != null && localUserRepository.isUserLoggedIn) {
            chatRepository.ActionLeftConversation(context, localUserRepository.loggedInUser.id!!,
                    conversationId!!)
        }
    }

    fun markAllRead(context: Context, topId: String) {
        if (conversationId != null && localUserRepository.isUserLoggedIn) {
            chatRepository.ActionMarkAllRead(context, myId!!, conversationId!!,
                    topTime!!, pageSize)
        }
    }

    fun markRead(context: Context, chatMessage: ChatMessage) {
        chatRepository.ActionMarkRead(context, chatMessage.getToId(), chatMessage.getId(), chatMessage.getConversationId())
    }

    init {
        friendStateController = Transformations.map(chatRepository.friendsStateController) { input: FriendStateTrigger ->
            Log.e("apply", input.online.toString())
            if (friendId == null) {
                return@map FriendStateTrigger.still
            }
            if (input.id == friendId) {
                return@map FriendStateTrigger.getActioning(input.id, input.online)
            }
            FriendStateTrigger.still
        }
    }
}