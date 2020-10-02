package com.stupidtree.hichat.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.source.ChatMessageWebSource;
import com.stupidtree.hichat.data.source.ConversationWebSource;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.List;

/**
 * 层次：Repository
 * 消息记录仓库
 */
public class ChatMessageRepository {
    //单例模式
    private static volatile ChatMessageRepository instance;

    public static ChatMessageRepository getInstance() {
        if(instance==null){
            instance = new ChatMessageRepository();
        }
        return instance;
    }

    //数据源1：网络类型数据，消息记录的网络数据源
    ChatMessageWebSource chatMessageWebSource;

    public ChatMessageRepository(){
        chatMessageWebSource = ChatMessageWebSource.getInstance();
    }

    /**
     * 获取某对话的消息记录
     * @param token 令牌
     * @param conversationId 对话id
     * @return 消息记录
     */
    public LiveData<DataState<List<ChatMessage>>> getMessages(@NonNull String token, @Nullable String conversationId){
        return chatMessageWebSource.getMessages(token,conversationId);
    }
}
