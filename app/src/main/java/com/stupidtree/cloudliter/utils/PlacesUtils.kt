package com.stupidtree.cloudliter.utils

import android.content.Context
import com.stupidtree.cloudliter.R

object PlacesUtils {
    var map:MutableMap<String,String>?=null

    fun getNameForSceneKey(context: Context,key:String):String?{
        if(map==null){
            init(context)
        }
        return map!![key]
    }

    private fun init(context: Context){
        map = mutableMapOf()
        val keys = context.resources.getStringArray(R.array.places_keys)
        val values = context.resources.getStringArray(R.array.places_names_cn)
        for((i, str) in keys.withIndex()){
            map!![str] = values[i]
        }
    }
}