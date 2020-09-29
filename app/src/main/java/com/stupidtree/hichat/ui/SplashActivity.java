package com.stupidtree.hichat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.stupidtree.hichat.ui.main.MainActivity;

/**
 * logo页Activity
 * 啥都不干，就显示一张logo，等静态资源都加载完了自动跳转到MainActivity
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

}


