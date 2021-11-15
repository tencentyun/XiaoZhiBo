package com.tencent.qcloud.tuikit.tuiplayer.model.constant;

/**
 * IM常量定义
 */
public class IMProtocol {

    public interface SignallingCode {
        int IM_INIT_SDK_FAILED   = -1;
        int IM_LINK_STOP_SUCCESS = 3;
        int IM_LINK_STOP_FAIL    = -3;
    }


    public interface SignallingDataKey {
        String KEY_VERSION     = "version";
        String KEY_BUSINESS_ID = "businessID";
        String KEY_DATA        = "data";
        String KEY_ROOM_ID     = "streamID";
        String KEY_CMD         = "cmd";
        String KEY_CMD_INFO    = "cmdInfo";
    }


    public interface SignallingDataValue {
        int    VALUE_VERSION            = 1;
        String VALUE_PLAYER_BUSINESS_ID = "TUIPlayer";      //直播场景
        String VALUE_PUSHER_BUSINESS_ID = "TUIPusher";
        String VALUE_PLATFORM           = "Android";        //当前平台
    }


    public interface SignallingCMD {
        String CMD_JOIN_ANCHOR_REQ      = "link_req";
        String CMD_JOIN_ANCHOR_CANCEL   = "link_cancel";
        String CMD_JOIN_ANCHOR_RES      = "link_res";
        String CMD_JOIN_ANCHOR_START    = "link_start_req";
        String CMD_JOIN_ANCHOR_STOP_REQ = "link_stop_req";
        String CMD_JOIN_ANCHOR_STOP_RES = "link_stop_res";
    }
}
