package com.stupidtree.accessibility

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import com.stupidtree.accessibility.ai.segmentation.SegmentationSource

object LightEngine {
    private val lightAgents = mutableListOf<LightAgent>()
    private var frontWindow: LightAgent? = null


    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {
                val la = LightAgent.initForWindow(activity)
                lightAgents.add(la)
            }

            override fun onActivityResumed(activity: Activity) {
                findLightAgentForActivity(activity)?.let {
                    frontWindow = it
                    it.getPageDescription(activity)
                }
            }

            override fun onActivityPaused(activity: Activity) {
                findLightAgentForActivity(activity)?.let {
                    frontWindow = null
                }
            }

            override fun onActivityStopped(activity: Activity) {


            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                findLightAgentForActivity(activity)?.let {
                    lightAgents.remove(it)
                }

            }
        })
        SegmentationSource.initSegmentation(application)
    }


    private fun findLightAgentForActivity(activity: Activity): LightAgent? {
        val toR = lightAgents.filter {
            it.window.get() == activity
        }
        return if (toR.isNotEmpty()) toR[0] else null
    }
}