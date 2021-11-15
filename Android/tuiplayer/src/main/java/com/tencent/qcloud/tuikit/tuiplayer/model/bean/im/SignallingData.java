package com.tencent.qcloud.tuikit.tuiplayer.model.bean.im;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tencent.liteav.basic.log.TXCLog;

import java.io.Serializable;
import java.util.Map;

import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataKey.KEY_BUSINESS_ID;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataKey.KEY_CMD;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataKey.KEY_CMD_INFO;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataKey.KEY_DATA;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataKey.KEY_ROOM_ID;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataKey.KEY_VERSION;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataValue.VALUE_PLATFORM;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataValue.VALUE_PLAYER_BUSINESS_ID;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataValue.VALUE_VERSION;

/**
 * 信令数据实体类
 */
public class SignallingData implements Serializable {
    private static final String TAG = "SignallingData";

    private int      version;
    private String   businessID;
    private String   platform;
    private String   extInfo;
    private DataInfo data;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getBusinessID() {
        return businessID;
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

    public DataInfo getData() {
        return data;
    }

    public void setData(DataInfo data) {
        this.data = data;
    }

    public static class DataInfo implements Serializable {
        private String streamID;
        private String cmd;
        private String cmdInfo;

        public DataInfo() {

        }

        public DataInfo(String cmd, String streamID, String cmdInfo) {
            this.cmd = cmd;
            this.streamID = streamID;
            this.cmdInfo = cmdInfo;
        }

        public String getStreamID() {
            return streamID;
        }

        public void setStreamID(String roomID) {
            this.streamID = roomID;
        }

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public String getCmdInfo() {
            return cmdInfo;
        }

        public void setCmdInfo(String cmdInfo) {
            this.cmdInfo = cmdInfo;
        }
    }

    /**
     * 创建信令JSON数据
     *
     * @param cmd
     * @param streamId
     * @return
     */
    public static String createSignallingJsonData(String cmd, String streamId, String reason) {
        SignallingData signallingData = new SignallingData();
        DataInfo dataInfo = new DataInfo(cmd, streamId, reason);
        signallingData.setVersion(VALUE_VERSION);
        signallingData.setBusinessID(VALUE_PLAYER_BUSINESS_ID);
        signallingData.setPlatform(VALUE_PLATFORM);
        signallingData.setExtInfo("");
        signallingData.setData(dataInfo);
        String json = new Gson().toJson(signallingData);
        TXCLog.i(TAG, "createSignallingData:" + json);
        return json;
    }

    /**
     * 从JSON数据 中解析出 信令实体
     *
     * @param json
     * @return
     */
    public static SignallingData convert2SignallingData(String json) {
        SignallingData signallingData = new SignallingData();
        Map<String, Object> extraMap;
        try {
            extraMap = new Gson().fromJson(json, Map.class);
            if (extraMap == null) {
                return signallingData;
            }
            if (extraMap.containsKey(KEY_VERSION)) {
                Object version = extraMap.get(KEY_VERSION);
                if (version instanceof Double) {
                    signallingData.setVersion(((Double) version).intValue());
                }
            }

            if (extraMap.containsKey(KEY_BUSINESS_ID)) {
                Object businessId = extraMap.get(KEY_BUSINESS_ID);
                if (businessId instanceof String) {
                    signallingData.setBusinessID((String) businessId);
                }
            }

            if (extraMap.containsKey(KEY_DATA)) {
                Object dataMapObj = extraMap.get(KEY_DATA);
                if (dataMapObj != null && dataMapObj instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) dataMapObj;
                    DataInfo dataInfo = convert2DataInfo(dataMap);
                    signallingData.setData(dataInfo);
                }
            }
        } catch (JsonSyntaxException e) {
        }
        return signallingData;
    }

    private static DataInfo convert2DataInfo(Map<String, Object> dataMap) {
        DataInfo dataInfo = new DataInfo();
        try {
            if (dataMap.containsKey(KEY_CMD)) {
                Object cmd = dataMap.get(KEY_CMD);
                if (cmd instanceof String) {
                    dataInfo.setCmd((String) cmd);
                }
            }
            if (dataMap.containsKey(KEY_ROOM_ID)) {
                Object roomId = dataMap.get(KEY_ROOM_ID);
                if (roomId instanceof String) {
                    dataInfo.setStreamID((String) roomId);
                }
            }
            if (dataMap.containsKey(KEY_CMD_INFO)) {
                Object cmdInfo = dataMap.get(KEY_CMD_INFO);
                if (cmdInfo != null && cmdInfo instanceof String) {
                    dataInfo.setCmdInfo((String) cmdInfo);
                } else {
                    dataInfo.setCmdInfo("");
                }
            }
        } catch (JsonSyntaxException e) {
        }
        return dataInfo;
    }
}
