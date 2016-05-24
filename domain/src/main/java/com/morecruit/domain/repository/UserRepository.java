package com.morecruit.domain.repository;

import com.morecruit.domain.model.user.NameCard;
import com.morecruit.domain.model.user.ProfileConfig;
import com.morecruit.domain.model.user.SettingEntity;
import com.morecruit.domain.model.user.UserInfoEntity;
import com.morecruit.domain.model.user.VerifyCode;
import com.morecruit.domain.model.user.Vuser;

import java.util.List;

import rx.Observable;

/**
 * Created by markzhai on 16/2/27
 *
 * @author markzhai
 * @version 1.0.0
 */
public interface UserRepository {

    Observable<Vuser> login(String mobile, String password);

    Observable<Vuser> register(String mobile, String password, String verifyCode);

    Observable<VerifyCode> sendVerifyCode(String mobile, String captureCode, String verifyCode, String sessionId);

    Observable<Void> resetPassword(String mobile, String password, String rePassword, String verifyCode);

    Observable<Void> perfect(String avatar, String nickname, int sex);

    Observable<Void> logout();

    Observable<UserInfoEntity> user(String userId);

    Observable<List<UserInfoEntity>> users(String tag);

    Observable<NameCard> getNameCard(String userId);

    Observable<NameCard> getNameCardLite(String userId);

    /**
     * 修改个人资料 (如果有些字段值不需要变更，不传入即可)
     */
    Observable<Void> updateProfile(String nickName, int age, int industry1, int industry2,
                                   int province, int city, String signature);

    Observable<Void> setAvatar(String avatar);

    Observable<Void> changeMobile(String mobile, String verifyCode);

    Observable<ProfileConfig> getProfileConfig();

    /**
     * 更新个人设置
     *
     * @param field 需要更新的字段（recv_msg_switch，like_switch）
     * @param value 需要更新的字段的值（值域：0,1,2)
     */
    Observable<Void> updateSetting(String field, int value);

    /**
     * 读取个人设置
     */
    Observable<SettingEntity> getSetting();


    /**
     * 更新安卓、苹果消息推送通道（每次登录时）
     *
     * @param appId         当前应用id（core.app_info 主键）
     * @param pushChannelId 每台设备的唯一通道号
     */
    Observable<Void> updatePushChannel(String pushChannelId);
}
