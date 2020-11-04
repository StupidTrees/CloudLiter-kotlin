package com.stupidtree.cloudliter.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.socket.SocketIOClientService
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseTabAdapter
import com.stupidtree.cloudliter.ui.main.contact.ContactFragment
import com.stupidtree.cloudliter.ui.main.conversations.ConversationsFragment
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.NotificationUtils
import java.util.*

/**
 * 很显然，这是主界面
 */
@SuppressLint("NonConstantResourceId")
class MainActivity : BaseActivity<MainViewModel>() {
    @JvmField
    @BindView(R.id.drawer)
    var drawerLayout: DrawerLayout? = null

    @JvmField
    @BindView(R.id.drawer_navigationview)
    var navigationView: NavigationView? = null

    @JvmField
    @BindView(R.id.pager)
    var pager: ViewPager? = null

    @JvmField
    @BindView(R.id.nav_view)
    var navView: BottomNavigationView? = null

    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.title)
    var title: TextView? = null

    @JvmField
    @BindView(R.id.avatar)
    var avatar: ImageView? = null

    /**
     * 抽屉里的View
     */
    var drawerAvatar: ImageView? = null
    var drawerNickname: TextView? = null
    var drawerUsername: TextView? = null
    var drawerHeader: ViewGroup? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowParams(true, true, false)
        super.onCreate(savedInstanceState)
        val bindIntent = Intent(this, SocketIOClientService::class.java)
        startService(bindIntent)
    }

    override fun onResume() {
        super.onResume()
        NotificationUtils.checkNotification(this)
        setUserViews(viewModel!!.localUser)
    }

    private fun setUpDrawer() {
        val headerView = navigationView!!.inflateHeaderView(R.layout.activity_main_nav_header)
        drawerLayout!!.setStatusBarBackgroundColor(Color.TRANSPARENT)
        drawerLayout!!.setScrimColor(getBackgroundColorSecondAsTint())
        drawerLayout!!.drawerElevation = ImageUtils.dp2px(this, 84f).toFloat()
        drawerAvatar = headerView.findViewById(R.id.avatar)
        drawerHeader = headerView.findViewById(R.id.drawer_header)
        drawerNickname = headerView.findViewById(R.id.nickname)
        drawerUsername = headerView.findViewById(R.id.username)
        drawerLayout!!.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                //offset 偏移值
                val mContent = drawerLayout!!.getChildAt(0)
                val scale = 1 - slideOffset
                val rightScale = 0.8f + scale * 0.2f
                mContent.translationX = -drawerView.measuredWidth * slideOffset
                //mContent.setAlpha(0.3f+0.7f*scale);
                mContent.pivotX = mContent.measuredWidth.toFloat()
                mContent.pivotY = (mContent.measuredHeight shr 1.toFloat().toInt()).toFloat()
                mContent.invalidate()
                mContent.scaleX = rightScale
                mContent.scaleY = rightScale
            }

            override fun onDrawerOpened(drawerView: View) {
                setUserViews(viewModel!!.localUser)
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    override fun initViews() {
        setSupportActionBar(toolbar)
        setUpDrawer()
        title!!.text = navView!!.menu.getItem(0).title
        //Objects.requireNonNull(getSupportActionBar()).setTitle(navView.getMenu().getItem(0).getTitle());
        pager!!.adapter = object : BaseTabAdapter(supportFragmentManager, 2) {
            override fun initItem(position: Int): Fragment {
                return if (position == 0) {
                    ConversationsFragment.newInstance()
                } else ContactFragment.newInstance()
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                super.destroyItem(container, position, `object`)
            }
        }
        pager!!.offscreenPageLimit = 3
        pager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                val item = navView!!.menu.getItem(position)
                item.isChecked = true
                title!!.text = item.title
                //Objects.requireNonNull(getSupportActionBar()).setTitle(item.getTitle());
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        navView!!.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> pager!!.currentItem = 0
                R.id.navigation_dashboard -> pager!!.currentItem = 1
            }
            title!!.text = item.title
            true
        }
        avatar!!.setOnClickListener { drawerLayout!!.openDrawer(GravityCompat.END) }
    }

    private fun setUserViews(userLocalInfo: UserLocal) {
        if (userLocalInfo.isValid) { //如果已登录
            //装载头像
            ImageUtils.loadLocalAvatarInto(this, userLocalInfo.avatar, drawerAvatar!!)
            ImageUtils.loadLocalAvatarInto(this, userLocalInfo.avatar, avatar!!)
            //设置各种文字
            drawerUsername!!.text = userLocalInfo.username
            drawerNickname!!.text = userLocalInfo.nickname
            drawerHeader!!.setOnClickListener { ActivityUtils.startProfileActivity(getThis(), viewModel!!.localUser.id!!) }
            navigationView!!.setNavigationItemSelectedListener { item: MenuItem ->
                if (item.itemId == R.id.drawer_nav_my_profile) {
                    ActivityUtils.startProfileActivity(getThis(),viewModel!!.localUser.id!!)
                    return@setNavigationItemSelectedListener true
                }
                false
            }
        } else {
            //未登录的信息显示
            drawerUsername!!.setText(R.string.not_logged_in)
            drawerNickname!!.setText(R.string.please_log_in)
            drawerAvatar!!.setImageResource(R.drawable.place_holder_avatar)
            avatar!!.setImageResource(R.drawable.place_holder_avatar)
            drawerHeader!!.setOnClickListener { ActivityUtils.startLoginActivity(getThis()) }
            navigationView!!.setNavigationItemSelectedListener { item: MenuItem ->
                if (item.itemId == R.id.drawer_nav_my_profile) {
                    ActivityUtils.startLoginActivity(getThis())
                    return@setNavigationItemSelectedListener true
                }
                false
            }
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed();
        if (drawerLayout!!.isDrawerOpen(GravityCompat.END)) {
            drawerLayout!!.closeDrawer(GravityCompat.END)
            return
        }
        //返回桌面而非退出
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }
}