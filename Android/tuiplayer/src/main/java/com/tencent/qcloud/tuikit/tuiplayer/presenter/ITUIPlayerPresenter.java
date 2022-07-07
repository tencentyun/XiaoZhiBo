package com.tencent.qcloud.tuikit.tuiplayer.presenter;

import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * 封装UI调用的接口 以及 通知UI更新的接口
 */
public interface ITUIPlayerPresenter {

    /**
     * 开启本地视频的预览画面
     *
     * @param url
     * @param view 承载视频画面的控件
     */
    int startPlay(String url, TXCloudVideoView view);

    /**
     * 停止播放
     */
    void stopPlay();

    /**
     * 发送 连麦 请求
     *
     * @param userId
     */
    void requestLink(String userId);

    /**
     * 取消 连麦 请求
     */
    void cancelLink();

    /**
     * 开始推流
     *
     * @param isFront
     * @param txCloudVideoView
     */
    void startPush(boolean isFront, TXCloudVideoView txCloudVideoView);

    /**
     * 停止连麦
     */
    void stopLink(int timeout);

    /**
     * 销毁所有资源
     */
    void destory();

    /**
     * 恢复音频流
     */
    void resumeAudio();

    /**
     * 恢复视频流
     */
    void resumeVideo();

    /**
     * 暂停音频流
     */
    void pauseAudio();

    /**
     * 暂停视频流
     */
    void pauseVideo();
}
