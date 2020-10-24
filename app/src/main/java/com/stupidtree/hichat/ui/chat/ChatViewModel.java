package com.stupidtree.hichat.ui.chat;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.ChatRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.StringTrigger;

import java.util.List;
import java.util.Objects;

public class ChatViewModel extends AndroidViewModel {
    /**
     * 数据区
     */
    //数据本体 朋友id
    private String friendId;
    //数据本体：对话对象
    private final MutableLiveData<Conversation> conversation = new MutableLiveData<>();

    //数据本体：消息列表
    private LiveData<DataState<List<ChatMessage>>> listData;


    //状态数据：朋友在线状态
    private LiveData<DataState<FriendState>> friendStateLiveData;
    //trigger：控制↑的刷新
    private final LiveData<FriendStateTrigger> friendStateController;

    //状态数据：消息发送结果
    private LiveData<DataState<ChatMessage>> messageSentState;

    //状态数据：图片消息发送
    private LiveData<DataState<ChatMessage>> imageSentResult;
    //控制↑的刷新
    private final MutableLiveData<StringTrigger> imageSendController = new MutableLiveData<>();


    private final int pageSize = 15;
    private Long topId = null;
    private Long bottomId = null;

    /**
     * 仓库区
     */
    private final ChatRepository chatRepository;
    private final LocalUserRepository localUserRepository;



