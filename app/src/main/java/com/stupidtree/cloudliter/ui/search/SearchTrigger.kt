package com.stupidtree.cloudliter.ui.search

import com.stupidtree.component.data.Trigger

class SearchTrigger : Trigger() {
    var searchText: String? = null

    companion object {
        @JvmStatic
        fun getSearchInstance(text: String?): SearchTrigger {
            val st = SearchTrigger()
            st.searchText = text
            st.setActioning()
            return st
        }
    }
}