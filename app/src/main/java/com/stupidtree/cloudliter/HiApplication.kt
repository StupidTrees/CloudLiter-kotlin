package com.stupidtree.cloudliter

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * APP类
 */
class HiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set
    }
}