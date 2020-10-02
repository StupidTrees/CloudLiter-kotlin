package com.stupidtree.hichat.ui.base;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseActivityWithReceiver<H extends ViewModel> extends BaseActivity<H> {
    protected BroadcastReceiver receiver;
   // protected LocalBroadcastManager broadcastManager;

    @NonNull
    protected abstract BroadcastReceiver initReceiver();
    protected abstract IntentFilter getIntentFilter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = initReceiver();
        //broadcastManager = LocalBroadcastManager.getInstance(this);
        registerReceiver(receiver,getIntentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //broadcastManager.unregisterReceiver(receiver);
        unregisterReceiver(receiver);
    }
}
