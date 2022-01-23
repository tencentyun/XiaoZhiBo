package com.tencent.qcloud.tuikit.tuiplayer.model.service;

import com.tencent.qcloud.tuikit.tuiplayer.model.listener.ITUIPlayerStreamListener;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * 封装 V2SDK 相关接口
 */
public interface ITUIPlayerStreamService {

    /**
     * 设置事件的回调
     *
     * @param listener
     */
    void setListener(ITUIPlayerStreamListener listener);

    /**
     * 开始拉流
     *
     * @param url
     * @param videoView
     * @return
     */
    int startPlay(String url, TXCloudVideoView videoView);

    /**
     * 停止拉流
     *
     * @return
     */
    int stopPlay();

    /**
     * 开始推流
     *
     * @param url
     * @param isFront
     * @param videoView
     * @return
     */
    int startPush(String url, boolean isFront, TXCloudVideoView videoView);

    /**
     * 停止推流
     *
     * @return
     */
    int stopPush();

    /**
     * 销毁资源
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
