package com.stupidtree.hichat.ui.chat;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.ChatRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatViewModel extends ViewModel {
    /**
     * 数据区
     */
    private MutableLiveData<Conversation> conversationMutableLiveData = new MutableLiveData<>();
    //数据本体：消息列表
    private LiveData<DataState<List<ChatMessage>>> listData;
    //trigger:控制器
    private MutableLiveData<ChatListTrigger> listDataController;

    //状态数据：朋友在线状态
    private LiveData<DataState<FriendState>> friendStateLiveData;
    //trigger：控制↑的刷新
    private LiveData<FriendStateTrigger> friendStateController;

    /**
     * 仓库区
     */
    private ChatRepository chatRepository;
    private LocalUserRepository localUserRepository;



    public ChatViewModel() {
        chatRepository = ChatRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
        listDataController = chatRepository.getChatListController();
        friendStateController = Transformations.map(chatRepository.getFriendsStateController(), input -> {
            Log.e("apply", String.valueOf(input.online));
            if(conversationMutableLiveData.getValue()==null){
                return FriendStateTrigger.getStill();
            }
            if(Objects.equals(input.id,conversationMutableLiveData.getValue().getFriendId())){
                return FriendStateTrigger.getActioning(input.id,input.online);
            }
            return FriendStateTrigger.getStill();
        });
    }


    /**
     * 初始化聊天对象
     *
     * @param conversationData 聊天对象
     */
    public void setConversationData(Conversation conversationData) {
        conversationMutableLiveData.setValue(conversationData);
    }

    /**
     * 获取页面对应的Conversation对象
     *
     * @return 结果
     */
    public MutableLiveData<Conversation> getConversationMutableLiveData() {
        return conversationMutableLiveData;
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
            friendStateLiveData = Transformations.switchMap(friendStateController, new Function<FriendStateTrigger, LiveData<DataState<FriendState>>>() {
                @Override
                public LiveData<DataState<FriendState>> apply(FriendStateTrigger input) {
                    if(input.isActioning()){
                        if (input.online) {
                            return new MutableLiveData<>(new DataState<>(FriendState.getOnline()));
                        } else {
                            return new MutableLiveData<>(new DataState<>(FriendState.getOffline()));
                        }
                    }
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
                }
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
        if (userLocal.isValid() && conversationMutableLiveData.getValue() != null) {
            ChatMessage message = new ChatMessage(userLocal.getId(),
                    conversationMutableLiveData.getValue().getFriendId(), content);
            //通知仓库发送消息
            chatRepository.sendMessage(message);
            if (conversationMutableLiveData.getValue() != null) {
                String conId = conversationMutableLiveData.getValue().getId();
                listDataController.setValue(ChatListTrigger.getActioning(conId, message));
            }
        }

    }

    public void bindService(Context context){
        chatRepository.bindService(context);
    }

    public void unbindService(Context context){
        chatRepository.unbindService(context);
    }

    /**
     * 控制获取完整的消息记录列表
     */
    public void fetchHistoryData() {
        if (conversationMutableLiveData.getValue() != null) {
            String conId = conversationMutableLiveData.getValue().getId();
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
        if (conversationMutableLiveData.getValue() != null) {
            return conversationMutableLiveData.getValue().getFriendAvatar();
        }
        return null;
    }

    @Nullable
    public String getFriendId() {
        if (conversationMutableLiveData.getValue() != null) {
            return conversationMutableLiveData.getValue().getFriendId();
        }
        return null;
    }

    /**
     * 声明进入了对话
     */
    public void getIntoConversation() {
        if (conversationMutableLiveData.getValue() != null && localUserRepository.isUserLoggedIn()
        ) {
            chatRepository.getIntoConversation(Objects.requireNonNull(localUserRepository.getLoggedInUserDirect().getId()),
                    conversationMutableLiveData.getValue().getFriendId(), conversationMutableLiveData.getValue().getId());
        }
    }

    /**
     * 声明离开对话
     */
    public void leftConversation() {
        if (conversationMutableLiveData.getValue() != null && localUserRepository.isUserLoggedIn()
        ) {
            chatRepository.leftConversation(Objects.requireNonNull(localUserRepository.getLoggedInUserDirect().getId()),
                    conversationMutableLiveData.getValue().getId());
        }
    }

    public void markAllRead() {
        if (conversationMutableLiveData.getValue() != null && localUserRepository.isUserLoggedIn()) {
            chatRepository.markAllRead(Objects.requireNonNull(getMyId()), conversationMutableLiveData.getValue().getId());
        }
    }

    private void markRead(ChatMessage chatMessage) {
        chatRepository.markRead(chatMessage);
    }

}
