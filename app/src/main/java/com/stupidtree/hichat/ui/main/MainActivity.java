package com.stupidtree.hichat.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.UserLocal;
import com.stupidtree.hichat.socket.SocketIOClientService;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseTabAdapter;
import com.stupidtree.hichat.ui.main.contact.ContactFragment;
import com.stupidtree.hichat.ui.main.conversations.ConversationsFragment;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.NotificationUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;

/**
 * 很显然，这是主界面
 */
@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseActivity<MainViewModel> {

    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;

    @BindView(R.id.drawer_navigationview)
    NavigationView navigationView;
    @BindView(R.id.pager)
    ViewPager pager;

    @BindView(R.id.nav_view)
    BottomNavigationView navView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.avatar)
    ImageView avatar;

    /**
     * 抽屉里的View
     */
    ImageView drawerAvatar;
    TextView drawerNickname;
    TextView drawerUsername;
    ViewGroup drawerHeader;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setWindowParams(true, true, false);
        super.onCreate(savedInstanceState);
        Intent bindIntent = new Intent(this, SocketIOClientService.class);
        startService(bindIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationUtils.checkNotification(this);
        setUserViews(viewModel.getLocalUser());
    }


    private void setUpDrawer() {
        View headerView = navigationView.inflateHeaderView(R.layout.activity_main_nav_header);
        drawerLayout.setStatusBarBackgroundColor(Color.TRANSPARENT);
        drawerLayout.setScrimColor(getBackgroundColorSecondAsTint());
        drawerLayout.setDrawerElevation(ImageUtils.dp2px(this, 84));
        drawerAvatar = headerView.findViewById(R.id.avatar);
        drawerHeader = headerView.findViewById(R.id.drawer_header);
        drawerNickname = headerView.findViewById(R.id.nickname);
        drawerUsername = headerView.findViewById(R.id.username);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NotNull View drawerView, float slideOffset) {
                //offset 偏移值
                View mContent = drawerLayout.getChildAt(0);
                float scale = 1 - slideOffset;
                float rightScale = 0.8f + scale * 0.2f;
                mContent.setTranslationX(-drawerView.getMeasuredWidth() * slideOffset);
                //mContent.setAlpha(0.3f+0.7f*scale);
                mContent.setPivotX(mContent.getMeasuredWidth());
                mContent.setPivotY(mContent.getMeasuredHeight() >> 1);
                mContent.invalidate();
                mContent.setScaleX(rightScale);
                mContent.setScaleY(rightScale);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                setUserViews(viewModel.getLocalUser());
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    protected void initViews() {
        setSupportActionBar(toolbar);
        setUpDrawer();
        title.setText(navView.getMenu().getItem(0).getTitle());
        //Objects.requireNonNull(getSupportActionBar()).setTitle(navView.getMenu().getItem(0).getTitle());
        pager.setAdapter(new BaseTabAdapter(getSupportFragmentManager(), 2) {
            @Override
            protected Fragment initItem(int position) {
                if (position == 0) {
                    return ConversationsFragment.newInstance();
                }
                return ContactFragment.newInstance();
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                super.destroyItem(container, position, object);
            }
        });
        pager.setOffscreenPageLimit(3);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MenuItem item = navView.getMenu().getItem(position);
                item.setChecked(true);
                title.setText(item.getTitle());
                //Objects.requireNonNull(getSupportActionBar()).setTitle(item.getTitle());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        navView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    pager.setCurrentItem(0);
                    break;
                case R.id.navigation_dashboard:
                    pager.setCurrentItem(1);
                    break;

            }
            title.setText(item.getTitle());
            // Objects.requireNonNull(getSupportActionBar()).setTitle(item.getTitle());
            return true;
        });

        avatar.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.END));
    }


    private void setUserViews(UserLocal userLocalInfo) {

        if (userLocalInfo.isValid()) { //如果已登录
            //装载头像
            ImageUtils.loadLocalAvatarInto(this, userLocalInfo.getAvatar(), drawerAvatar);
            ImageUtils.loadLocalAvatarInto(this, userLocalInfo.getAvatar(), avatar);
            //设置各种文字
            drawerUsername.setText(userLocalInfo.getUsername());
            drawerNickname.setText(userLocalInfo.getNickname());
            drawerHeader.setOnClickListener(view -> ActivityUtils.startProfileActivity(getThis(), viewModel.getLocalUser().getId()));
            navigationView.setNavigationItemSelectedListener(item -> {
                if(item.getItemId()==R.id.drawer_nav_my_profile){
                    ActivityUtils.startProfileActivity(getThis(), Objects.requireNonNull(viewModel.getLocalUser().getId()));
                    return true;
                }
                return false;
            });
        } else {
            //未登录的信息显示
            drawerUsername.setText(R.string.not_logged_in);
            drawerNickname.setText(R.string.please_log_in);
            drawerAvatar.setImageResource(R.drawable.place_holder_avatar);
            avatar.setImageResource(R.drawable.place_holder_avatar);
            drawerHeader.setOnClickListener(view -> ActivityUtils.startLoginActivity(getThis()));
            navigationView.setNavigationItemSelectedListener(item -> {
                if(item.getItemId()==R.id.drawer_nav_my_profile){
                    ActivityUtils.startLoginActivity(getThis());
                    return true;
                }
                return false;
            });
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        //返回桌面而非退出
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected Class<MainViewModel> getViewModelClass() {
        return MainViewModel.class;
    }


}