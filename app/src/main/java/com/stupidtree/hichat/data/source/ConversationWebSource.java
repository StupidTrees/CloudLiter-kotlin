package com.stupidtree.hichat.data.source;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.ApiResponse;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.model.UserProfile;
import com.stupidtree.hichat.data.model.UserSearched;
import com.stupidtree.hichat.service.ConversationService;
import com.stupidtree.hichat.service.LiveDataCallAdapter;
import com.stupidtree.hichat.service.ConversationService;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.welcome.login.LoginResult;
import com.stupidtree.hichat.ui.welcome.signup.SignUpResult;
import com.stupidtree.hichat.utils.JsonUtils;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.stupidtree.hichat.service.codes.SUCCESS;
import static com.stupidtree.hichat.service.codes.TOKEN_INVALID;
import static com.stupidtree.hichat.service.codes.USER_ALREADY_EXISTS;
import static com.stupidtree.hichat.service.codes.WRONG_PASSWORD;
import static com.stupidtree.hichat.service.codes.WRONG_USERNAME;

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
     * @param token 令牌
     * @param id 用户id
     * @return 获取结果
     */
    public LiveData<DataState<List<Conversation>>> getConversations(@NonNull String token, @Nullable String id) {
        return Transformations.map(service.getConversations(token, id), input -> {

            if(null==input){
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
     * @param token 令牌
     * @param userId 我的id
     * @param friendId 朋友的id
     * @return 查询结果
     */
    public LiveData<DataState<Conversation>> queryConversation(@NonNull String token, @Nullable String userId,@NonNull String friendId) {
        return Transformations.map(service.queryConversation(token, userId, friendId), new Function<ApiResponse<Conversation>, DataState<Conversation>>() {
            @Override
            public DataState<Conversation> apply(ApiResponse<Conversation> input) {
                if(null==input){
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


}
