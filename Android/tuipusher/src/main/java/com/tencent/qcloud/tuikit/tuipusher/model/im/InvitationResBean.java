package com.tencent.qcloud.tuikit.tuipusher.model.im;

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
