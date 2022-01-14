package com.tencent.qcloud.tuikit.tuipusher.model.service;

import android.graphics.Bitmap;

import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.qcloud.tuikit.tuipusher.model.listener.ITUIPusherStreamListener;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * 封装 V2SDK 相关接口
 */
public interface ITUIPusherStreamService {

    /**
     * 设置事件的回调
     *
     * @param listener
     */
    void setListener(ITUIPusherStreamListener listener);

    /**
     * 开启摄像头预览
     *
     * @param isFront
     * @param videoView
     * @return
     */
    int startCameraPreview(boolean isFront, TXCloudVideoView videoView);

    /**
     * 开启图片推流
     *
     * @param bitmap 推流图片
     * @return
     */
    void startVirtualCamera(Bitmap bitmap);

    /**
     * 关闭图片推流
     */
    void stopVirtualCamera();

    /**
     * 开始推流
     *
     * @param url
     * @return
     */
    int startPush(String url);

    /**
     * 开始拉流
     *
     * @param pkVideoView 视频View
     * @param roomId      对方的streamId
     * @param userId      自己的userId
     * @return
     */
    int startPlay(TXCloudVideoView pkVideoView, String roomId, String userId);

    /**
     * 停止拉流
     *
     * @return
     */
    int stopPlay();

    /**
     * 混流 自己的视频流 和 PK主播的视频流
     *
     * @param myUserId
     * @param myRoomId
     * @param pkUserId
     * @param pkRoomId
     * @return
     */
    int setPKMixTranscodingConfig(String myUserId, String myRoomId, String pkUserId, String pkRoomId);

    /**
     * 混流 自己的视频流 和 连麦观众的视频流
     *
     * @param myUserId
     * @param myRoomId
     * @param pkUserId
     * @param pkRoomId
     * @return
     */
    int setLinkMixTranscodingConfig(String myUserId, String myRoomId, String pkUserId, String pkRoomId);

    /**
     * 停止推流
     *
     * @return
     */
    int stopPush();

    /**
     * 切换摄像头
     *
     * @param frontCamera
     */
    void switchCamera(boolean frontCamera);

    /**
     * 设置镜像
     *
     * @param isMirror
     */
    void setMirror(boolean isMirror);

    /**
     * 设置视频参数
     *
     * @param param
     */
    void setVideoQuality(V2TXLiveDef.V2TXLiveVideoEncoderParam param);

    /**
     * 获取美颜管理类
     *
     * @return
     */
    TXBeautyManager getTXBeautyManager();

    /**
     * 获取音效管理类
     *
     * @return
     */
    TXAudioEffectManager getTXAudioEffectManager();

    /**
     * 销毁资源
     */
    void destory();
}
