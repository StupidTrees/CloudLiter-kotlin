package com.stupidtree.hichat.data.repository;

import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.AppDatabase;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.source.ChatMessageDao;
import com.stupidtree.hichat.data.source.ChatMessageWebSource;
import com.stupidtree.hichat.data.source.SocketWebSource;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.chat.FriendStateTrigger;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_FRIEND_STATE_CHANGED;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_MESSAGE_SENT;
import static com.stupidtree.hichat.socket.SocketIOClientService.ACTION_RECEIVE_MESSAGE;

/**
 * 层次：Repository
 * 消息记录仓库
 */
public class ChatRepository {
    //不采用单例模式
    public static ChatRepository getInstance(@NonNull Context context) {
        return new ChatRepository(context.getApplicationContext());
    }


    //数据源1：网络类型数据，消息记录的网络数据源
    ChatMessageWebSource chatMessageWebSource;
    //数据源2：和后台服务通信的Service
    SocketWebSource socketWebSource;
    //数据源3：本地数据库的数据源
    ChatMessageDao chatMessageDao;

    // 动态数据对象：列表状态
    MediatorLiveData<DataState<List<ChatMessage>>> listDataState = new MediatorLiveData<>();
    LiveData<List<ChatMessage>> localListState = null; //本地获取数据
    LiveData<DataState<List<ChatMessage>>> webListState = null; //网络获取数据


    public ChatRepository(@NonNull Context context) {
        chatMessageWebSource = ChatMessageWebSource.getInstance();
        socketWebSource = new SocketWebSource();
        chatMessageDao = AppDatabase.getDatabase(context).chatMessageDao();
    }


    public MediatorLiveData<DataState<List<ChatMessage>>> getListDataState() {
        listDataState.addSource(socketWebSource.getNewMessageState(), message -> {
            if (message != null) {
                listDataState.removeSource(localListState);
                listDataState.setValue(new DataState<>(Collections.singletonList(message)).setListAction(DataState.LIST_ACTION.APPEND));
                saveMessageAsync(message);
            }
        });
        listDataState.addSource(getMessageSentSate(), chatMessageDataState -> {
            listDataState.removeSource(localListState);
            ChatMessage sentMessage = chatMessageDataState.getData();
            saveMessageAsync(sentMessage);
        });
        return listDataState;
    }


    /**
     * 动作：拉取聊天记录
     *
     * @param token          令牌
     * @param conversationId 对话id
     * @param fromId         现有列表顶部的消息id
     * @param pageSize       分页大小
     * @param action         操作：全部刷新或在头部插入
     */
    public void ActionFetchMessages(@NonNull String token, @Nullable String conversationId, Long fromId, int pageSize, DataState.LIST_ACTION action) {
        listDataState.removeSource(localListState);
        Log.e("fromId", String.valueOf(fromId));
        if (fromId == null) {
            localListState = chatMessageDao.getMessages(conversationId, pageSize);
        } else {
            localListState = chatMessageDao.getMessages(conversationId, pageSize, fromId);
        }
        final boolean[] tried = {false};
        listDataState.addSource(localListState, chatMessages -> {
            Log.e("onChanged", chatMessages.size() + "-" + tried[0] + "-" + action + "-" + fromId);
            if (!tried[0] && chatMessages.size() < pageSize) {
                listDataState.removeSource(webListState);
                webListState = chatMessageWebSource.getMessages(token, conversationId, String.valueOf(fromId), pageSize);
                listDataState.addSource(webListState, result -> {
                    tried[0] = true;
                    if (result.getState() == DataState.STATE.SUCCESS && result.getData().size() > 0) {
                        saveMessageAsync(result.getData());
                    } else {
                        listDataState.setValue(new DataState<>(chatMessages).setListAction(action));
                    }
                });
            } else if (!tried[0] && fromId == null && chatMessages.size() > 0) { //第一次获取，拉取新消息
                //先显示本地有的
                listDataState.setValue(new DataState<>(chatMessages).setListAction(action));
                //尝试查询本地缺的
                listDataState.removeSource(webListState);
                webListState = chatMessageWebSource.pullLatestMessages(token, conversationId, String.valueOf(chatMessages.get(0).getId()));
                listDataState.addSource(webListState, result -> {
                    tried[0] = true;
                    Log.e("补全本地消息", String.valueOf(result));
                    if (result.getState() == DataState.STATE.SUCCESS && result.getData().size() > 0) {
                        listDataState.removeSource(localListState);
                        saveMessageAsync(result.getData());
                        listDataState.setValue(new DataState<>(result.getData()).setListAction(DataState.LIST_ACTION.APPEND));
                    }
                });

            } else {
                listDataState.setValue(new DataState<>(chatMessages).setListAction(action));
            }
        });


    }

