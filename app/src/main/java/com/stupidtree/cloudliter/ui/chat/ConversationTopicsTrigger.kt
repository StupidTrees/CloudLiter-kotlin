package com.stupidtree.cloudliter.ui.chat

import com.google.gson.Gson
import com.stupidtree.cloudliter.ui.wordcloud.WordCloudEntity
import com.stupidtree.component.data.Trigger

class ConversationTopicsTrigger: Trigger() {
    var conversationId:String = ""
    var topics:MutableList<WordCloudEntity> = mutableListOf()


    companion object{

        fun create(conversationId:String,topicsString: String):ConversationTopicsTrigger{
            val rrs = ConversationTopicsTrigger()
            val gson = Gson()
            val list =  gson.fromJson(topicsString,List::class.java)
            for(l in list){
                try {
                    rrs.topics.add(gson.fromJson(l.toString(),WordCloudEntity::class.java))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            rrs.setActioning()
            rrs.conversationId = conversationId
            return rrs
        }
    }
}