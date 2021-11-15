package com.tencent.qcloud.tuikit.tuigift.view;

import com.tencent.qcloud.tuikit.tuigift.model.TUIGiftModel;

/**
 * 礼物发送结果监听
 */
public interface TUIGiftListener {
    void onSuccess(int code, String msg, TUIGiftModel giftModel);

    void onFailed(int code, String msg);
}