package com.stupidtree.hichat.ui.search;

import com.stupidtree.hichat.ui.base.Trigger;

public class SearchTrigger extends Trigger {
    private String searchText;

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public static SearchTrigger getSearchInstance(String text){
        SearchTrigger st = new SearchTrigger();
        st.setSearchText(text);
        st.setActioning();
        return st;
    }
}
