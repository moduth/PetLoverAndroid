package com.morecruit.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * 全局响应格式
 *
 * @author markzhai on 16/3/3
 * @version 1.0.0
 */
public class MrResponse {

    @SerializedName("status_no")
    private int statusCode;

    @SerializedName("status_msg")
    private String statusMessage;

    @SerializedName("time")
    private long time;

    /**
     * 响应内容
     */
    @SerializedName("data")
    public Object data;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * status_no	详细描述
     * -1	普通异常，详见 status_msg 字段描述
     * -1000	用户登录凭证不合法，请先登录或重新登录
     * -1010	请前往完善用户信息（昵称、头像）
     * -1020	第三方账号登陆后，请前往绑定官方账号（详见文档）
     * -9990	APPID 不合法
     * -9991	APPID 对应的应用信息不存在，请联系管理员
     * -9992	APPVER 不合法
     * -9999	应用传输数据解密失败，请联系管理员
     * -999	未知的其他异常
     * -404	指定目标不存在或已删除
     * -2001	请输入正确的图形验证码
     *
     * @return 响应代码
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return 响应文本
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * @return 当前时间戳
     */
    public long getTime() {
        return time;
    }
}
