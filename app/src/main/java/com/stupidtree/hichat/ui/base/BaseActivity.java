package com.stupidtree.hichat.ui.base;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

import butterknife.ButterKnife;

/**
 * 本项目所有Activity的基类
 * @param <T> 泛型T指定的是这个页面绑定的ViewModel
 */
public abstract class BaseActivity<T extends ViewModel> extends AppCompatActivity {
    /**
     * 每个Acitivity绑定一个ViewModel
     */
    protected T viewModel;

    /**
     * 所有继承BaseActivity的Activity都要实现以下几个函数
     */
    //获取这个Activity的布局id
    protected abstract int getLayoutId();
    //获取ViewModel的具体类型
    protected abstract Class<T> getViewModelClass();
    //为Activity中的View设置行为
    protected abstract void initViews();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        //所有的Activity都可以使用ButterKnife进行View注入，十分方便
        ButterKnife.bind(this);
        //对ViewModel进行初始化
        if(getViewModelClass()!=null){
            viewModel = new ViewModelProvider(this).get(getViewModelClass());
        }
        //调用这个函数
        initViews();
    }

    /**
     * 设置Activity的窗口属性
     * @param statusBar 是否让状态栏沉浸
     * @param darkColor 状态栏图标的颜色是否显示为深色
     * @param navi 是否让导航栏沉浸（一些手机底部会开启三大金刚）
     */
    protected void setWindowParams(Boolean statusBar, Boolean darkColor, Boolean navi){
        if(darkColor){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }else{
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        /* if (AppCompatDelegate.getDefaultNightMode()!=AppCompatDelegate.MODE_NIGHT_YES&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&darkColor)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); */
        if(statusBar)getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(navi)getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    /**
     * 设置一个带返回按钮的Toolbar
     * @param toolbar 已经初始化的一个Toolbar
     */
    protected void setToolbarActionBack(Toolbar toolbar){
        //设置为系统支持的ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }

    /**
     * 获取这个Activity本身
     * @return Activity自己
     */
    protected BaseActivity<T> getThis(){
        return this;
    }
}
