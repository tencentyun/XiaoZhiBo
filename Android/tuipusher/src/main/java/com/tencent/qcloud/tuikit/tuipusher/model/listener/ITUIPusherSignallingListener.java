package com.tencent.qcloud.tuikit.tuipusher.model.listener;

import com.tencent.qcloud.tuikit.tuipusher.model.bean.im.InvitationReqBean;
import com.tencent.qcloud.tuikit.tuipusher.model.bean.im.InvitationResBean;

public interface ITUIPusherSignallingListener {

    /**
     * 统一错误码的回调
     *
     * @param code
     * @param message
     */
    void onCommonResult(int code, String message);

    /**
     * 收到请求跨房 PK 通知
     *
     * @param bean
     */
    void onRequestPK(InvitationReqBean bean);

    /**
     * 收到响应 请求跨房PK 通知
     *
     * @param bean
     * @param state
     */
    void onResponsePK(InvitationResBean bean, PKResponseState state);

    /**
     * 收到停止PK的请求
     */
    void onStopPK();

    /**
     * 收到连麦请求回调
     */
    void onRequestJoinAnchor(InvitationReqBean bean);

    /**
     * 响应连麦请求
     *
     * @param bean
     * @param state
     */
    void onResponseLink(InvitationResBean bean, LinkResponseState state);

    /**
     * 收到开始连麦的回调
     */
    void onStartLink(InvitationReqBean bean);

    /**
     * 收到停止连麦的回调
     *
     * @param bean
     */
    void onStopLink(InvitationReqBean bean);

    /**
     * 信令超时未处理响应
     *
     * @param inviteId
     */
    void onTimeOut(String inviteId);

    enum PKResponseState {
        ACCEPT,
        REJECT,
        CANCEL
    }


    enum LinkResponseState {
        ACCEPT,
        REJECT,
        CANCEL
    }
}
