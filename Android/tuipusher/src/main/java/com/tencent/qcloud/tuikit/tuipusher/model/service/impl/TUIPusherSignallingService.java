package com.tencent.qcloud.tuikit.tuipusher.model.service.impl;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMSignalingListener;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.tuikit.tuipusher.model.TUIPusherCallback;
import com.tencent.qcloud.tuikit.tuipusher.model.bean.im.InvitationReqBean;
import com.tencent.qcloud.tuikit.tuipusher.model.bean.im.InvitationResBean;
import com.tencent.qcloud.tuikit.tuipusher.model.bean.im.SignallingData;
import com.tencent.qcloud.tuikit.tuipusher.model.listener.ITUIPusherSignallingListener;
import com.tencent.qcloud.tuikit.tuipusher.model.service.ITUIPusherSignallingService;

import java.util.List;

import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_CANCEL;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_RES;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_START_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_START_RES;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_STOP_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_STOP_RES;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_PK_CANCEL;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_PK_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_PK_RES;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_PK_STOP_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_PK_STOP_RES;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCode.IM_LINK_STOP_FAIL;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCode.IM_LINK_STOP_SUCCESS;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCode.IM_PK_STOP_FAIL;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCode.IM_PK_STOP_SUCCESS;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingDataValue.VALUE_PLAYER_BUSINESS_ID;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingDataValue.VALUE_PUSHER_BUSINESS_ID;

public class TUIPusherSignallingService implements ITUIPusherSignallingService {
    private static final String TAG = TUIPusherSignallingService.class.getSimpleName();

    private ITUIPusherSignallingListener mListener;
    private Context                      mContext;
    private TUIPusherIMSignalingListener mIMSignalingListener;

    public TUIPusherSignallingService(Context context) {
        mContext = context;
        mIMSignalingListener = new TUIPusherIMSignalingListener();
    }

    @Override
    public void setListener(ITUIPusherSignallingListener listener) {
        mListener = listener;
    }

    @Override
    public void addSignalingListener(final TUIPusherCallback callback) {
        TXCLog.d(TAG, "addSignalingListener");
        V2TIMManager.getMessageManager();
        V2TIMManager.getSignalingManager().addSignalingListener(mIMSignalingListener);
        callback.onResult(0, "success");
    }

    @Override
    public void responseLink(String inviteId, String streamId, boolean result, String reaseon, int timeout) {
        TXCLog.d(TAG, "responseLink inviteId:" + inviteId + ", streamId:" + streamId + ", result:" + result);
        String json = SignallingData.createSignallingJsonData(CMD_JOIN_ANCHOR_RES, streamId, reaseon);
        V2TIMCallback callback = new V2TIMCallback() {
            @Override
            public void onSuccess() {
                TXCLog.d(TAG, "responseLink success");
            }

            @Override
            public void onError(int code, String desc) {
                TXCLog.d(TAG, "responseLink fail code:" + code + ", desc: " + desc);
            }
        };
        if (result) {
            V2TIMManager.getSignalingManager().accept(inviteId, json, callback);
        } else {
            V2TIMManager.getSignalingManager().reject(inviteId, json, callback);
        }
    }

