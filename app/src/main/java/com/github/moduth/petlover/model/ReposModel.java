package com.github.moduth.petlover.model;

import android.databinding.BaseObservable;

/**
 * Created by Abner on 16/6/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class ReposModel extends BaseObservable {

    private int mId;

    private String mReposName;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getReposName() {
        return mReposName;
    }

    public void setReposName(String reposName) {
        mReposName = reposName;
    }
}
