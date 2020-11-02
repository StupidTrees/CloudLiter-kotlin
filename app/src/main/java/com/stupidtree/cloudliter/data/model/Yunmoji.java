package com.stupidtree.cloudliter.data.model;

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
        if (position <= 8) {
            lastStr = "[y00" + Integer.valueOf(position+1).toString() + "]";
        } else {
            lastStr = "[y0" + Integer.valueOf(position+1).toString() + "]";
        }
        return lastStr;
    }
}
