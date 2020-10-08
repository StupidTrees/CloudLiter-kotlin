package com.stupidtree.hichat.ui.base;

public class StringTrigger extends Trigger {
    String data;
    public static StringTrigger getActioning(String data){
        StringTrigger stringTrigger = new StringTrigger();
        stringTrigger.setActioning();
        stringTrigger.data = data;
        return stringTrigger;
    }

    public String getData() {
        return data;
    }
}
