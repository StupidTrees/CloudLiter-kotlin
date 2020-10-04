package com.stupidtree.hichat.ui.main.conversations;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.ConversationRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.Trigger;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ConversationsViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：列表数据
    private LiveData<DataState<List<Conversation>>> listData;
    //Trigger：控制↑的刷新
    private MutableLiveData<Trigger> listDataController = new MutableLiveData<>();

    //数据本体：未读消息
    private LiveData<DataState<List<ChatMessage>>> unreadMessageState;
    private List<ChatMessage> unreadMessages = new LinkedList<>();

    /**
     * 仓库区
     */
    //对话仓库
    private ConversationRepository conversationRepository;
    //本地用户仓库
    private LocalUserRepository localUserRepository;


    public ConversationsViewModel() {
        conversationRepository = ConversationRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
        unreadMessageState = Transformations.map(conversationRepository.getUnreadMessages(), input -> {
            if (input.getListAction() == DataState.LIST_ACTION.APPEND) {
                unreadMessages.addAll(input.getData());
            } else if (input.getListAction() == DataState.LIST_ACTION.DELETE) {
                unreadMessages.removeAll(input.getData());
            } else if (input.getListAction() == DataState.LIST_ACTION.REPLACE_ALL) {
                unreadMessages.clear();
                unreadMessages.addAll(input.getData());
            }
            return input;
        });
    }

    public LiveData<DataState<List<Conversation>>> getListData() {
        if (listData == null) {
            listData = Transformations.switchMap(listDataController, input -> {
                UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
                if (userLocal.isValid()) {
                    return conversationRepository.getConversations(Objects.requireNonNull(userLocal.getToken()), userLocal.getId());
                } else {
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                }
            });
        }
        return listData;
    }

    public LiveData<DataState<List<ChatMessage>>> getUnreadMessageState() {
        return unreadMessageState;
    }

    public void startRefresh() {
        listDataController.setValue(Trigger.getActioning());
    }

    /**
     * 绑定服务
     *
     * @param context Activity对象
     */
    public void bindService(Context context) {
        conversationRepository.bindService(context);
    }

    public void unbindService(Context context) {
        conversationRepository.unbindService(context);
    }

    /**
     * 获取某个对话的未读数量
     *
     * @return 未读消息数目
     */
    public int getUnreadNumber(Conversation conversation) {
        int res = 0;
        for(ChatMessage cm:unreadMessages){
            if(Objects.equals(cm.getConversationId(),conversation.getId())){
                res++;
            }
        }
        return res;
    }

    public void callOnline(@NonNull Context context) {
        UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
        if (userLocal.isValid()) {
            conversationRepository.callOnline(context,userLocal);
        }
    }
}