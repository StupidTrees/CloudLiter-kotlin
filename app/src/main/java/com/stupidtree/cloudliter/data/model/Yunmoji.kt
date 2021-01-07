package com.stupidtree.cloudliter.data.model

class Yunmoji(val imageID: Int,val description:String) {
    private var lastStr: String? = null

    fun getLastName(position: Int): String {
        lastStr = if (position <= 8) {
            "[y00" + Integer.valueOf(position + 1).toString() + "]"
        } else {
            "[y0" + Integer.valueOf(position + 1).toString() + "]"
        }
        return lastStr as String
    }

}