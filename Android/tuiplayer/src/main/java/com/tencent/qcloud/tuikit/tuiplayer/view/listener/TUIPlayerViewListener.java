package com.tencent.qcloud.tuikit.tuiplayer.view.listener;

import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;

/**
 * TUIPlayerView 的事件监听
 */
public interface TUIPlayerViewListener {
    enum TUIPlayerEvent {
        TUIPLAYER_EVENT_SUCCESS(1),

        TUIPLAYER_EVENT_FAILED(-1),

        TUIPLAYER_EVENT_INVALID_LICENSE(-2),

        TUIPLAYER_EVENT_URL_NOTSUPPORT(-3),

        TUIPLAYER_EVENT_LINKMIC_START(10001), // 开始连麦

        TUIPLAYER_EVENT_LINKMIC_STOP(10002);  // 结束连麦

        int code;

        TUIPlayerEvent(int code) {
            this.code = code;
        }
    }


    interface ResponseCallback {
        void response(boolean isAgree);
    }

    /**
     * 开始拉流
     *
     * @param playView
     * @param url
     */
    void onPlayStarted(TUIPlayerView playView, String url);

    /**
     * 结束拉流
     *
     * @param playView
     * @param url
     */
    void onPlayStoped(TUIPlayerView playView, String url);

    /**
     * 事件发生时调用
     *
     * @param playView
     * @param event
     * @param message
     */
    void onPlayEvent(TUIPlayerView playView, TUIPlayerEvent event, String message);

    /**
     * 对方拒绝 PK请求的 回调
     *
     * @param playView
     * @param reason   1：对方主动拒绝 2：忙线中(pk 或者 连麦)
     */
    void onRejectJoinAnchorResponse(TUIPlayerView playView, int reason);
}
