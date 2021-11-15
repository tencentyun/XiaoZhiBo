package com.tencent.qcloud.tuikit.tuiplayer.model.bean.im;

import java.io.Serializable;
import java.util.List;

/**
 * 邀请信令数据实体类
 */
public class InvitationReqBean implements Serializable {
    private String         inviteID;
    private String         inviter;
    private String         groupID;
    private List<String>   inviteeList;
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

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public List<String> getInviteeList() {
        return inviteeList;
    }

    public void setInviteeList(List<String> inviteeList) {
        this.inviteeList = inviteeList;
    }

    public SignallingData getData() {
        return data;
    }

    public void setData(SignallingData data) {
        this.data = data;
    }
}
