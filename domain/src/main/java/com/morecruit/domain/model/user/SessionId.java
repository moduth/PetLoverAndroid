package com.morecruit.domain.model.user;

import com.google.gson.annotations.SerializedName;

/**
 * @author markzhai on 16/3/7
 * @version 1.0.0
 */
public class SessionId {

    @SerializedName("name")
    private String name;

    @SerializedName("value")
    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
