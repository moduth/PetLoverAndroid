package com.github.moduth.petlover.event;

/**
 * EventBus的基本事件定义，其他业务事件都继承该类进行拓展
 * <p>
 * @author markzhai on 15/7/29.
 */
public class BaseEvent {

    public boolean success = false;

    public int requestId; // 请求Id，全局唯一
    public int type; // 请求类型，见EventType
    public int code; // 网络请求返回码，对应网络请求里rc，网络请求不成功返回-1
    public String msg; // 返回消息，对应网络请求里me
    public Object data; // 返回数据

    // 扩展字段
    public Object ext1;

    public BaseEvent() {
    }

    /**
     * @param code code
     * @param msg  message description
     */
    public BaseEvent(int requestId, int type, int code, String msg) {
        this.requestId = requestId;
        this.type = type;
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return requestId + ", " + type + ", " + code + ", " + msg + ", " + data;
    }
}
