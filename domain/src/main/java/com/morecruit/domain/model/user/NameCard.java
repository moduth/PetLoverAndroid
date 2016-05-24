package com.morecruit.domain.model.user;

import com.google.gson.annotations.SerializedName;
import com.morecruit.domain.model.MrResponse;
import com.morecruit.domain.model.feed.Tag;

import java.util.List;

/**
 * @author markzhai on 16/3/9
 * @version 1.0.0
 */
public class NameCard extends MrResponse{

    @SerializedName("name_card")
    private UserInfoEntity nameCard;

    @SerializedName("tag_count")
    private int tagCount;

    @SerializedName("tag_list")
    private List<Tag> tagList;

    public void setNameCard(UserInfoEntity nameCard) {
        this.nameCard = nameCard;
    }

    public void setTagCount(int tagCount) {
        this.tagCount = tagCount;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

    public UserInfoEntity getNameCard() {
        return nameCard;
    }

    public int getTagCount() {
        return tagCount;
    }

    public List<Tag> getTagList() {
        return tagList;
    }
}
