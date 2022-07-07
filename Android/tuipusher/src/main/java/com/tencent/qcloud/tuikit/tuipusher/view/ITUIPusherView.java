package com.tencent.qcloud.tuikit.tuipusher.view;

import com.tencent.qcloud.tuikit.tuipusher.view.listener.TUIPusherViewListener;

public interface ITUIPusherView {
    /**
     * 设置TUIPusherView 事件的回调对象
     *
     * @param listener 回调
     */
    void setTUIPusherViewListener(TUIPusherViewListener listener);

    /**
     * 开始摄像头预览
     *
     * @param url
     */
    void start(String url);

    /**
     * 停止推流
     */
    void stop();

    /**
     * set groupId for TUIPusher
     *
     * @param groupId the groupId
     */
    void setGroupId(String groupId);

    /**
     * 发送 PK 请求
     *
     * @param userId userId
     * @return
     */
    boolean sendPKRequest(String userId);

    /**
     * 取消 PK 请求
     */
    void cancelPKRequest();

    /**
     * 发送 停止PK 请求
     */

    void stopPK();

    /**
     * 发送 停止连麦 请求
     */
    void stopJoinAnchor();

    /**
     * 设置镜像模式
     *
     * @param isMirror true:开启镜像模式; false:关闭镜像模式
     */
    void setMirror(boolean isMirror);

    /**
     * 切换前后摄像头
     *
     * @param isFrontCamera true:使用前置摄像头； false:使用后置摄像头
     */
    void switchCamera(boolean isFrontCamera);

    /**
     * 设置分辨率
     *
     * @param resolution 视频分辨率
     */
    void setVideoResolution(TUIPusherView.TUIPusherVideoResolution resolution);
}
