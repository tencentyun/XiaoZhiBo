package com.tencent.liteav.debug;

/**
 * Module:   GenerateGlobalConfig
 *
 * 此文件提供有关于小直播编译过程中需要配置等所有变量，需要您根据自己的产品参数自行设置
 *
 * 更多细节见腾讯云官网文档：https://cloud.tencent.com/document/product/454/38625
 */
public class GenerateGlobalConfig {

    /**
     * 腾讯云直播license管理页面(https://console.cloud.tencent.com/live/license)
     * 当前应用的License LicenseUrl
     *
     * License Management View (https://console.cloud.tencent.com/live/license)
     * License URL of your application
     */
    public static final String LICENSEURL = "https://liteav.sdk.qcloud.com/app/res/licence/xiaozhibo/android/XiaoZhiBo_Android.license";

    /**
     * 腾讯云直播license管理页面(https://console.cloud.tencent.com/live/license)
     * 当前应用的License Key
     *
     * License Management View (https://console.cloud.tencent.com/live/license)
     * License key of your application
     */
    public static final String LICENSEURLKEY = "9bc74ac7bfd07ea392e8fdff2ba5678a";

    /**
     * 配置的后台服务域名，类似：https://service-3vscss6c-xxxxxxxxxxx.gz.apigw.tencentcs.com"
     *
     * 小直播后台提供有登录、房间列表等服务，更多细节见文档：https://cloud.tencent.com/document/product/454/38625
     */
    public static final String SERVERLESSURL = "https://demos.trtc.tencent-cloud.com/prod/";

    /**
     * 配置的播放域名
     *
     * 腾讯云域名管理页面：https://console.cloud.tencent.com/live/domainmanage
     */
    public static final String PLAY_DOMAIN = "3891.liveplay.myqcloud.com";
}
