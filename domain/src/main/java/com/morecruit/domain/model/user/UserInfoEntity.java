package com.morecruit.domain.model.user;

import com.google.gson.annotations.SerializedName;
import com.morecruit.domain.model.feed.Tag;

import java.util.List;

/**
 * 用户基本信息
 *
 * @author markzhai
 * @version 1.0.0
 */
public class UserInfoEntity {

    @SerializedName("uid")
    private String userId;

    @SerializedName("nickname")
    private String nickName;

    @SerializedName("sex")
    private int sex;

    @SerializedName("province")
    private String province;

    @SerializedName("city")
    private String city;

    @SerializedName("country")
    private String country;

    @SerializedName("headimgurl")
    private String headerImageUrl;

    @SerializedName("vip_lv")
    private int vipLevel;

    @SerializedName("age")
    private int age;

    @SerializedName("industry_1")
    private String industry1;

    @SerializedName("industry_2")
    private String industry2;

    @SerializedName("signature")
    private String signature;

    @SerializedName("liked_count")
    private int likedCount;

    @SerializedName("is_online")
    private int isOnline;

    @SerializedName("latest_tags")
    private List<Tag> latestTags;

    @SerializedName("is_liked")
    private int isLiked;

    @SerializedName("is_self")
    private int isSelf;

    public UserInfoEntity() {
        //empty
    }

    public UserInfoEntity(String id, String nickName, String headerImageUrl) {
        this.userId = id;
        this.nickName = nickName;
        this.headerImageUrl = headerImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHeaderImageUrl() {
        return headerImageUrl;
    }

    public void setHeaderImageUrl(String headerImageUrl) {
        this.headerImageUrl = headerImageUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIndustry1() {
        return industry1;
    }

    public void setIndustry1(String industry1) {
        this.industry1 = industry1;
    }

    public String getIndustry2() {
        return industry2;
    }

    public void setIndustry2(String industry2) {
        this.industry2 = industry2;
    }

    /**
     * @return 用户最近访问的标签
     */
    public List<Tag> getLatestTags() {
        return latestTags;
    }

    public void setLatestTags(List<Tag> latestTags) {
        this.latestTags = latestTags;
    }

    /**
     * @return 收到的赞
     */
    public int getLikedCount() {
        return likedCount;
    }

    public void setLikedCount(int likedCount) {
        this.likedCount = likedCount;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public int getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(int isLiked) {
        this.isLiked = isLiked;
    }

    /**
     * <ul>
     * <li>为0时，表示这是别人的名片卡，tag_count是共同的标签个数。tag_list是共同的标签列表。</li>
     * <li>为1时，表示这是自己的名片卡，tag_count是自己的标签个数。tag_list是自己的标签列表。</li>
     * </ul>
     */
    public int getIsSelf() {
        return isSelf;
    }

    public void setIsSelf(int isSelf) {
        this.isSelf = isSelf;
    }

    @Override
    public String toString() {
        return "UserInfoEntity{" +
                "userId=" + userId +
                ", nickName='" + nickName + '\'' +
                ", sex=" + sex +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", headerImageUrl='" + headerImageUrl + '\'' +
                ", age=" + age +
                ", industry1='" + industry1 + '\'' +
                ", industry2='" + industry2 + '\'' +
                ", signature='" + signature + '\'' +
                ", vipLevel=" + vipLevel +
                ", likedCount=" + likedCount +
                ", isOnline=" + isOnline +
                ", latestTags=" + latestTags +
                '}';
    }
}
