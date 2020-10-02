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

    private SocketIOClientService.JWebSocketClientBinder binder;
    private SocketIOClientService jWebSClientService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.e("MainActivity", "服务与活动成功绑定");
            binder = (SocketIOClientService.JWebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
            callOnline();

        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //服务与活动断开
            Log.e("MainActivity", "服务与活动成功断开");
        }
    };

    /**
     * 绑定服务
     */
    public void bindService(Context context) {
        Intent bindIntent = new Intent(context, SocketIOClientService.class);
        context.bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 声明用户上线
     */
   public void callOnline(){
        UserLocal userLocal = LocalUserRepository.getInstance().getLoggedInUserDirect();
        if(userLocal.isValid()&&binder!=null){
            binder.online(userLocal);
        }
    }
}
