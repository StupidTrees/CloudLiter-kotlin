package com.stupidtree.hichat.ui.chat;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.ChatRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.MTransformations;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatViewModel extends ViewModel {
    /**
     * 数据区
     */
    //数据本体 朋友id
    private String friendId;
    //数据本体：对话对象
    private MediatorLiveData<DataState<Conversation>> conversationLiveData;
    //trigger:控制↑的刷新
    private MutableLiveData<ConversationTrigger> conversationController = new MutableLiveData<>();

    //数据本体：消息列表
    private LiveData<DataState<List<ChatMessage>>> listData;
    //trigger:控制器
    private MediatorLiveData<ChatListTrigger> listDataController;

    //状态数据：朋友在线状态
    private LiveData<DataState<FriendState>> friendStateLiveData;
    //trigger：控制↑的刷新
    private LiveData<FriendStateTrigger> friendStateController;

    /**
     * 仓库区
     */
    private ChatRepository chatRepository;
    private LocalUserRepository localUserRepository;

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public ChatViewModel() {
        chatRepository = ChatRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
        listDataController = MTransformations.map(chatRepository.getChatListController(), input -> {
            if (conversationLiveData.getValue() == null || conversationLiveData.getValue().getData() == null) {
                return ChatListTrigger.getStill();
            }
            if (Objects.equals(input.getNewMessage().getFromId(), conversationLiveData.getValue().getData().getFriendId())) {
                return input;
            } else {
                return ChatListTrigger.getStill();
            }
        });
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
     * 开始获取对话对象
     *
     * @param friendId 朋友id
     */
    public void startFetchingConversation(String friendId) {
        conversationController.setValue(ConversationTrigger.getActioning(friendId));
    }

    /**
     * 获取页面对应的Conversation对象
     *
     * @return 结果
     */
    public LiveData<DataState<Conversation>> getConversationLiveData() {
        if (conversationLiveData == null) {
            conversationLiveData = MTransformations.switchMap(conversationController, input -> {
                if (localUserRepository.isUserLoggedIn()) {
                    return chatRepository.queryConversation(Objects.requireNonNull(localUserRepository.getLoggedInUserDirect().getToken()),
                            localUserRepository.getLoggedInUserDirect().getId(), input.friendId);
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return conversationLiveData;
    }

    /**
     * 获取聊天列表状态数据
     * 注意：并不是存放完整的聊天列表，而是某一时刻的列表状态
     *
     * @return 状态数据
     */
    public LiveData<DataState<List<ChatMessage>>> getListData() {
        if (listData == null) {
            listData = Transformations.switchMap(listDataController, input -> {
                if (!input.isActioning()) {
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
                }
                UserLocal local = localUserRepository.getLoggedInUserDirect();
                if (!local.isValid()) {
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                }
                if (input.getMode() == ChatListTrigger.MODE.ADD_MESSAGE) {
                    return new MutableLiveData<>(new DataState<>(Collections.singletonList(input.getNewMessage())).setListAction(DataState.LIST_ACTION.APPEND));
                } else {
                    return chatRepository.getMessages(Objects.requireNonNull(local.getToken()), input.getConversationId());
                }
            });
        }
        return listData;
    }

    public LiveData<DataState<FriendState>> getFriendStateLiveData() {
        if (friendStateLiveData == null) {
            friendStateLiveData = Transformations.switchMap(friendStateController, input -> {
                if (input.isActioning()) {
                    if (input.online) {
                        return new MutableLiveData<>(new DataState<>(FriendState.getOnline()));
                    } else {
                        return new MutableLiveData<>(new DataState<>(FriendState.getOffline()));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return friendStateLiveData;
    }

    /**
     * 发送消息
     *
     * @param content 消息文本
     */
    public void sendMessage(String content) {
        UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
        if (userLocal.isValid() && getFriendId() != null) {
            ChatMessage message = new ChatMessage(userLocal.getId(),
                    getFriendId(), content);
            //通知仓库发送消息
            chatRepository.sendMessage(message);
            if (getConversationId() != null) {
                String conId = getConversationId();
                listDataController.setValue(ChatListTrigger.getActioning(conId, message));
            }
        }

    }

    public void bindService(Context context) {
        chatRepository.bindService(context);
    }

    public void unbindService(Context context) {
        chatRepository.unbindService(context);
    }

    /**
     * 控制获取完整的消息记录列表
     */
    public void fetchHistoryData() {
        if (getConversationId() != null) {
            String conId = getConversationId();
            listDataController.setValue(ChatListTrigger.getActioning(conId));
        }
    }

    @Nullable
    public String getMyAvatar() {
        UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
        return userLocal.getAvatar();
    }

    @Nullable
    public String getMyId() {
        UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
        return userLocal.getId();
    }

    @Nullable
    public String getFriendAvatar() {
        if (conversationLiveData.getValue() != null && conversationLiveData.getValue().getData() != null) {
            return conversationLiveData.getValue().getData().getFriendAvatar();
        }
        return null;
    }

    @Nullable
    public String getConversationId() {
        if (conversationLiveData.getValue() == null || conversationLiveData.getValue().getData() == null) {
            return null;
        }
        return conversationLiveData.getValue().getData().getId();
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
            chatRepository.getIntoConversation(context, Objects.requireNonNull(localUserRepository.getLoggedInUserDirect().getId()),
                    getFriendId(), getConversationId());
        }
    }

    /**
     * 声明离开对话
     */
    public void leftConversation(@NonNull Context context) {
        if (getConversationId() != null && localUserRepository.isUserLoggedIn()
        ) {
            chatRepository.leftConversation(context, Objects.requireNonNull(localUserRepository.getLoggedInUserDirect().getId()),
                    getConversationId());
        }
    }

    public void markAllRead(@NonNull Context context) {
        if (getConversationId() != null && localUserRepository.isUserLoggedIn()) {
            chatRepository.markAllRead(context, Objects.requireNonNull(getMyId()), getConversationId());
        }
    }

    public void markRead(@NonNull Context context, ChatMessage chatMessage) {
        chatRepository.markRead(context, chatMessage.getId());
    }

}