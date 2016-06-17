package com.github.moduth.data.exception;

import com.google.gson.Gson;
import com.github.moduth.domain.model.MrResponse;
import com.github.moduth.domain.model.repos.ReposEntity;

/**
 * Say like response's is not successful with an error code.
 *
 * @author markzhai on 16/2/28
 * @version 1.0.0
 */
public class ResponseException extends Exception {

    public static final int STATUS_CODE_SUCCESS = 0;

    public static final int ERROR_CODE_NEED_LOGIN = -1000;
    public static final int ERROR_CODE_NEED_PERFECT_PROFILE = -1010;
    public static final int ERROR_CODE_NEED_THIRD_PARTY_BIND = -1020;

    private final int mStatusCode;

    private ReposEntity mReposEntity;

    /**
     * @param response 全局响应格式
     */
    public ResponseException(MrResponse response) {
        super(response.getStatusMessage());
        mStatusCode = response.getStatusCode();

        if (mStatusCode == ERROR_CODE_NEED_PERFECT_PROFILE) {
            Gson gson = new Gson();
            mReposEntity = gson.fromJson(gson.toJson(response.data), ReposEntity.class);
        }
    }

    /**
     * -1	    普通异常，详见 status_msg 字段描述
     * -1000	用户登录凭证不合法，请先登录或重新登录
     * -1010	请前往完善用户信息（昵称、头像）
     * -1020	第三方账号登陆后，请前往绑定官方账号（详见文档）
     * -9990	APPID 不合法
     * -9991	APPID 对应的应用信息不存在，请联系管理员
     * -9992	APPVER 不合法
     * -9999	应用传输数据解密失败，请联系管理员
     * -999	    未知的其他异常
     * -404	    指定目标不存在或已删除
     * -2001	请输入正确的图形验证码
     *
     * @return 全局响应代码，非0（0为成功）
     */
    public int getStatusCode() {
        return mStatusCode;
    }

    public ReposEntity getReposEntity() {
        return mReposEntity;
    }
}
