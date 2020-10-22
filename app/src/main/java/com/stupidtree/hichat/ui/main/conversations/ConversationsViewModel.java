package com.stupidtree.hichat.ui.main.conversations;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.ConversationRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.HashMap;
import java.util.List;

public class ConversationsViewModel extends AndroidViewModel {

    /**
     * 数据区
     */
    //数据本体：列表数据
    private MediatorLiveData<DataState<List<Conversation>>> listData;

    //数据本体：未读消息
    private LiveData<DataState<HashMap<String, Integer>>> unreadMessageState;
    private final HashMap<String, Integer> unreadMessages = new HashMap<>();

    /**
     * 仓库区
     */
    //对话仓库
    private final ConversationRepository conversationRepository;
    //本地用户仓库
    private final LocalUserRepository localUserRepository;


    public ConversationsViewModel(Application application) {
        super(application);
        conversationRepository = ConversationRepository.getInstance(application);
        localUserRepository = LocalUserRepository.getInstance();

    }

    public LiveData<DataState<List<Conversation>>> getListData() {
        if(listData==null){
            listData = conversationRepository.getListLiveData();
        }
        return listData;
    }

    public LiveData<DataState<HashMap<String, Integer>>> getUnreadMessageState() {
        if (unreadMessageState == null) {
            unreadMessageState = Transformations.map(conversationRepository.getUnreadMessageState(), input -> {
                if (input.getListAction() == DataState.LIST_ACTION.APPEND) {
                    for (String key : input.getData().keySet()) {
                        Integer oldValue = unreadMessages.get(key);
                        if (oldValue == null) {
                            unreadMessages.put(key, 1);
                        } else {
                            unreadMessages.put(key, oldValue + 1);
                        }
                    }
                } else if (input.getListAction() == DataState.LIST_ACTION.DELETE) {
                    for (String key : input.getData().keySet()) {
                        Integer oldValue = unreadMessages.get(key);
                        Integer deleteValue = input.getData().get(key);
                        if (oldValue != null) {
                            if (oldValue <= 1) {
                                unreadMessages.remove(key);
                            } else if (deleteValue != null) {
                                unreadMessages.put(key, oldValue - deleteValue);
                            } else {
                                unreadMessages.put(key, oldValue - 1);
                            }
                        }
                    }
                } else if (input.getListAction() == DataState.LIST_ACTION.REPLACE_ALL) {
                    unreadMessages.clear();
                    unreadMessages.putAll(input.getData());
                }
                return input;
            });
        }
        return unreadMessageState;
    }

    public void startRefresh() {
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        if (userLocal.isValid()) {
            conversationRepository.ActionGetConversations(userLocal.getToken());
        } else{
            listData.setValue(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
        }
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
        Integer res = unreadMessages.get(conversation.getId());
//        for(ChatMessage cm:unreadMessages){
//            if(Objects.equals(cm.getConversationId(),conversation.getId())){
//                res++;
//            }
//        }
        if (res != null) {
            return res;
        } else {
            return 0;
        }
    }

    public void callOnline(@NonNull Context context) {
        UserLocal userLocal = localUserRepository.getLoggedInUser();
        if (userLocal.isValid()) {
            conversationRepository.ActionCallOnline(context, userLocal);
        }
    }
}