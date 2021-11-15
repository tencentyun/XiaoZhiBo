package com.tencent.qcloud.tuikit.tuipusher.model.constant;

/**
 * IM常量定义
 */
public class IMProtocol {

    /**
     * 信令字段 常量
     */
    public interface SignallingDataKey {
        String KEY_VERSION     = "version";
        String KEY_BUSINESS_ID = "businessID";
        String KEY_DATA        = "data";
        String KEY_ROOM_ID     = "streamID";
        String KEY_CMD         = "cmd";
        String KEY_CMD_INFO    = "cmdInfo";
    }


    /**
     * 信令字段 默认值
     */
    public interface SignallingDataValue {
        int    VALUE_VERSION            = 1;
        String VALUE_PUSHER_BUSINESS_ID = "TUIPusher";      //直播场景
        String VALUE_PLAYER_BUSINESS_ID = "TUIPlayer";      //直播场景
        String VALUE_PLATFORM           = "Android";        //当前平台
    }


    /**
     * 信令CMD 常量
     */
    public interface SignallingCMD {
        String CMD_PK_REQ                = "pk_req";                        //pk请求
        String CMD_PK_RES                = "pk_res";                        //pk响应
        String CMD_PK_CANCEL             = "pk_cancel";                     //pk取消
        String CMD_PK_STOP_REQ           = "pk_stop_req";                   //停止PK
        String CMD_PK_STOP_RES           = "pk_stop_res";                   //停止PK响应
        String CMD_JOIN_ANCHOR_REQ       = "link_req";                      //连麦请求
        String CMD_JOIN_ANCHOR_RES       = "link_res";                      //连麦响应
        String CMD_JOIN_ANCHOR_START_REQ = "link_start_req";                //连麦开始 请求
        String CMD_JOIN_ANCHOR_START_RES = "link_start_res";                //连麦开始 响应
        String CMD_JOIN_ANCHOR_STOP_REQ  = "link_stop_req";                 //连麦停止 请求
        String CMD_JOIN_ANCHOR_STOP_RES  = "link_stop_res";                 //连麦停止 响应
        String CMD_JOIN_ANCHOR_CANCEL    = "link_cancel";                   //连麦取消 取消
    }


    /**
     * 信令回调状态码
     */
    public interface SignallingCode {
        int IM_INIT_SDK_FAILED   = -1;
        int IM_PK_STOP_SUCCESS   = 2;
        int IM_PK_STOP_FAIL      = -2;
        int IM_LINK_STOP_SUCCESS = 3;
        int IM_LINK_STOP_FAIL    = -3;
    }
}
