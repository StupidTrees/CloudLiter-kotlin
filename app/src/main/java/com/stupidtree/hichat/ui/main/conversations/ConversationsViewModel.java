package com.stupidtree.hichat.ui.main.conversations;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.ConversationRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.service.SocketIOClientService;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.Trigger;

import java.util.List;
import java.util.Objects;

import static android.content.Context.BIND_AUTO_CREATE;

public class ConversationsViewModel extends ViewModel {

    /**
     * 数据区
     */
    //数据本体：列表数据
    private LiveData<DataState<List<Conversation>>> listData;
    //Trigger：控制↑的刷新
    private MutableLiveData<Trigger> listDataController = new MutableLiveData<>();

    /**
     * 仓库区
     */
    //对话仓库
    private ConversationRepository conversationRepository;
    private LocalUserRepository localUserRepository;

    /**
     * 绑定服务
     */
    private SocketIOClientService.JWebSocketClientBinder binder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (SocketIOClientService.JWebSocketClientBinder) iBinder;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //服务与活动断开
            Log.e("MainActivity", "服务与活动成功断开");
        }
    };

    public ConversationsViewModel() {
        conversationRepository = ConversationRepository.getInstance();
        localUserRepository = LocalUserRepository.getInstance();
    }

    public LiveData<DataState<List<Conversation>>> getListData() {
        if(listData==null){
            listData = Transformations.switchMap(listDataController, input -> {
                UserLocal userLocal = localUserRepository.getLoggedInUserDirect();
                if(userLocal.isValid()){
                    return conversationRepository.getConversations(Objects.requireNonNull(userLocal.getToken()),userLocal.getId());
                }else{
                    return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                }
            });
        }
        return listData;
    }

    public void startRefresh(){
        listDataController.setValue(Trigger.getActioning());
    }

    /**
     * 绑定服务
     * @param context Activity对象
     */
    public void bindService(Context context) {
        Intent bindIntent = new Intent(context, SocketIOClientService.class);
        context.bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 获取某个对话的未读数量
     * @return 未读消息数目
     */
    public int getUnreadNumber(Conversation conversation){
        if(binder!=null){
            return binder.getUnread(conversation);
        }
        return 0;
    }
}