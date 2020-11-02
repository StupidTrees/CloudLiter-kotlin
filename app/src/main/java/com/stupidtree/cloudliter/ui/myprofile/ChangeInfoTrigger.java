package com.stupidtree.cloudliter.ui.myprofile;

import com.stupidtree.cloudliter.ui.base.Trigger;

/**
 * 带String信息的Trigger
 */
public class ChangeInfoTrigger extends Trigger {
    String value;

    public static ChangeInfoTrigger getActioning(String value){
        ChangeInfoTrigger changeAvatarTrigger = new ChangeInfoTrigger();
        changeAvatarTrigger.setValue(value);
        changeAvatarTrigger.setActioning();
        return changeAvatarTrigger;
    }
    public String getValue() {
        return value;
    }

    public void setValue(String path) {
        this.value = path;
    }
}
