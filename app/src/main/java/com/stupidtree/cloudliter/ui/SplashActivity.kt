package com.stupidtree.cloudliter.ui

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import com.stupidtree.cloudliter.ui.main.MainActivity

/**
 * logo页Activity
 * 啥都不干，就显示一张logo，等静态资源都加载完了自动跳转到MainActivity
 */
class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}