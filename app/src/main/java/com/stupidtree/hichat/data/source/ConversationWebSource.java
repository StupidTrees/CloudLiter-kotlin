package com.stupidtree.hichat.data.source;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.service.ConversationService;
import com.stupidtree.hichat.service.LiveDataCallAdapter;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.HashMap;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.stupidtree.hichat.service.codes.SUCCESS;
import static com.stupidtree.hichat.service.codes.TOKEN_INVALID;

/**
 * 层次：DataSource
 * 对话的数据源
 * 类型：网络数据
 * 数据：异步读，异步写
 */
public class ConversationWebSource extends BaseWebSource<ConversationService> {

    //单例模式
    private static volatile ConversationWebSource instance;

    public static ConversationWebSource getInstance() {
        if (instance == null) {
            instance = new ConversationWebSource();
        }
        return instance;
    }

    public ConversationWebSource() {
        super(new Retrofit.Builder()
                .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://hita.store:3000").build());
    }

    @Override
    protected Class<ConversationService> getServiceClass() {
        return ConversationService.class;
    }


    /**
     * 获取某用户所有的对话
     *
     * @param token 令牌
     * @return 获取结果
     */
    public LiveData<DataState<List<Conversation>>> getConversations(@NonNull String token) {
        return Transformations.map(service.getConversations(token), input -> {

            if (null == input) {
                return new DataState<>(DataState.STATE.FETCH_FAILED);
            }
            //Log.e("get_conversation",input.toString());
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
     * 查询两用户的对话对象
     *
     * @param token    令牌
     * @param userId   我的id
     * @param friendId 朋友的id
     * @return 查询结果
     */
    public LiveData<DataState<Conversation>> queryConversation(@NonNull String token, @Nullable String userId, @NonNull String friendId) {
        return Transformations.map(service.queryConversation(token, userId, friendId), new Function<ApiResponse<Conversation>, DataState<Conversation>>() {
            @Override
            public DataState<Conversation> apply(ApiResponse<Conversation> input) {
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
            }
        });
    }
    /**
     * 获取对话词云
     * @param token 用户令牌
     * @return 词频表
     */
    public LiveData<DataState<HashMap<String,Float>>> getWordCloud(@Nullable String token, @NonNull String userId, @NonNull String friendId){
        return Transformations.map(service.getWordCloud(token,userId,friendId), input -> {
            // Log.e("getWordCloud", String.valueOf(input));
            if(input!=null){
                switch (input.getCode()){
                    case SUCCESS:
                        return new DataState<>(input.getData());
                    case TOKEN_INVALID:
                        return new DataState<>(DataState.STATE.TOKEN_INVALID);
                    default:
                        return new DataState<>(DataState.STATE.FETCH_FAILED);
                }
            }
            return new DataState<>(DataState.STATE.FETCH_FAILED);
        });
    }

}
