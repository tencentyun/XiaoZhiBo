package com.tencent.qcloud.tuikit.tuipusher.model.utils;

import com.tencent.qcloud.tuicore.TUILogin;

/**
 * 收到连麦请求时，根据请求者的streamId 生成拉流地址
 * 详情请参考：「https://cloud.tencent.com/document/product/454/7915」
 * <p>
 * <p>
 * Generating Streaming URLs
 * See [https://cloud.tencent.com/document/product/454/7915].
 */
public class LinkURLUtils {
    public static final String RTMP        = "rtmp://";
    public static final String TRTC        = "trtc://";
    public static final String TRTC_DOMAIN = "cloud.tencent.com";

    /**
     * 根据请求者的streamId 生成拉流地址
     * Generating Playback URLs
     *
     * @param streamId
     * @return
     */
    public static String generatePlayUrl(String streamId) {
        return TRTC + TRTC_DOMAIN + "/play/" + streamId + "?sdkappid=" + TUILogin.getSdkAppId() + "&userid=" + TUILogin.getUserId() + "&usersig=" + TUILogin.getUserSig();
    }

    public enum PushType {
        RTC,
        RTMP
    }
}
