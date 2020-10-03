package com.stupidtree.hichat.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.data.repository.LocalUserRepository;
import com.stupidtree.hichat.service.SocketIOClientService;

import static android.content.Context.BIND_AUTO_CREATE;

public class MainViewModel extends ViewModel {

    /**
     * 仓库区
     */
    //本地用户仓库
    public LocalUserRepository localUserRepository;

    public MainViewModel(){
        localUserRepository = LocalUserRepository.getInstance();
    }





}
