package com.tencent.qcloud.tuikit.tuipusher.view.listener;

import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;

/**
 * TUIPusherView 的事件监听
 */
public interface TUIPusherViewListener {
    enum TUIPusherEvent {
        TUIPUSHER_EVENT_SUCCESS(1),

        TUIPUSHER_EVENT_FAILED(-1),

        TUIPUSHER_EVENT_INVALID_LICENSE(-2),

        TUIPUSHER_EVENT_URL_NOTSUPPORT(-3),   // 不支持当前URL

        TUIPUSHER_EVENT_NOT_LOGIN(-4);       // 用户状态没有登录

        int code;

        TUIPusherEvent(int code) {
            this.code = code;
        }
    }


    interface ResponseCallback {
        void response(boolean isAgree);
    }

    void onPushStarted(TUIPusherView pushView, String url);

    void onPushStoped(TUIPusherView pushView, String url);

    void onPushEvent(TUIPusherView pusherView, TUIPusherEvent event, String message);

    void onClickStartPushButton(TUIPusherView pushView, String url, ResponseCallback callback);

    /**
     * 收到 PK请求的 回调
     *
     * @param pushView
     * @param userId   请求者的userId
     * @param callback 返回处理结果{@link ResponseCallback#response(boolean)}
     */
    void onReceivePKRequest(TUIPusherView pushView, String userId, ResponseCallback callback);

    /**
     * 对方拒绝 PK请求的 回调
     *
     * @param pusherView
     * @param reason  1：对方主动拒绝 2：忙线中(pk 或者 连麦)
     */
    void onRejectPKResponse(TUIPusherView pusherView, int reason);

    /**
     * 取消PK请求回调
     *
     * @param pusherView
     */
    void onCancelPKRequest(TUIPusherView pusherView);

    /**
     * 开始PK成功回调
     *
     * @param pusherView
     */
    void onStartPK(TUIPusherView pusherView);

    /**
     * 停止PK回调
     *
     * @param pusherView
     */
    void onStopPK(TUIPusherView pusherView);

    /**
     * PK请求超时回调
     *
     * @param pusherView
     */
    void onPKTimeout(TUIPusherView pusherView);

    /**
     * 收到 连麦请求 的回调
     *
     * @param pushView
     * @param userId
     * @param callback
     */
    void onReceiveJoinAnchorRequest(TUIPusherView pushView, String userId, ResponseCallback callback);

    /**
     * 取消连麦请求的回调
     *
     * @param pusherView
     */
    void onCancelJoinAnchorRequest(TUIPusherView pusherView);

    /**
     * 连麦成功的回调
     *
     * @param pusherView
     */
    void onStartJoinAnchor(TUIPusherView pusherView);

    /**
     * 停止连麦的回调
     *
     * @param pusherView
     */
    void onStopJoinAnchor(TUIPusherView pusherView);

    /**
     * 连麦请求超时回调
     *
     * @param pusherView
     */
    void onJoinAnchorTimeout(TUIPusherView pusherView);
}