    public void setConversation(Conversation conversation) {
       this.conversation.setValue(conversation);
       this.friendId = conversation.getFriendId();
    }

    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRepository = ChatRepository.getInstance(application);
        localUserRepository = LocalUserRepository.getInstance();
        friendStateController = Transformations.map(chatRepository.getFriendsStateController(), input -> {
            Log.e("apply", String.valueOf(input.online));
            if (getFriendId() == null) {
                return FriendStateTrigger.getStill();
            }
            if (Objects.equals(input.id, getFriendId())) {
                return FriendStateTrigger.getActioning(input.id, input.online);
            }
            return FriendStateTrigger.getStill();
        });


    }



    /**
     * 获取聊天列表状态数据
     * 注意：并不是存放完整的聊天列表，而是动作，比如插入、删除等
     * @return 状态数据
     */
    public LiveData<DataState<List<ChatMessage>>> getListData() {
        if (listData == null) {
            listData = Transformations.map(chatRepository.getListDataState(), input2 -> {
                if(input2.getData() != null && input2.getData().size() > 0){
                    switch (input2.getListAction()){
                        case APPEND:
                            bottomId = input2.getData().get(0).getId();
                            if(input2.getData().size()==1){ //某一条新消息到达
                                //不是这个窗口的消息
                                if(!Objects.equals(input2.getData().get(0).getConversationId(),getConversationId())){
                                    return new DataState<>(DataState.STATE.NOTHING);
                                }
                            }
                            break;
                        case REPLACE_ALL:
                            topId = input2.getData().get(input2.getData().size()-1).getId();
                            bottomId = input2.getData().get(0).getId();
                        case PUSH_HEAD:
                            topId = input2.getData().get(input2.getData().size()-1).getId();
                    }
                }
                return input2;
            });
        }
        return listData;
    }

    public LiveData<DataState<FriendState>> getFriendStateLiveData() {
        if (friendStateLiveData == null) {
            friendStateLiveData = Transformations.switchMap(friendStateController, input -> {
                if (input.isActioning()) {
                    switch (input.online) {
                        case "ONLINE":
                            return new MutableLiveData<>(new DataState<>(FriendState.getOnline()));
                        case "OFFLINE":
                            return new MutableLiveData<>(new DataState<>(FriendState.getOffline()));
                        case "YOU":
                            return new MutableLiveData<>(new DataState<>(FriendState.getWithYou()));
                        case "OTHER":
                            return new MutableLiveData<>(new DataState<>(FriendState.getWithOther()));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return friendStateLiveData;
    }

    public LiveData<DataState<ChatMessage>> getImageSentResult() {
        if(imageSentResult==null){
            imageSentResult = Transformations.switchMap(imageSendController, input -> {
                if(input.isActioning()){
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if(userLocal.isValid()&&getFriendId()!=null){
                        return chatRepository.ActionSendImageMessage(getApplication(),Objects.requireNonNull(userLocal.getToken()), Objects.requireNonNull(userLocal.getId()),getFriendId(),input.getData());
                    }else{
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return imageSentResult;
    }

    /**
     * 发送消息
     *
     * @param content 消息文本
     */
    public void sendMessage(String content) {
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        if (userLocal.isValid() && getFriendId() != null) {
            ChatMessage message = new ChatMessage(userLocal.getId(),
                    getFriendId(), content);
            //通知仓库发送消息
            chatRepository.ActionSendMessage(message);
        }

    }

    /**
     * 发送图片
     */
    public void sendImageMessage(String path){
        imageSendController.setValue(StringTrigger.getActioning(path));
    }
    public void bindService(Context context) {
        chatRepository.bindService(context);
    }

    public void unbindService(Context context) {
        chatRepository.unbindService(context);
    }

    /**
     * 第一次进入获取聊天记录
     */
    public void fetchHistoryData() {
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        if (userLocal.isValid()&&getConversationId()!=null) {
            chatRepository.ActionFetchMessages(
                    userLocal.getToken(),
                    getConversationId(),
                    null,
                    pageSize,
                    DataState.LIST_ACTION.REPLACE_ALL
            );
        }
    }

    /**
     * 手动拉取新消息
     */
    public void fetchNewData(){
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        if (userLocal.isValid()&&getConversationId()!=null) {
            chatRepository.ActionFetchNewMessages(
                    userLocal.getToken(),
                    getConversationId(),
                    bottomId
            );
        }
    }
    /**
     * 控制获取完整的消息记录列表
     */
    public void loadMore() {
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        if (userLocal.isValid()&&getConversationId()!=null) {
            chatRepository.ActionFetchMessages(
                    userLocal.getToken(),
                    getConversationId(),
                    topId,
                    pageSize,
                    DataState.LIST_ACTION.PUSH_HEAD
            );
        }

    }


    @Nullable
    public String getMyAvatar() {
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        return userLocal.getAvatar();
    }

    @Nullable
    public String getMyId() {
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        return userLocal.getId();
    }

    @Nullable
    public String getFriendAvatar() {
        if (conversation.getValue()!=null) {
            return conversation.getValue().getFriendAvatar();
        }
        return null;
    }

    public MutableLiveData<Conversation> getConversation() {
        return conversation;
    }

    @Nullable
    public String getConversationId() {
        if (conversation.getValue()==null) {
            return null;
        }
        return conversation.getValue().getId();
    }

    @Nullable
    public String getFriendId() {
        return friendId;
    }

    /**
     * 声明进入了对话
     */
    public void getIntoConversation(Context context) {
        if (getFriendId() != null && getConversationId() != null && localUserRepository.isUserLoggedIn()
        ) {
            chatRepository.ActionGetIntoConversation(context, Objects.requireNonNull(localUserRepository.getLoggedInUser().getId()),
                    getFriendId(), getConversationId());
        }
    }

    /**
     * 声明离开对话
     */
    public void leftConversation(@NonNull Context context) {
        if (getConversationId() != null && localUserRepository.isUserLoggedIn()
        ) {
            chatRepository.ActionLeftConversation(context, Objects.requireNonNull(localUserRepository.getLoggedInUser().getId()),
                    getConversationId());
        }
    }

    public void markAllRead(@NonNull Context context) {
        if (getConversationId() != null && localUserRepository.isUserLoggedIn()) {
            chatRepository.ActionMarkAllRead(context, Objects.requireNonNull(getMyId()), getConversationId());
        }
    }

    public void markRead(@NonNull Context context, ChatMessage chatMessage) {
        chatRepository.ActionMarkRead(context, String.valueOf(chatMessage.getId()), chatMessage.getConversationId());
    }

    public LiveData<DataState<ChatMessage>> getMessageSentState() {
        if (messageSentState == null) {
            messageSentState = Transformations.map(chatRepository.getMessageSentSate(), input -> input);
        }
        return messageSentState;
    }
}
