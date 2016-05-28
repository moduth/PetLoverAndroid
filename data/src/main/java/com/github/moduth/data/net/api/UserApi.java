package com.github.moduth.data.net.api;


import com.github.moduth.domain.model.MrResponse;
import com.github.moduth.domain.model.user.Vuser;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * 用户系统
 *
 * @author markzhai on 16/3/4
 * @version 1.0.0
 */
public interface UserApi {

    /**
     * 登录
     * <p>
     * 这里有个约定，登录后当响应的 status_no 为 -1010 时，需客户端引导用户前往完善信息（昵称、头像）
     *
     * @param mobile   手机号
     * @param password 密码
     * @return {@link Vuser}
     */
    @FormUrlEncoded
    @POST("auth/login")
    Observable<MrResponse> login(@Field("mobile") String mobile,
                                 @Field("password") String password);
}
