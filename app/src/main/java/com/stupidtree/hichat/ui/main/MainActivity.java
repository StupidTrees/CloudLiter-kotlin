package com.stupidtree.hichat.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.stupidtree.hichat.R;
import com.stupidtree.hichat.service.SocketIOClientService;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseTabAdapter;
import com.stupidtree.hichat.ui.main.contact.ContactFragment;
import com.stupidtree.hichat.ui.main.conversations.ConversationsFragment;
import com.stupidtree.hichat.ui.main.me.MeFragment;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.NotificationUtils;

import butterknife.BindView;

/**
 * 很显然，这是主界面
 */
public class MainActivity extends BaseActivity<MainViewModel> {

    @BindView(R.id.pager)
    ViewPager pager;

    @BindView(R.id.nav_view)
    BottomNavigationView navView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.add)
    ImageView add;

    @BindView(R.id.title)
    TextView title;


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
    }

    @Override
    protected void initViews() {
        setSupportActionBar(toolbar);
        title.setText(navView.getMenu().getItem(0).getTitle());
        //Objects.requireNonNull(getSupportActionBar()).setTitle(navView.getMenu().getItem(0).getTitle());
        pager.setAdapter(new BaseTabAdapter(getSupportFragmentManager(), 3) {
            @Override
            protected Fragment initItem(int position) {
                switch (position) {
                    case 0:
                        return ConversationsFragment.newInstance();
                    case 1:
                        return ContactFragment.newInstance();
                    default:
                        return MeFragment.newInstance();
                }
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
                case R.id.navigation_notifications:
                    pager.setCurrentItem(2);
                    break;

            }
            title.setText(item.getTitle());
            // Objects.requireNonNull(getSupportActionBar()).setTitle(item.getTitle());
            return true;
        });

        add.setOnClickListener(view -> {
            PopupMenu pm = new PopupMenu(getThis(), view);
            pm.inflate(R.menu.toolbar_more_popup_menu);
            pm.setOnMenuItemClickListener(item1 -> {
                if (item1.getItemId() == R.id.menu_action_make_friend) {
                    ActivityUtils.startSearchActivity(getThis());
                    return true;
                }
                return false;
            });
            pm.show();
        });
//        toolbar.setOnMenuItemClickListener(item -> {
//            if (item.getItemId() == R.id.menu_action_add) {
//                PopupMenu pm = new PopupMenu(getThis(),item.getActionView());
//                pm.inflate(R.menu.toolbar_more_popup_menu);
//                pm.setOnMenuItemClickListener(item1 -> {
//                    if(item1.getItemId()==R.id.menu_action_make_friend){
//                        ActivityUtils.startSearchActivity(getThis());
//                        return true;
//                    }
//                    return false;
//                });
//                pm.show();
//            }
//            return true;
//        });
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
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