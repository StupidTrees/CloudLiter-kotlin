package com.stupidtree.hichat.data.model;

public class WordsTag {
    private String name;
    private int left;
    private int top;
    private int right;
    private int bottom;
    private int textsize;
//    private float frequency;

    public WordsTag(String name, int textsize, int left, int top, int right, int bottom) {
        this.name = name;
        this.textsize = textsize;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
//        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public float getFrequency() {
//        return frequency;
//    }
//
//    public void setFrequency() {
//        this.frequency = frequency;
//    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getTextsize() {
        return textsize;
    }

    public void setTextsize(int textsize) {
        this.textsize = textsize;
    }
}
