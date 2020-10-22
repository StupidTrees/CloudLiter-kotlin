package com.stupidtree.hichat.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.stupidtree.hichat.data.AppDatabase;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.source.ConversationDao;
import com.stupidtree.hichat.data.source.ConversationWebSource;
import com.stupidtree.hichat.data.source.SocketWebSource;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_FRIEND_STATE_CHANGED;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_RECEIVE_MESSAGE;

/**
 * 层次：Repository
 * 对话仓库
 */
public class ConversationRepository {
    //单例模式
    private static volatile ConversationRepository instance;

    public static ConversationRepository getInstance(@NonNull Application application) {
        if (instance == null) {
            instance = new ConversationRepository(application.getApplicationContext());
        }
        return instance;
    }

    //数据源1：网络类型数据，对话的网络数据源
    ConversationWebSource conversationWebSource;
    //数据源2：长连接数据源
    SocketWebSource socketWebSource;
    //数据源3：对话的本地存储
    ConversationDao conversationDao;


    MediatorLiveData<DataState<List<Conversation>>> listLiveData = new MediatorLiveData<>();
    LiveData<List<Conversation>> listLocalData;
    LiveData<DataState<List<Conversation>>> listWebData;

    public ConversationRepository(@NonNull Context context) {
        conversationWebSource = ConversationWebSource.getInstance();
        socketWebSource = new SocketWebSource();
        conversationDao = AppDatabase.getDatabase(context).conversationDao();
    }

    public void bindService(Context context) {
        IntentFilter IF = new IntentFilter();
        IF.addAction(ACTION_RECEIVE_MESSAGE);
        IF.addAction(ACTION_FRIEND_STATE_CHANGED);
        context.registerReceiver(socketWebSource, IF);
        socketWebSource.bindService("conversation", context);
    }

    public void unbindService(Context context) {
        socketWebSource.unbindService(context);
        context.unregisterReceiver(socketWebSource);
    }


    public MediatorLiveData<DataState<List<Conversation>>> getListLiveData() {
        return listLiveData;
    }


    /**
     * 动作：获取对话列表
     *
     * @param token 令牌
     */
    public void ActionGetConversations(@NonNull String token) {
        listLiveData.removeSource(listLocalData);
        listLocalData = conversationDao.getConversations();
        AtomicBoolean tried = new AtomicBoolean(false);
        listLiveData.addSource(listLocalData, conversations -> {
            listLiveData.setValue(new DataState<>(conversations));
            //只进行一次网络拉取
            if (!tried.get()) {
                tried.set(true);
                listLiveData.removeSource(listWebData);
                listWebData = conversationWebSource.getConversations(token);
                listLiveData.addSource(listWebData, listDataState -> {
                    if (listDataState.getState() == DataState.STATE.SUCCESS) {
                        new Thread(() -> conversationDao.saveConversations(listDataState.getData())).start();
                    } else if (listDataState.getState() == DataState.STATE.FETCH_FAILED) {
                        listLiveData.setValue(new DataState<>(conversations, DataState.STATE.FETCH_FAILED));
                    }

                });
            }
        });
    }


    /**
     * 动作：通知上线
     *
     * @param context   上下文
     * @param userLocal 本地用户
     */
    public void ActionCallOnline(@NonNull Context context, @NonNull UserLocal userLocal) {
        socketWebSource.callOnline(context, userLocal);
    }


    public LiveData<DataState<HashMap<String, Integer>>> getUnreadMessageState() {
        return socketWebSource.getUnreadMessageState();
    }

    public LiveData<DataState<Conversation>> queryConversation(@NonNull String token, @Nullable String userId, @NonNull String friendId) {
        return conversationWebSource.queryConversation(token, userId, friendId);
    }
}
