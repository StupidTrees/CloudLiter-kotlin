package com.stupidtree.cloudliter

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.stupidtree.accessibility.LightEngine

/**
 * APP类
 */
class CApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LightEngine.init(this)

    }

}