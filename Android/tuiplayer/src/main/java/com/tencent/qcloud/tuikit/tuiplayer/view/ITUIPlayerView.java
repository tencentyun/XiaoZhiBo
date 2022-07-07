package com.tencent.qcloud.tuikit.tuiplayer.view;

import com.tencent.qcloud.tuikit.tuiplayer.view.listener.TUIPlayerViewListener;

public interface ITUIPlayerView {
    public enum TUIPlayerUIState {
        TUIPLAYER_UISTATE_DEFAULT,        //默认，展示全部视图
        TUIPLAYER_UISTATE_VIDEOONLY,   //只展示视频播放View
    }

    /**
     * 设置拉流状态监听
     *
     * @param mListener listener
     */
    void setTUIPlayerViewListener(TUIPlayerViewListener mListener);

    /**
     * 更新PlayerView UI显示状态
     *
     * @param state TUIPlayerView UI显示状态
     */
    void updatePlayerUIState(TUIPlayerView.TUIPlayerUIState state);

    /**
     * 开始拉流
     *
     * @param url 流地址
     */
    int startPlay(String url);

    /**
     * 停止拉流
     */
    void stopPlay();

    /**
     * 暂停视频流
     */
    void pauseVideo();

    /**
     * 恢复视频流
     */
    void resumeVideo();

    /**
     * 暂停音频流
     */
    void pauseAudio();

    /**
     * 恢复音频流
     */
    void resumeAudio();

    /**
     * 加载挂件时的groupId
     *
     * @param groupId groupId
     */
    void setGroupId(String groupId);

    /**
     * 禁用连麦功能
     */
    void disableLinkMic();
}
