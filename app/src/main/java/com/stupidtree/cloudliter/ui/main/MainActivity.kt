package com.stupidtree.cloudliter.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.databinding.ActivityMainBinding
import com.stupidtree.cloudliter.service.socket.SocketIOClientService
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseTabAdapter
import com.stupidtree.cloudliter.ui.main.contact.ContactFragment
import com.stupidtree.cloudliter.ui.main.conversations.ConversationsFragment
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import com.stupidtree.cloudliter.utils.NotificationUtils
import me.ibrahimsn.lib.OnItemSelectedListener

/**
 * 很显然，这是主界面
 */
@SuppressLint("NonConstantResourceId")
class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    /**
     * 抽屉里的View
     */
    private var drawerAvatar: ImageView? = null
    private var drawerNickname: TextView? = null
    private var drawerUsername: TextView? = null
    private var drawerHeader: ViewGroup? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        val bindIntent = Intent(this, SocketIOClientService::class.java)
        startService(bindIntent)
    }

    override fun onStart() {
        super.onStart()
        try {
            NotificationUtils.checkNotification(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setUserViews(viewModel.localUser)
    }


    private fun setUpDrawer() {
        binding.drawerNavigationview.itemIconTintList = null
        val headerView = binding.drawerNavigationview.inflateHeaderView(R.layout.activity_main_nav_header)
        binding.drawer.setStatusBarBackgroundColor(Color.TRANSPARENT)
        binding.drawer.setScrimColor(getBackgroundColorSecondAsTint())
        binding.drawer.drawerElevation = ImageUtils.dp2px(this, 84f).toFloat()
        drawerAvatar = headerView.findViewById(R.id.avatar)
        drawerHeader = headerView.findViewById(R.id.drawer_header)
        drawerNickname = headerView.findViewById(R.id.nickname)
        drawerUsername = headerView.findViewById(R.id.username)
        binding.drawer.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                //offset 偏移值
                val mContent = binding.drawer.getChildAt(0)
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
                setUserViews(viewModel.localUser)
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    override fun initViews() {

        setUpDrawer()
        binding.title.text = getString(R.string.title_home)
        //Objects.requireNonNull(getSupportActionBar()).setTitle(navView.getMenu().getItem(0).getTitle());
        binding.pager.adapter = object : BaseTabAdapter(supportFragmentManager, 2) {
            override fun initItem(position: Int): Fragment {
                return if (position == 0) {
                    ConversationsFragment.newInstance()
                } else ContactFragment.newInstance()
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                super.destroyItem(container, position, `object`)
            }
        }
        binding.pager.offscreenPageLimit = 3
        binding.pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                binding.navView.itemActiveIndex = position
                binding.title.setText(when(position){
                    0->R.string.title_home
                    else->R.string.title_contact
                })
                //Objects.requireNonNull(getSupportActionBar()).setTitle(item.getTitle());
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.navView.onItemSelectedListener = object:OnItemSelectedListener {
            override fun onItemSelect(pos: Int): Boolean {
                binding.navView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                binding.pager.currentItem = pos
                return true
            }

        }
//        binding.navView.setOnNavigationItemSelectedListener { item: MenuItem ->
//            when (item.itemId) {
//                R.id.navigation_home -> binding.pager.currentItem = 0
//                R.id.navigation_dashboard -> binding.pager.currentItem = 1
//            }
//            binding.title.text = item.title
//            true
//        }
        binding.avatar.setOnClickListener { binding.drawer.openDrawer(GravityCompat.END) }
        binding.exitLayout.setOnClickListener {
            finish()
        }
    }

    private fun setUserViews(userLocalInfo: UserLocal) {
        if (userLocalInfo.isValid) { //如果已登录
            //装载头像
            ImageUtils.loadLocalAvatarInto(this, userLocalInfo.avatar, drawerAvatar!!)
            ImageUtils.loadLocalAvatarInto(this, userLocalInfo.avatar, binding.avatar)
            //设置各种文字
            drawerUsername!!.text = userLocalInfo.username
            drawerNickname!!.text = userLocalInfo.nickname
            drawerHeader!!.setOnClickListener { ActivityUtils.startProfileActivity(getThis(), viewModel.localUser.id!!) }
            binding.drawerNavigationview.setNavigationItemSelectedListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.drawer_nav_my_profile -> {
                        ActivityUtils.startProfileActivity(getThis(), viewModel.localUser.id!!)
                        true
                    }
                    R.id.drawer_nav_scan_qr -> {
                        ActivityUtils.startQRCodeActivity(getThis())
                        true
                    }
                    R.id.drawer_nav_discover_friend -> {
                        ActivityUtils.startSearchActivity(getThis())
                        true
                    }
                    else -> false
                }
            }
        } else {
            //未登录的信息显示
            drawerUsername!!.setText(R.string.not_logged_in)
            drawerNickname!!.setText(R.string.please_log_in)
            drawerAvatar!!.setImageResource(R.drawable.place_holder_avatar)
            binding.avatar.setImageResource(R.drawable.place_holder_avatar)
            drawerHeader!!.setOnClickListener { ActivityUtils.startLoginActivity(getThis()) }
            binding.drawerNavigationview.setNavigationItemSelectedListener { item: MenuItem ->
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
        if (binding.drawer.isDrawerOpen(GravityCompat.END)) {
            binding.drawer.closeDrawer(GravityCompat.END)
            return
        }
        //返回桌面而非退出
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }


    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun initViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
}