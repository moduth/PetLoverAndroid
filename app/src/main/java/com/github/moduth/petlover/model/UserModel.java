package com.github.moduth.petlover.model;

import android.databinding.BaseObservable;

/**
 * Created by Abner on 16/6/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class UserModel extends BaseObservable {

    private String mUid;

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }
}
