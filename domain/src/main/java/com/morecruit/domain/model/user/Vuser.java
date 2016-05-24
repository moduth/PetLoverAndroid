package com.morecruit.domain.model.user;

import com.google.gson.annotations.SerializedName;
import com.morecruit.domain.model.MrResponse;

/**
 * @author markzhai on 16/3/4
 * @version 1.0.0
 */
public class Vuser extends MrResponse{
    /**
     * __vuser : 37682fb2e6571ccd935797d4ea587c43cc52bf6b-ca941550277a8b5e3c66bbcf16c8fd84d3740da7
     */
    @SerializedName("data")
    public String data;

    @Override
    public Object getData() {
        return data;
    }

    public String getVuser(){
        return data;
    }
}
