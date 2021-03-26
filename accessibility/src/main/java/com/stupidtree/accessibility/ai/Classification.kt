package com.stupidtree.accessibility.ai

import kotlin.math.roundToInt

class Classification:Comparable<Classification> {
    var name:String?=""
    var confidence:Float= 0f
    override fun compareTo(other: Classification): Int {
        return (other.confidence-confidence).roundToInt()
    }

    override fun toString(): String {
        return "Classification(name=$name, confidence=$confidence)"
    }


}