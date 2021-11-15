package com.tencent.qcloud.tuikit.tuiplayer.model.listener;

import com.tencent.qcloud.tuikit.tuiplayer.model.bean.im.InvitationResBean;

/**
 * IM信令事件回调
 */
public interface ITUIPlayerSignallingListener {

    /**
     * 统一错误码的回调
     *
     * @param code
     * @param message
     */
    void onCommonResult(int code, String message);

    /**
     * 收到响应连麦请求的回调
     * <p>
     * 处理流程：
     * 1、停止webrtc拉流
     * 2、开启RTC拉流
     * 3、推流
     *
     * @param bean
     */
    void onResponseJoinAnchor(InvitationResBean bean, LinkResponseState state);

    /**
     * 停止加入主播的回调
     */
    void onStopJoinAnchor();

    enum LinkResponseState {
        ACCEPT,
        REJECT,
        CANCEL,
        TIMEOUT
    }
}
