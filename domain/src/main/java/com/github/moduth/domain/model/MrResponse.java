package com.github.moduth.domain.model;

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
