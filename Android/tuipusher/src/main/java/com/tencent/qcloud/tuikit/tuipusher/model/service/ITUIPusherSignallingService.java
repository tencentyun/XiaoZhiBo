package com.tencent.qcloud.tuikit.tuipusher.model.service;

import com.tencent.qcloud.tuikit.tuipusher.model.TUIPusherCallback;
import com.tencent.qcloud.tuikit.tuipusher.model.listener.ITUIPusherSignallingListener;

/**
 * 封装 IMSDk 的接口
 */
public interface ITUIPusherSignallingService {

    /**
     * 设置事件的回调
     *
     * @param listener
     */
    void setListener(ITUIPusherSignallingListener listener);

    /**
     * 增加信令监听器
     */
    void addSignalingListener(TUIPusherCallback callback);

    /**
     * 连麦：响应请求
     */
    void responseLink(String inviteId, String streamId, boolean result, String reason, int timeout);

    /**
     * 停止连麦
     *
     * @param roomId
     * @param userId
     * @param timeout
     */
    void stopLink(String roomId, String userId, int timeout);

    /**
     * 请求 PK 邀请
     */
    String requestPK(String roomId, String userId, int timeout);

    /**
     * 取消 PK 邀请
     *
     * @param inviteId
     * @param roomId
     */
    void cancelPK(String inviteId, String roomId);

    /**
     * 响应 PK 邀请
     */
    void responsePK(String inviteId, String streamId, boolean result, String reason, int timeout);

    /**
     * 停止PK
     *
     * @param roomId  自己roomID
     * @param userId  对方的userId
     * @param timeout
     */
    void stopPK(String roomId, String userId, int timeout);

    /**
     * 销毁资源
     */
    void destory();
}
