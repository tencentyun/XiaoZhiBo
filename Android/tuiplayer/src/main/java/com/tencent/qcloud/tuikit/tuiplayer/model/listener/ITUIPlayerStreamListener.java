package com.tencent.qcloud.tuikit.tuiplayer.model.listener;

import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;

/**
 * 拉流事件回调
 */
public interface ITUIPlayerStreamListener {

    /**
     * 观众端 - 连麦 推流成功回调
     */
    void onPushSuccess();

    /**
     * 流状态回调
     * @param status
     */
    void onNotifyPlayStatus(TUIPlayerView.PlayStatus status);


}
