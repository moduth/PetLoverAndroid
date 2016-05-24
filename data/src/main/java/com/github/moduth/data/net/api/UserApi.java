package com.github.moduth.data.net.api;


import com.github.moduth.data.net.MrResponse;
import com.morecruit.domain.model.user.NameCard;
import com.morecruit.domain.model.user.ProfileConfig;
import com.morecruit.domain.model.user.SettingEntity;
import com.morecruit.domain.model.user.UserInfoEntity;
import com.morecruit.domain.model.user.Vuser;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    /**
     * 注册
     * <p>
     * Note: 注册完请客户端引导用户前往完善信息（昵称、头像）
     *
     * @param mobile     手机号
     * @param password   密码
     * @param verifyCode 短信验证码
     * @return {@link Vuser}
     */
    @FormUrlEncoded
    @POST("auth/register")
    Observable<MrResponse> register(@Field("mobile") String mobile,
                                    @Field("password") String password,
                                    @Field("vcode") String verifyCode);

    /**
     * 发送验证码
     *
     * @param mobile      手机号
     * @param captureCode 图形验证码
     * @param scene       使用场景
     *                    REGISTER : 注册
     *                    RESET_PWD : 重置密码
     *                    CHANGE_MOBILE : 更换手机号
     * @return {@link com.morecruit.domain.model.user.VerifyCode}
     */
    @FormUrlEncoded
    @POST("auth/sendVcode")
    Observable<MrResponse> sendVerifyCode(@Field("mobile") String mobile,
                                          @Field("capt_code") String captureCode,
                                          @Field("scene") String scene,
                                          @Header("Cookie") String sessionId);

    /**
     * 重置密码
     * <p>
     * Note: 注册完请客户端引导用户前往完善信息（昵称、头像）
     *
     * @param mobile     手机号
     * @param password   密码
     * @param rePassword 重复密码（可选，如果UE不需要该字段，则不传该值）
     * @param verifyCode 短信验证码
     */
    @FormUrlEncoded
    @POST("auth/resetPassword")
    Observable<MrResponse> resetPassword(@Field("mobile") String mobile,
                                         @Field("password") String password,
                                         @Field("re_password") String rePassword,
                                         @Field("vcode") String verifyCode);

    /**
     * 退出登录
     */
    @POST("auth/logout")
    Observable<MrResponse> logout();

    /**
     * 补填、完善用户信息（强制）
     *
     * @param avatar   七牛返回的图片key
     * @param nickName 昵称
     * @param sex      性别 1:男 2:女
     */
    @FormUrlEncoded
    @POST("user/perfect")
    Observable<MrResponse> perfect(@Field("avatar") String avatar,
                                   @Field("nickname") String nickName,
                                   @Field("sex") int sex);

    /**
     * 更换登录手机号
     *
     * @param mobile     手机号
     * @param verifyCode 短信验证码
     */
    @FormUrlEncoded
    @POST("user/changeMobile")
    Observable<MrResponse> changeMobile(@Field("mobile") String mobile,
                                        @Field("vcode") String verifyCode);

    /**
     * 更换头像
     *
     * @param avatar 七牛返回的图片key
     */
    @FormUrlEncoded
    @POST("user/changeAvatar")
    Observable<MrResponse> changeAvatar(@Field("avatar") String avatar);

    /**
     * 读取资料配置选项
     *
     * @return {@link ProfileConfig}
     */
    @POST("user/getProfileConfig")
    Observable<MrResponse> getProfileConfig();

    /**
     * 修改个人资料 (如果有些字段值不需要变更，不传入即可)
     *
     * @param nickName  昵称
     * @param age       年龄
     * @param industry1 行业大分类
     * @param industry2 行业小分类
     * @param province  省份
     * @param city      城市
     * @param signature 签名档
     */
    @FormUrlEncoded
    @POST("user/updateProfile")
    Observable<MrResponse> updateProfile(@Field("nickname") String nickName,
                                         @Field("age") int age,
                                         @Field("industry_1") int industry1,
                                         @Field("industry_2") int industry2,
                                         @Field("province") int province,
                                         @Field("city") int city,
                                         @Field("signature") String signature);

    /**
     * 获取指定用户名片信息 (如果不指定uid，则缺省获取当前登录用户的信息)
     *
     * @param userId 用户uid
     * @return {@link NameCard}
     */
    @FormUrlEncoded
    @POST("user/nameCard")
    Observable<MrResponse> getNameCard(@Field("uid") String userId);

    /**
     * 获取指定用户名片信息（简略）(如果不指定uid，则缺省获取当前登录用户的信息)
     *
     * @param userId 用户uid
     * @return {@link NameCard} (只带uid, nickname, headimgurl)
     */
    @FormUrlEncoded
    @POST("user/nameCardLite")
    Observable<MrResponse> getNameCardLite(@Field("uid") String userId);

    /**
     * 更新个人设置
     *
     * @param field 需要更新的字段（recv_msg_switch，like_switch）
     * @param value 需要更新的字段的值（值域：0,1,2)
     */
    @FormUrlEncoded
    @POST("user/updateSettings")
    Observable<MrResponse> updateSetting(@Field("field") String field,
                                         @Field("value") int value);

    /**
     * 读取个人设置
     *
     * @return {@link SettingEntity}
     */
    @POST("user/getSettings")
    Observable<MrResponse> getSetting();

    /**
     * 更新安卓、苹果消息推送通道（每次登录时）
     *
     * @param pushChannelId 每台设备的唯一通道号
     */
    @FormUrlEncoded
    @POST("user/updatePushChannel")
    Observable<MrResponse> updatePushChannel(@Field("push_channel_id") String pushChannelId);

    // ============== dummy ==============

    @GET("users.json")
    Observable<List<UserInfoEntity>> userEntityList();

    @GET("users_{id}.json")
    Observable<UserInfoEntity> userEntityById(@Path("id") String userId);
}
