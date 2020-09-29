package com.stupidtree.hichat.ui.widgets;

import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.stupidtree.hichat.R;

import butterknife.ButterKnife;

/**
 * 透明背景的底部弹窗Fragment
 */
public abstract class TransparentBottomSheetDialog extends BottomSheetDialogFragment {

    @StringRes
    int confirm_text;
    @StringRes
    int cancel_text;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(),R.style.AppTheme);// your app theme here
        View v = inflater.cloneInContext(contextThemeWrapper).inflate(getLayoutId(), container, false);

        ButterKnife.bind(this,v);
        initViews(v);
        return v;
    }

    protected abstract int getLayoutId();
    protected abstract void initViews(View v);
}
