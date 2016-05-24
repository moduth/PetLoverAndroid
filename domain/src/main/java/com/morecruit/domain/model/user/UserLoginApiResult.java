package com.morecruit.domain.model.user;

import com.google.gson.annotations.SerializedName;
import com.morecruit.domain.model.MrResponse;

/**
 * Created by Abner on 16/5/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class UserLoginApiResult extends MrResponse {


    @SerializedName("data")
    public String data;

}