    /**
     * 动作：获取新消息
     *
     * @param token          令牌
     * @param conversationId 对话id
     * @param afterId        现有列表底部的消息id
     */
    public void ActionFetchNewMessages(@NonNull String token, @Nullable String conversationId, @NotNull Long afterId) {
        //尝试查询本地缺的
        listDataState.removeSource(webListState);
        webListState = chatMessageWebSource.pullLatestMessages(token, conversationId, String.valueOf(afterId));
        listDataState.addSource(webListState, result -> {
            Log.e("手动拉取本地未存新消息", afterId + "-" + result);
            if (result.getState() == DataState.STATE.SUCCESS && result.getData().size() > 0) {
                listDataState.removeSource(localListState);
                saveMessageAsync(result.getData());
                listDataState.setValue(new DataState<>(result.getData()).setListAction(DataState.LIST_ACTION.APPEND));
            }
        });
    }

    /**
     * 动作：发消息
     *
     * @param message 消息
     */
    public void ActionSendMessage(ChatMessage message) {
        socketWebSource.sendMessage(message);
        listDataState.setValue(new DataState<>(Collections.singletonList(message)).setListAction(DataState.LIST_ACTION.APPEND));
    }

    /**
     * 动作：标记对话全部已读
     *
     * @param context        上下文，为了发广播
     * @param userId         用户id
     * @param conversationId 对话id
     */
    public void ActionMarkAllRead(@NonNull Context context, @NonNull String userId, @NonNull String conversationId) {
        socketWebSource.markAllRead(context, userId, conversationId);
    }

    /**
     * 动作：标记某消息已读
     *
     * @param context        上下文
     * @param messageId      消息id
     * @param conversationId 对话id
     */
    public void ActionMarkRead(@NonNull Context context, @NonNull String messageId, @NonNull String conversationId) {
        socketWebSource.markRead(context, messageId, conversationId);
    }


    /**
     * 动作：进入对话
     *
     * @param context        上下文
     * @param userId         用户id
     * @param friendId       朋友id
     * @param conversationId 对话id
     */
    public void ActionGetIntoConversation(Context context, @NonNull String userId, @NonNull String friendId, @NonNull String conversationId) {
        socketWebSource.getIntoConversation(context, userId, friendId, conversationId);
    }

    /**
     * 动作：退出对话
     *
     * @param context        上下文
     * @param userId         用户id
     * @param conversationId 对话id
     */
    public void ActionLeftConversation(@NonNull Context context, @NonNull String userId, @NonNull String conversationId) {
        socketWebSource.leftConversation(context, userId, conversationId);
    }


    /**
     * 发送图片
     *
     * @param token    令牌
     * @param toId     朋友id
     * @param filePath 文件
     * @return 返回结果
     */
    public LiveData<DataState<ChatMessage>> ActionSendImageMessage(@NonNull Context context, @NonNull String token, @NonNull String fromId, @NonNull String toId, @NonNull String filePath) {
        MediatorLiveData<DataState<ChatMessage>> result = new MediatorLiveData<>();
        //读取图片文件
        File f = new File(filePath);
        ChatMessage tempMsg = new ChatMessage(fromId, toId, filePath);
        tempMsg.setType(ChatMessage.TYPE.IMG);
        listDataState.setValue(new DataState<>(Collections.singletonList(tempMsg)).setListAction(DataState.LIST_ACTION.APPEND));
        Luban.with(context)
                .setTargetDir(context.getExternalFilesDir("image").getAbsolutePath())
                .load(f)
                .setCompressListener(new OnCompressListener() { //设置回调
                    @Override
                    public void onStart() {
                        Log.e("luban", "开始压缩");
                    }

                    @Override
                    public void onSuccess(java.io.File file) {
                        Log.e("luban", "压缩成功");
                        // MutableLiveData<DataState<String>> result = new MutableLiveData<>();
                        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        //构造一个图片格式的POST表单
                        MultipartBody.Part body =
                                MultipartBody.Part.createFormData("upload", file.getName(), requestFile);
                        LiveData<DataState<ChatMessage>> sendResult = chatMessageWebSource.sendImageMessage(token, toId, body, tempMsg.getUuid());
                        result.addSource(sendResult, result::setValue);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("luban", "压缩失败");
                    }
                }).launch();    //启动压缩

        return result;
    }


    public void bindService(Context context) {
        IntentFilter IF = new IntentFilter();
        IF.addAction(ACTION_RECEIVE_MESSAGE);
        IF.addAction(ACTION_MESSAGE_SENT);
        IF.addAction(ACTION_FRIEND_STATE_CHANGED);
        context.registerReceiver(socketWebSource, IF);
        socketWebSource.bindService("Chat", context);
    }

    public void unbindService(Context context) {
        context.unregisterReceiver(socketWebSource);
        socketWebSource.unbindService(context);
    }

    private void saveMessageAsync(ChatMessage chatMessage) {
        new Thread(() -> chatMessageDao.saveMessage(Collections.singletonList(chatMessage))).start();
    }

    private void saveMessageAsync(List<ChatMessage> chatMessages) {
        new Thread(() -> chatMessageDao.saveMessage(chatMessages)).start();
    }


    public MutableLiveData<DataState<ChatMessage>> getMessageSentSate() {
        return socketWebSource.getMessageSentSate();
    }


    public MutableLiveData<FriendStateTrigger> getFriendsStateController() {
        return socketWebSource.getFriendStateController();
    }


}
