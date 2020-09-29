package com.stupidtree.hichat.ui.welcome;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseTabAdapter;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.ui.welcome.login.LoginFragment;
import com.stupidtree.hichat.ui.welcome.signup.SignUpFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.viewpager.widget.ViewPager;

import java.util.Objects;

import butterknife.BindView;

/**
 * 用户注册/登录页面
 * 只是个空壳，不需要ViewModel
 */
public class WelcomeActivity extends BaseActivity<ViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.pager)
    ViewPager pager;

    @BindView(R.id.tabs)
    TabLayout tabs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setWindowParams(true,true,true);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Class<ViewModel> getViewModelClass() {
        //不需要ViewModel。返回null即可
        return null;
    }

    @Override
    protected void initViews() {
        setSupportActionBar(toolbar);
        setToolbarActionBack(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        //设置两个fragment，一个登录一个注册
        pager.setAdapter(new BaseTabAdapter(getSupportFragmentManager(),2) {
            @Override
            protected Fragment initItem(int position) {
                switch (position){
                    case 0:
                        return LoginFragment.newInstance();
                    case 1:
                        return SignUpFragment.newInstance();
                }
                return null;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) {
                    return getString(R.string.Login);
                }
                return getString(R.string.sign_up);
            }
        });
        tabs.setupWithViewPager(pager);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_welcome;
    }

}