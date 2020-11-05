package com.stupidtree.cloudliter.ui.welcome

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import com.google.android.material.tabs.TabLayout
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseTabAdapter
import com.stupidtree.cloudliter.ui.welcome.login.LoginFragment
import com.stupidtree.cloudliter.ui.welcome.signup.SignUpFragment
import java.util.*

/**
 * 用户注册/登录页面
 * 只是个空壳，不需要ViewModel
 */
class WelcomeActivity : BaseActivity<ViewModel?>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.pager)
    var pager: ViewPager? = null

    @JvmField
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowParams(true, true, true)
        super.onCreate(savedInstanceState)
    }

    override fun getViewModelClass(): Class<ViewModel?>? {
        //不需要ViewModel。返回null即可
        return null
    }

    override fun initViews() {
        setSupportActionBar(toolbar)
        setToolbarActionBack(toolbar!!)
        supportActionBar?.title = ""
        //设置两个fragment，一个登录一个注册
        pager!!.adapter = object : BaseTabAdapter(supportFragmentManager, 2) {
            override fun initItem(position: Int): Fragment {
                if(position==0){
                    return LoginFragment.newInstance()
                }else{
                    return SignUpFragment.newInstance()
                }
            }

            override fun getPageTitle(position: Int): CharSequence {
                return if (position == 0) {
                    getString(R.string.Login)
                } else getString(R.string.sign_up)
            }
        }
        tabs!!.setupWithViewPager(pager)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_welcome
    }
}