package com.stupidtree.cloudliter.ui.base;


/**
 * Trigger和LiveData配合使用，用于触发某种UI上的功能
 */
public class Trigger{

    enum STATES{ACTION,STILL}
    STATES state = STATES.STILL;

    public static Trigger getActioning(){
        Trigger trigger = new Trigger();
        trigger.setActioning();
        return trigger;
    }

    public static Trigger getStill(){
        Trigger trigger = new Trigger();
        trigger.cancelActioning();
        return trigger;
    }
    public void setActioning(){
        state = STATES.ACTION;
    }

    public void cancelActioning(){
        state = STATES.STILL;
    }

    public boolean  isActioning(){
        return state==STATES.ACTION;
    }


}
