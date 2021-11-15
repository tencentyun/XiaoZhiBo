package com.tencent.qcloud.tuikit.tuipusher.model;

import com.blankj.utilcode.util.SPUtils;

/**
 * TUIPusher 的公共配置参数
 */
public class TUIPusherConfig {
    private static final String TAG                 = "TUIPusherConfig";
    private static final String SP_NAME             = "tuipusher_sp";
    private static final String SP_KEY_FRONT_CAMERA = "key_front_camera";

    private TUIPusherConfig(){
    }

    private static volatile TUIPusherConfig instance;

    public static TUIPusherConfig getInstance() {
        if (instance == null) {
            synchronized (TUIPusherConfig.class) {
                if (instance == null) {
                    instance = new TUIPusherConfig();
                }
            }
        }
        return instance;
    }

    public void setFrontCamera(boolean flag){
        SPUtils.getInstance(SP_NAME).put(SP_KEY_FRONT_CAMERA, flag);
    }

    public boolean isFrontCamera(){
        return SPUtils.getInstance(SP_NAME).getBoolean(SP_KEY_FRONT_CAMERA, true);
    }

    public void destory(){
        SPUtils.getInstance(SP_NAME).put(SP_KEY_FRONT_CAMERA, true);
    }
}
