package com.morecruit.domain.model.user;

import com.google.gson.annotations.SerializedName;
import com.morecruit.domain.model.MrResponse;

/**
 * @author markzhai on 16/3/4
 * @version 1.0.0
 */
public class SettingEntity extends MrResponse{

    @SerializedName("recv_msg_switch")
    private int receiveMsgSwitch;

    @SerializedName("like_switch")
    private int likeSwitch;

    public void setReceiveMsgSwitch(int receiveMsgSwitch) {
        this.receiveMsgSwitch = receiveMsgSwitch;
    }

    public void setLikeSwitch(int likeSwitch) {
        this.likeSwitch = likeSwitch;
    }

    /**
     * 接收新消息通知总开关
     *
     * @return 0:关闭 1:开启
     */
    public int getReceiveMsgSwitch() {
        return receiveMsgSwitch;
    }

    /**
     * 是否接收到"赞"的通知
     *
     * @return 0:关闭 1:所有人 2:我关注的人
     */
    public int getLikeSwitch() {
        return likeSwitch;
    }
}
