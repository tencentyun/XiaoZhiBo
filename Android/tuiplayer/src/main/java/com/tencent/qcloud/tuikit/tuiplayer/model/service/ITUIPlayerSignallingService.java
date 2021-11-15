package com.tencent.qcloud.tuikit.tuiplayer.model.service;

import com.tencent.qcloud.tuikit.tuiplayer.model.TUIPlayerCallback;
import com.tencent.qcloud.tuikit.tuiplayer.model.listener.ITUIPlayerSignallingListener;

/**
 * 封装 IMSDk 的接口
 */
public interface ITUIPlayerSignallingService {

    /**
     * 设置事件的回调
     *
     * @param listener
     */
    void setListener(ITUIPlayerSignallingListener listener);

    /**
     * 登录IM,添加信令
     *
     */
    void login();

    /**
     * 请求 连麦 邀请
     */
    String requestLink(String roomId, String userId, int timeout, TUIPlayerCallback callback);

    /**
     * 发送取消连麦的信令
     *
     * @param inviteID
     * @param roomId
     */
    void cancelLink(String inviteID, String roomId, TUIPlayerCallback callback);

    /**
     * 请求 连麦 邀请
     */
    void startLink(String roomId, String userId, int timeout, TUIPlayerCallback callback);

    void stopLink(String roomId, String userId, int timeout, TUIPlayerCallback callback);

    /**
     * 销毁资源
     */
    void destory();
}
