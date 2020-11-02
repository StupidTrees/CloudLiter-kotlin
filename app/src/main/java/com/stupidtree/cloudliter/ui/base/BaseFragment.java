package com.stupidtree.cloudliter.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import butterknife.ButterKnife;

/**
 * 本项目所有Fragment的基类
 * @param <T> 泛型指定的是Fragment所绑定的ViewModel类型
 */
public abstract class BaseFragment<T extends ViewModel> extends Fragment {
    //本Fragment持有的根View
    private View view;
    //本Fragment绑定的ViewModel
    protected T viewModel;

    /**
     * 以下四个函数的作用和BaseActivity里的四个函数类似
     */
    protected abstract Class<T> getViewModelClass();
    protected abstract void initViews(View view);
    protected abstract int getLayoutId();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view==null){
            view = inflater.inflate(getLayoutId(),container,false);
        }
        //同样，所有的Fragment也支持ButterKnife的View注入
        ButterKnife.bind(this,view);
        //初始化ViewModel
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        initViews(view);
        return view;
    }
}
