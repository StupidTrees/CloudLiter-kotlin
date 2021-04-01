package com.stupidtree.cloudliter.ui.welcome

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.ActivityWelcomeBinding
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.style.base.BaseTabAdapter
import com.stupidtree.cloudliter.ui.welcome.login.LoginFragment
import com.stupidtree.cloudliter.ui.welcome.signup.SignUpFragment
import com.stupidtree.cloudliter.utils.AnimationUtils

/**
 * 用户注册/登录页面
 * 只是个空壳，不需要ViewModel
 */
@SuppressLint("NonConstantResourceId")
class WelcomeActivity : BaseActivity<WelcomeViewModel, ActivityWelcomeBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.title = ""
        setToolbarActionBack(binding.toolbar)
    }

    override fun initViews() {

        supportActionBar?.title = ""
        //设置两个fragment，一个登录一个注册
        binding.pager.adapter = object : BaseTabAdapter(supportFragmentManager, 2) {
            override fun initItem(position: Int): Fragment {
                return if(position==0){
                    LoginFragment.newInstance()
                }else{
                    SignUpFragment.newInstance()
                }
            }

            override fun getPageTitle(position: Int): CharSequence {
                return if (position == 0) {
                    getString(R.string.Login)
                } else getString(R.string.sign_up)
            }
        }
        binding.tabs.setupWithViewPager(binding.pager)
    }

    override fun onStart() {
        super.onStart()
        AnimationUtils.floatAnim(binding.logo,0)
    }


    override fun initViewBinding(): ActivityWelcomeBinding {
        return ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<WelcomeViewModel> {
        return WelcomeViewModel::class.java
    }
}