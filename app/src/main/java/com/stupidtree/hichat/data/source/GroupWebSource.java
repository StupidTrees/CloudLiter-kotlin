package com.stupidtree.hichat.data.source;

import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.stupidtree.hichat.data.model.ApiResponse;
import com.stupidtree.hichat.data.model.RelationGroup;
import com.stupidtree.hichat.service.GroupService;
import com.stupidtree.hichat.service.LiveDataCallAdapter;
import com.stupidtree.hichat.service.codes;
import com.stupidtree.hichat.ui.base.DataState;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.stupidtree.hichat.ui.base.DataState.STATE.FETCH_FAILED;
import static com.stupidtree.hichat.ui.base.DataState.STATE.SPECIAL;
import static com.stupidtree.hichat.ui.base.DataState.STATE.TOKEN_INVALID;

public class GroupWebSource extends BaseWebSource<GroupService> {

    //单例模式
    private static volatile GroupWebSource instance;

    public static GroupWebSource getInstance() {
        if (instance == null) {
            instance = new GroupWebSource();
        }
        return instance;
    }

    public GroupWebSource() {
        super(new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapter.LiveDataCallAdapterFactory.INSTANCE)
                .baseUrl("http://hita.store:3000").build());
    }

    @Override
    protected Class<GroupService> getServiceClass() {
        return GroupService.class;
    }

    /**
     * 获取我的所有消息
     *
     * @param token 令牌
     * @return 查询结果
     */
    public LiveData<DataState<List<RelationGroup>>> queryMyGroups(String token) {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.queryMyGroups(token), (Function<ApiResponse<List<RelationGroup>>, DataState<List<RelationGroup>>>) input -> {
            if (input == null) {
                return new DataState<>(FETCH_FAILED);
            } else {
                switch (input.getCode()) {
                    case codes.SUCCESS:
                        return new DataState<>(input.getData());
                    case codes.TOKEN_INVALID:
                        return new DataState<>(TOKEN_INVALID);
                    default:
                        return new DataState<>(FETCH_FAILED, input.getMessage());
                }
            }
        });
    }

    /**
     * 获取添加结果
     *
     * @param token 令牌
     * @return 查询结果
     */
    public LiveData<DataState<String>> addMyGroups(String token,String groupName) {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.addMyGroups(token,groupName), input -> {
            Log.e("her","！");
            if (input == null) {
                return new DataState<>(FETCH_FAILED);
            } else {
                switch (input.getCode()) {
                    case codes.SUCCESS:
                        System.out.println("used!!!");
                        System.out.println("websource stage: func:addMyGroup succeed");
                        return new DataState<>(input.getData());
                    case codes.TOKEN_INVALID:
                        System.out.println("websource stage: func:addMyGroup token_invalid");
                        return new DataState<>(TOKEN_INVALID);
                    case codes.GROUP_NAME_EXIST:
                        return new DataState<>(SPECIAL,input.getMessage());
                    default:
                        System.out.println("websource stage: func:addMyGroup default");
                        return new DataState<>(FETCH_FAILED, input.getMessage());
                }
            }
        });
    }


    /**
     * 获取删除结果
     *
     * @param token 令牌
     * @return 查询结果
     */
    public LiveData<DataState<String>> deleteMyGroups(String token,String groupName) {
        //当网络请求返回的结果解析、包装为DataState形式
        return Transformations.map(service.deleteMyGroups(token,groupName), input -> {
            Log.e("her","！");
            if (input == null) {
                return new DataState<>(FETCH_FAILED);
            } else {
                switch (input.getCode()) {
                    case codes.SUCCESS:
                        System.out.println("used!!!");
                        System.out.println("websource stage: func:deleteMyGroup succeed");
                        return new DataState<>(input.getData());
                    case codes.TOKEN_INVALID:
                        System.out.println("websource stage: func:deleteMyGroup token_invalid");
                        return new DataState<>(TOKEN_INVALID);
                    case codes.GROUP_NAME_EXIST:
                        return new DataState<>(SPECIAL,input.getMessage());
                    default:
                        System.out.println("websource stage: func:deleteMyGroup default");
                        return new DataState<>(FETCH_FAILED, input.getMessage());
                }
            }
        });
    }

}
