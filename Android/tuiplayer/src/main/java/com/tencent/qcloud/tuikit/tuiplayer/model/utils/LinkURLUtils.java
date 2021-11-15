package com.tencent.qcloud.tuikit.tuiplayer.model.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.tencent.qcloud.tuicore.TUILogin;

/**
 * 连麦过程中 根据 streamId 生成 推流地址和拉流地址
 * 详情请参考：「https://cloud.tencent.com/document/product/454/7915」
 * <p>
 * <p>
 * Generating Streaming URLs
 * See [https://cloud.tencent.com/document/product/454/7915].
 */
public class LinkURLUtils {

    public static final String TRTC        = "trtc://";
    public static final String TRTC_DOMAIN = "cloud.tencent.com";

    /**
     * 根据 streamId 生成推流地址
     * Generating Publishing URLs
     *
     * @param streamId
     * @return
     */
    public static String generatePushUrl(String streamId) {
        return TRTC + TRTC_DOMAIN + "/push/" + streamId + "?sdkappid=" + TUILogin.getSdkAppId() + "&userid=" + TUILogin.getUserId() + "&usersig=" + TUILogin.getUserSig();
    }

    /**
     * 根据 streamId 生成拉流地址
     * Generating Playback URLs
     *
     * @param streamId
     * @return
     */
    public static String generatePlayUrl(String streamId) {

        return TRTC + TRTC_DOMAIN + "/play/" + streamId + "?sdkappid=" + TUILogin.getSdkAppId() + "&userid=" + TUILogin.getUserId() + "&usersig=" + TUILogin.getUserSig();
    }

    /**
     * 获取URL中的 streamId 字段
     *
     * @param url
     * @return
     */
    public static String getStreamIdByPushUrl(String url) {
        Uri uri = Uri.parse(url);
        if (uri == null) {
            return "";
        }
        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        if (!path.contains("/")) {
            return "";
        }
        String[] pathArray = path.split("/");
        if (pathArray != null && pathArray.length > 0) {
            return pathArray[pathArray.length - 1];
        }
        return "";
    }

    public static boolean checkPlayURL(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return true;
    }
}
