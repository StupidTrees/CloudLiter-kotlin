package com.stupidtree.hichat.ui.conversation;

import android.app.Application;
import android.content.Context;

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

import java.util.Objects;

public class ConversationViewModel extends AndroidViewModel {
    /**
     * 数据区
     */
    private LiveData<DataState<Conversation>> conversationLiveData;
    private MutableLiveData<StringTrigger> conversationTrigger = new MutableLiveData<>();

    /**
     * 仓库区
     */
    private ConversationRepository repository;
    private LocalUserRepository localUserRepository;

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

    public void startRefresh(String friendId){
        conversationTrigger.setValue(StringTrigger.getActioning(friendId));
    }
}
