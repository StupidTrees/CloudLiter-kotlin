package com.stupidtree.cloudliter.ui.wordcloud

import kotlin.math.roundToInt

class WordCloudEntity :Comparable<WordCloudEntity>{
    var name:String=""
    var frequency:Float = 0f



    override fun compareTo(other: WordCloudEntity): Int {
        return (frequency-other.frequency).roundToInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordCloudEntity

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }


}