    @Override
    public void stopLink(String roomId, String userId, int timeout) {
        TXCLog.d(TAG, "stopLink roomId:" + roomId + ", userId:" + userId);
        String json = SignallingData.createSignallingJsonData(CMD_JOIN_ANCHOR_STOP_REQ, roomId, "");
        String inviteID = V2TIMManager.getSignalingManager().invite(userId, json, true, null, timeout, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                mListener.onCommonResult(IM_LINK_STOP_SUCCESS, "IM STOP PK SUCCESS");
            }

            @Override
            public void onError(int code, String desc) {
                mListener.onCommonResult(IM_LINK_STOP_FAIL, "IM STOP PK FAIL");
            }
        });
        TXCLog.d(TAG, "inviteId:" + inviteID);
    }

    @Override
    public String requestPK(String roomId, String userId, int timeout) {
        TXCLog.d(TAG, "requestPK roomId:" + roomId + ", userId:" + userId);
        String json = SignallingData.createSignallingJsonData(CMD_PK_REQ, roomId + "", "");
        String inviteID = V2TIMManager.getSignalingManager().invite(userId, json, true, null, timeout, null);
        TXCLog.d(TAG, "inviteId:" + inviteID);
        return inviteID;
    }

    @Override
    public void cancelPK(String inviteID, String roomId) {
        TXCLog.d(TAG, "cancelPK inviteId:" + inviteID + ", roomId:" + roomId);
        String json = SignallingData.createSignallingJsonData(CMD_PK_CANCEL, roomId + "", "");
        V2TIMManager.getSignalingManager().cancel(inviteID, json, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                TXCLog.d(TAG, "cancelPK success");
            }

            @Override
            public void onError(int code, String desc) {
                TXCLog.d(TAG, "cancelPK error: " + ", code:" + code + ", desc:" + desc);
            }
        });
    }

    @Override
    public void responsePK(String inviteId, String streamId, boolean result, String reason, int timeout) {
        TXCLog.d(TAG, "responsePK inviteId:" + inviteId + ", streamId:" + streamId + ", result:" + result);
        String json = SignallingData.createSignallingJsonData(CMD_PK_RES, streamId, reason);
        V2TIMCallback callback = new V2TIMCallback() {
            @Override
            public void onSuccess() {
                TXCLog.d(TAG, "responsePK success");
            }

            @Override
            public void onError(int code, String desc) {
                TXCLog.d(TAG, "responsePK fail code:" + code + ", desc: " + desc);
            }
        };
        if (result) {
            V2TIMManager.getSignalingManager().accept(inviteId, json, callback);
        } else {
            V2TIMManager.getSignalingManager().reject(inviteId, json, callback);
        }

    }

    @Override
    public void stopPK(String roomId, String userId, int timeout) {
        TXCLog.d(TAG, "stopPK roomId:" + roomId + ", userId:" + userId);
        String json = SignallingData.createSignallingJsonData(CMD_PK_STOP_REQ, "", "");
        String inviteID = V2TIMManager.getSignalingManager().invite(userId, json, true, null, timeout, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                mListener.onCommonResult(IM_PK_STOP_SUCCESS, "IM STOP PK SUCCESS");
            }

            @Override
            public void onError(int code, String desc) {
                mListener.onCommonResult(IM_PK_STOP_FAIL, "IM STOP PK FAIL");
            }
        });
        TXCLog.d(TAG, "inviteId:" + inviteID);
    }

    /**
     * 信令监听器
     */
    private final class TUIPusherIMSignalingListener extends V2TIMSignalingListener {

        public void onReceiveNewInvitation(String inviteID, String inviter, String groupID, List<String> inviteeList, String data) {
            TXCLog.d(TAG, "onReceiveNewInvitation inviteID:" + inviteID + ", inviter:" + inviter + ", groupID: " + groupID + ", data: " + data);
            if (inviteeList != null && inviteeList.size() > 0) {
                TXCLog.d(TAG, "onReceiveNewInvitation inviteID:" + inviteID + ", inviteeList: " + new Gson().toJson(inviteeList));
            }

            SignallingData signallingData = SignallingData.convert2SignallingData(data);
            if (signallingData == null || signallingData.getData() == null) {
                return;
            }

            if (!VALUE_PLAYER_BUSINESS_ID.equals(signallingData.getBusinessID()) && !VALUE_PUSHER_BUSINESS_ID.equals(signallingData.getBusinessID())) {
                return;
            }

            if (CMD_PK_REQ.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationReqBean bean = new InvitationReqBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setGroupID(groupID);
                    bean.setInviteeList(inviteeList);
                    bean.setData(signallingData);
                    mListener.onRequestPK(bean);
                }
            } else if (CMD_PK_STOP_REQ.equals(signallingData.getData().getCmd())) {
                V2TIMManager.getSignalingManager().accept(inviteID, SignallingData.createSignallingJsonData(CMD_PK_STOP_RES, inviter, ""), null);
                if (mListener != null) {
                    mListener.onStopPK();
                }
            } else if (CMD_JOIN_ANCHOR_REQ.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationReqBean bean = new InvitationReqBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setGroupID(groupID);
                    bean.setInviteeList(inviteeList);
                    bean.setData(signallingData);
                    mListener.onRequestJoinAnchor(bean);
                }
            } else if (CMD_JOIN_ANCHOR_START_REQ.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationReqBean bean = new InvitationReqBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setGroupID(groupID);
                    bean.setInviteeList(inviteeList);
                    bean.setData(signallingData);
                    mListener.onStartLink(bean);
                    V2TIMManager.getSignalingManager().accept(inviteID, SignallingData.createSignallingJsonData(CMD_JOIN_ANCHOR_START_RES, inviter, ""), null);
                }
            } else if (CMD_JOIN_ANCHOR_STOP_REQ.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationReqBean bean = new InvitationReqBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setGroupID(groupID);
                    bean.setInviteeList(inviteeList);
                    bean.setData(signallingData);
                    mListener.onStopLink(bean);
                    V2TIMManager.getSignalingManager().accept(inviteID, SignallingData.createSignallingJsonData(CMD_JOIN_ANCHOR_STOP_RES, inviter, ""), null);
                }
            }
        }

        public void onInviteeAccepted(String inviteID, String inviter, String data) {
            TXCLog.d(TAG, "onInviteeAccepted inviteID:" + inviteID + ", invitee:" + inviter + ", data: " + data);
            SignallingData signallingData = SignallingData.convert2SignallingData(data);
            if (signallingData == null || signallingData.getData() == null) {
                return;
            }

            if (!VALUE_PLAYER_BUSINESS_ID.equals(signallingData.getBusinessID()) && !VALUE_PUSHER_BUSINESS_ID.equals(signallingData.getBusinessID())) {
                return;
            }
            if (CMD_PK_RES.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationResBean bean = new InvitationResBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setData(signallingData);
                    mListener.onResponsePK(bean, ITUIPusherSignallingListener.PKResponseState.ACCEPT);
                }
            }
        }

        public void onInviteeRejected(String inviteID, String inviter, String data) {
            TXCLog.d(TAG, "onInviteeRejected inviteID:" + inviteID + ", invitee:" + inviter + ", data: " + data);
            SignallingData signallingData = SignallingData.convert2SignallingData(data);
            if (signallingData == null || signallingData.getData() == null) {
                return;
            }

            if (!VALUE_PLAYER_BUSINESS_ID.equals(signallingData.getBusinessID()) && !VALUE_PUSHER_BUSINESS_ID.equals(signallingData.getBusinessID())) {
                return;
            }
            if (CMD_PK_RES.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationResBean bean = new InvitationResBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setData(signallingData);
                    mListener.onResponsePK(bean, ITUIPusherSignallingListener.PKResponseState.REJECT);
                }
            }
        }

        public void onInvitationCancelled(String inviteID, String inviter, String data) {
            TXCLog.d(TAG, "onInvitationCancelled inviteID:" + inviteID + ", inviter:" + inviter + ", data: " + data);
            SignallingData signallingData = SignallingData.convert2SignallingData(data);
            if (signallingData == null || signallingData.getData() == null) {
                return;
            }

            if (!VALUE_PLAYER_BUSINESS_ID.equals(signallingData.getBusinessID()) && !VALUE_PUSHER_BUSINESS_ID.equals(signallingData.getBusinessID())) {
                return;
            }
            if (CMD_PK_CANCEL.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationResBean bean = new InvitationResBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setData(signallingData);
                    mListener.onResponsePK(bean, ITUIPusherSignallingListener.PKResponseState.CANCEL);
                }
            } else if (CMD_JOIN_ANCHOR_CANCEL.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationResBean bean = new InvitationResBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setData(signallingData);
                    mListener.onResponseLink(bean, ITUIPusherSignallingListener.LinkResponseState.CANCEL);
                }
            }
        }

        public void onInvitationTimeout(String inviteID, List<String> inviteeList) {
            TXCLog.d(TAG, "onInvitationTimeout inviteID:" + inviteID);
            if (inviteeList != null && inviteeList.size() > 0) {
                TXCLog.d(TAG, "onInvitationTimeout inviteID:" + inviteID + ", inviteeList: " + new Gson().toJson(inviteeList));
            }
            if (mListener != null) {
                InvitationResBean bean = new InvitationResBean();
                bean.setInviteID(inviteID);
                mListener.onTimeOut(inviteID);
            }
        }

    }


    @Override
    public void destory() {
        TXCLog.d(TAG, "destory");
        V2TIMManager.getSignalingManager().removeSignalingListener(mIMSignalingListener);
    }

    public enum RejectReason {
        NORMAL("1"),        //正常拒绝PK
        BUSY("2");          //忙线中(PK或者连麦中)

        RejectReason(String reason){
            this.reason = reason;
        }

        private String reason;
        private String code;

        public String getReason() {
            return reason;
        }

        public static boolean isRejectReason(String reason){
            if(TextUtils.isEmpty(reason)){
                return false;
            }
            return NORMAL.getReason().equals(reason) || BUSY.getReason().equals(reason);
        }

        public static int getRejectReasonCode(String reason){
            if(TextUtils.isEmpty(reason)){
                return -1;
            }
            if(NORMAL.getReason().equals(reason)){
                return 1;
            }
            if(BUSY.getReason().equals(reason)){
                return 2;
            }
            return -1;
        }
    }
}
