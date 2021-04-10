package com.stupidtree.visual.face.expression

class ExpressionResult {
    var faceId:String = ""
    var expression:String = ""

    override fun toString(): String {
        return "faceId:${faceId},expression:${expression}"
    }
}