package com.stupidtree.hichat.ui.profile;

import com.stupidtree.hichat.ui.base.Trigger;

/**
 * 也是个携带String信息的Trigger
 */
public class ProfileTrigger extends Trigger {
    private String id;

    public String getId() {
        return id;
    }

    public static ProfileTrigger getActioning(String id){
        ProfileTrigger pt = new ProfileTrigger();
        pt.id = id;
        pt.setActioning();
        return pt;
    }
}
