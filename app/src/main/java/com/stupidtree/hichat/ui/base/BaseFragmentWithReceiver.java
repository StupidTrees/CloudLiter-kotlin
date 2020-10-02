package com.stupidtree.hichat.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseFragmentWithReceiver<H extends ViewModel> extends BaseFragment<H> {
    protected BroadcastReceiver receiver;
   // protected LocalBroadcastManager broadcastManager;


    @NonNull
    protected abstract BroadcastReceiver initReceiver(Context context);

    protected abstract IntentFilter getIntentFilter();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
       // broadcastManager = LocalBroadcastManager.getInstance(context);
        receiver = initReceiver(context);
        //broadcastManager.registerReceiver(receiver,getIntentFilter());
        context.registerReceiver(receiver,getIntentFilter());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //broadcastManager.unregisterReceiver(receiver);
        requireContext().unregisterReceiver(receiver);
        receiver = null;
    }

}
