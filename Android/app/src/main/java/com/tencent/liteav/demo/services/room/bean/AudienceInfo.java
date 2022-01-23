package com.tencent.liteav.demo.services.room.bean;

import java.util.Objects;

/**
 * 在线观众信息
 */
public class AudienceInfo {
    private String memberId;
    private String name;    // 昵称
    private String avatar;  // 头像

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "AudienceInfo{"
                + "memberId='" + memberId + '\''
                + ", name='" + name + '\''
                + ", avatar='" + avatar + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudienceInfo that = (AudienceInfo) o;
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}
