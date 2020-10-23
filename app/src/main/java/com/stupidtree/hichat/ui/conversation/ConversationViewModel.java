package com.stupidtree.hichat.ui.conversation;

import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.ConversationRepository;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.base.StringTrigger;
import com.stupidtree.hichat.ui.base.Trigger;

import java.util.HashMap;
import java.util.Objects;

public class ConversationViewModel extends AndroidViewModel {
    /**
     * 数据区
     */
    private LiveData<DataState<Conversation>> conversationLiveData;
    //数据本体：聊天词云
    private LiveData<DataState<HashMap<String,Float>>> wordCloudLiveData;
    private final MutableLiveData<StringTrigger> conversationTrigger = new MutableLiveData<>();


    /**
     * 仓库区
     */
    private final ConversationRepository repository;
    private final LocalUserRepository localUserRepository;

    public ConversationViewModel(Application application){
        super(application);
        repository = ConversationRepository.getInstance(application);
        localUserRepository = LocalUserRepository.getInstance();
    }

    public LiveData<DataState<Conversation>> getConversationLiveData() {
        if(conversationLiveData==null){
            conversationLiveData = Transformations.switchMap(conversationTrigger, (Function<StringTrigger, LiveData<DataState<Conversation>>>) input -> {
                if(input.isActioning()){
                    UserLocal userLocal = localUserRepository.getLoggedInUser();
                    if(userLocal.isValid()){
                        return repository.queryConversation(Objects.requireNonNull(userLocal.getToken()),userLocal.getId(),input.getData());
                    }else{
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return conversationLiveData;
    }


    public LiveData<DataState<HashMap<String, Float>>> getWordCloudLiveData() {
        if(wordCloudLiveData==null){
            wordCloudLiveData = Transformations.switchMap(conversationTrigger, input -> {
                UserLocal user = localUserRepository.getLoggedInUser();
                if (input.isActioning()) {
                    if (user.isValid()) {
                        return repository.getUserWordCloud(user.getToken(),user.getId(),input.getData());
                    } else {
                        return new MutableLiveData<>(new DataState<>(DataState.STATE.NOT_LOGGED_IN));
                    }
                }
                return new MutableLiveData<>(new DataState<>(DataState.STATE.NOTHING));
            });
        }
        return wordCloudLiveData;
    }

    @Nullable
    private String getConversationId(){
        if(getConversationLiveData().getValue()!=null){
            Conversation c = getConversationLiveData().getValue().getData();
            if(c!=null){
                return c.getId();
            }
        }
        return null;
    }
    public void startRefresh(String friendId){
        conversationTrigger.setValue(StringTrigger.getActioning(friendId));
    }

}
