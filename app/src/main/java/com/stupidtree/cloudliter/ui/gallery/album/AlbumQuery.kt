package com.stupidtree.cloudliter.ui.gallery.album

class AlbumQuery(var pageSize: Int = 10,
                 var pageNum: Int = 0,
                 var key: String = "",
                 var mode: QType = QType.SCENE,
                 var nextPage: Boolean = false
) {
    enum class QType { SCENE, FRIEND }

    override fun toString(): String {
        return "AlbumQuery(pageSize=$pageSize, pageNum=$pageNum, key='$key', nextPage=$nextPage)"
    }
}