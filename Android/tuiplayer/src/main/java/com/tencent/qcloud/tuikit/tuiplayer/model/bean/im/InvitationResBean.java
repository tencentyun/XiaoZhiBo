package com.tencent.qcloud.tuikit.tuiplayer.model.bean.im;

/**
 * 响应信令数据实体类
 */
public class InvitationResBean {
    private String         inviteID;
    private String         inviter;
    private SignallingData data;

    public String getInviteID() {
        return inviteID;
    }

    public void setInviteID(String inviteID) {
        this.inviteID = inviteID;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public SignallingData getData() {
        return data;
    }

    public void setData(SignallingData data) {
        this.data = data;
    }
}
