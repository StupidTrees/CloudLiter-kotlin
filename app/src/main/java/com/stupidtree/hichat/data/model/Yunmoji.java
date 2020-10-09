package com.stupidtree.hichat.data.model;

import com.stupidtree.hichat.R;

public class Yunmoji {
    private int imageID;
    private String lastStr;

    public Yunmoji(int imageID) {
        this.imageID = imageID;
    }

    public int getImageID() {
        return imageID;
    }

    public String getLastname(int position) {
        switch (position){
            case 0:lastStr = "[y001]";break;
            case 1:lastStr = "[y002]";break;
            case 2:lastStr = "[y003]";break;
            case 3:lastStr = "[y004]";break;
            case 4:lastStr = "[y005]";break;
            case 5:lastStr = "[y006]";break;
            case 6:lastStr = "[y007]";break;
            case 7:lastStr = "[y008]";break;
            case 8:lastStr = "[y009]";break;
            case 9:lastStr = "[y010]";break;
            case 10:lastStr = "[y011]";break;
            case 11:lastStr = "[y012]";break;
            case 12:lastStr = "[y013]";break;
            case 13:lastStr = "[y014]";break;
            case 14:lastStr = "[y015]";break;
            case 15:lastStr = "[y016]";break;
            case 16:lastStr = "[y017]";break;
            case 17:lastStr = "[y018]";break;
            case 18:lastStr = "[y019]";break;
            case 19:lastStr = "[y020]";break;
            default:lastStr = "";
        }
        return lastStr;
    }
}
