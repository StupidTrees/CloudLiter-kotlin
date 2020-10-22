package com.stupidtree.hichat.data.source;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.service.ChatMessageService;
import com.stupidtree.hichat.service.LiveDataCallAdapter;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.JsonUtils;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;

import static com.stupidtree.hichat.service.codes.SUCCESS;
import static com.stupidtree.hichat.service.codes.TOKEN_INVALID;

/**
 * 层次：DataSource
 * 消息记录的数据源
 * 类型：网络数据
 * 数据：异步读，异步写
 */
public class ChatMessageWebSource extends BaseWebSource<ChatMessageService> {

    //单例模式
    private static volatile ChatMessageWebSource instance;

    public static ChatMessageWebSource getInstance() {
        if (instance == null) {
            instance = new ChatMessageWebSource();
        }
        return instance;
    }

    public ChatMessageWebSource() {
        super(new Retrofit.Builder()
                .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://hita.store:3000").build());
    }


    /**
     * 获取某对话下的所有消息记录
     *
     * @param token 令牌
     * @param id    对话id
     * @return 获取结果
     */
    public LiveData<DataState<List<ChatMessage>>> getMessages(@NonNull String token, @Nullable String id, String fromId, int pageSize) {
        // Log.e("getMes",token+"-"+fromId+"-"+pageSize);
        return Transformations.map(service.getChatMessages(token, id, fromId, pageSize), input -> {
            if (null == input) {
                return new DataState<>(DataState.STATE.FETCH_FAILED);
            }
            Log.e("id", id);
            Log.e("get_messages", input.toString());
            switch (input.getCode()) {
                case SUCCESS:
                    return new DataState<>(input.getData());
                case TOKEN_INVALID:
                    return new DataState<>(DataState.STATE.TOKEN_INVALID);
                default:
                    return new DataState<>(DataState.STATE.FETCH_FAILED);
            }
        });
    }

    public Call<ApiResponse<List<ChatMessage>>> getMessagesCall(@NonNull String token, @Nullable String id, String fromId, int pageSize) {
        // Log.e("getMes",token+"-"+fromId+"-"+pageSize);
        return service.getChatMessagesCall(token, id, fromId, pageSize);
    }


    /**
     * 拉取最新消息
     *
     * @param token   令牌
     * @param id      对话id
     * @param afterId 查询该id之后的消息
     * @return 获取结果
     */
    public LiveData<DataState<List<ChatMessage>>> pullLatestMessages(@NonNull String token, @Nullable String id, String afterId) {
        return Transformations.map(service.pullLatestChatMessages(token, id, afterId), input -> {
            if (null == input) {
                return new DataState<>(DataState.STATE.FETCH_FAILED);
            }
            switch (input.getCode()) {
                case SUCCESS:
                    return new DataState<>(input.getData());
                case TOKEN_INVALID:
                    return new DataState<>(DataState.STATE.TOKEN_INVALID);
                default:
                    return new DataState<>(DataState.STATE.FETCH_FAILED);
            }
        });
    }


    /**
     * 发送图片
     *
     * @param token 令牌
     * @param toId  朋友id
     * @param file  文件
     * @return 返回结果
     */
    public LiveData<DataState<ChatMessage>> sendImageMessage(@NonNull String token, @NonNull String toId, @NonNull MultipartBody.Part file,@NonNull String uuid) {
        return Transformations.map(service.sendImageMessage(token, toId, file,uuid), input -> {
            Log.e("发送图片结果", String.valueOf(input));
            if (input == null) {
                return new DataState<>(DataState.STATE.FETCH_FAILED);
            }
            switch (input.getCode()) {
                case SUCCESS:
                    return new DataState<>(input.getData());
                case TOKEN_INVALID:
                    return new DataState<>(DataState.STATE.TOKEN_INVALID);
                default:
                    return new DataState<>(DataState.STATE.FETCH_FAILED, input.getMessage());
            }
        });
    }


    @Override
    protected Class<ChatMessageService> getServiceClass() {
        return ChatMessageService.class;
    }
}
