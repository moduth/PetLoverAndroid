package com.morecruit.domain.model.user;

import com.google.gson.annotations.SerializedName;
import com.morecruit.domain.model.MrResponse;

/**
 * X小时内如果发送总数超过N条，则增加图形验证码检测，请客户端把 data.captcha 显示在前端让用户输入
 *
 * @author markzhai on 16/3/4
 * @version 1.0.0
 */
public class VerifyCode extends MrResponse{

    @SerializedName("data")
    private int secs;

    @SerializedName("captcha")
    private String captcha;

    @SerializedName("sess_id")
    private SessionId sessionId;

    public void setSecs(int secs) {
        this.secs = secs;
    }

    public int getSecs() {
        return secs;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionId sessionId) {
        this.sessionId = sessionId;
    }
}
