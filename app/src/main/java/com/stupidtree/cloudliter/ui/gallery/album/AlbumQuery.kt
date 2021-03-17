package com.stupidtree.cloudliter.ui.gallery.album

class AlbumQuery(var pageSize:Int = 10,
                 var pageNum:Int = 0,
                 var key: String = "",
                 var nextPage:Boolean = false
){
    override fun toString(): String {
        return "AlbumQuery(pageSize=$pageSize, pageNum=$pageNum, key='$key', nextPage=$nextPage)"
    }
}