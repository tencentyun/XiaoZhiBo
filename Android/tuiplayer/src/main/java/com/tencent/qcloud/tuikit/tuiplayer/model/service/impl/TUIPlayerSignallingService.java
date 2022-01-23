package com.tencent.qcloud.tuikit.tuiplayer.model.service.impl;

import android.content.Context;

import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMGroupChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupListener;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMSignalingListener;
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener;
import com.tencent.imsdk.v2.V2TIMUserInfo;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.tuikit.tuiplayer.model.TUIPlayerCallback;
import com.tencent.qcloud.tuikit.tuiplayer.model.bean.im.InvitationReqBean;
import com.tencent.qcloud.tuikit.tuiplayer.model.bean.im.InvitationResBean;
import com.tencent.qcloud.tuikit.tuiplayer.model.bean.im.SignallingData;
import com.tencent.qcloud.tuikit.tuiplayer.model.listener.ITUIPlayerSignallingListener;
import com.tencent.qcloud.tuikit.tuiplayer.model.service.ITUIPlayerSignallingService;

import java.util.List;

import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_CANCEL;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_REQ;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_RES;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_START;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_STOP_REQ;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_STOP_RES;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingCode.IM_LINK_STOP_FAIL;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingCode.IM_LINK_STOP_SUCCESS;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataValue.VALUE_PLAYER_BUSINESS_ID;
import static com.tencent.qcloud.tuikit.tuiplayer.model.constant.IMProtocol.SignallingDataValue.VALUE_PUSHER_BUSINESS_ID;

public class TUIPlayerSignallingService implements ITUIPlayerSignallingService {
    private static final String TAG = TUIPlayerSignallingService.class.getSimpleName();

    private ITUIPlayerSignallingListener mListener;
    private Context                      mContext;
    private TUIPlayerIMSignalingListener mIMSignalingListener;

    private TUIPlayerSignallingService(Context context) {
        mContext = context;
        mIMSignalingListener = new TUIPlayerIMSignalingListener();
    }

    private static volatile TUIPlayerSignallingService instance;

    public static TUIPlayerSignallingService getInstance(Context context) {
        if (instance == null) {
            synchronized (TUIPlayerSignallingService.class) {       //保证线程安全
                if (instance == null) {
                    instance = new TUIPlayerSignallingService(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void setListener(ITUIPlayerSignallingListener listener) {
        mListener = listener;
    }


    @Override
    public void login() {
        TXCLog.d(TAG, "login");
        V2TIMManager.getMessageManager();
        V2TIMManager.getSignalingManager().addSignalingListener(mIMSignalingListener);
    }


    @Override
    public String requestLink(String roomId, String userId, int timeout, final TUIPlayerCallback callback) {
        TXCLog.d(TAG, "requestLink roomId:" + roomId + ", userId: " + userId);
        String json = SignallingData.createSignallingJsonData(CMD_JOIN_ANCHOR_REQ, roomId + "", "");
        String inviteID = V2TIMManager.getSignalingManager().invite(userId, json, true, null, timeout, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                TXCLog.d(TAG, "requestLink onSuccess");
                if (callback != null) {
                    callback.onResult(TUIPlayerCallback.SUCCESS_CODE, TUIPlayerCallback.SUCCESS_MESSAGE);
                }
            }

            @Override
            public void onError(int code, String desc) {
                TXCLog.d(TAG, "requestLink onError code:" + code + ", desc:" + desc);
                if (callback != null) {
                    callback.onResult(code, desc);
                }
            }
        });
        TXCLog.d(TAG, "inviteId:" + inviteID);
        return inviteID;
    }

    @Override
    public void cancelLink(String inviteID, String roomId, final TUIPlayerCallback callback) {
        TXCLog.d(TAG, "cancelLink inviteId:" + inviteID);
        String json = SignallingData.createSignallingJsonData(CMD_JOIN_ANCHOR_CANCEL, roomId, "");
        V2TIMManager.getSignalingManager().cancel(inviteID, json, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                TXCLog.d(TAG, "cancelLink success");
                if (callback != null) {
                    callback.onResult(TUIPlayerCallback.SUCCESS_CODE, TUIPlayerCallback.SUCCESS_MESSAGE);
                }
            }

            @Override
            public void onError(int code, String desc) {
                TXCLog.d(TAG, "cancelLink error: " + ", code:" + code + ", desc:" + desc);
                if (callback != null) {
                    callback.onResult(code, desc);
                }
            }
        });
    }

    @Override
    public void startLink(String roomId, String userId, int timeout, final TUIPlayerCallback callback) {
        TXCLog.d(TAG, "startLink roomId:" + roomId + ", userId:" + userId);
        String json = SignallingData.createSignallingJsonData(CMD_JOIN_ANCHOR_START, roomId + "", "");
        String inviteID = V2TIMManager.getSignalingManager().invite(userId, json, true, null, timeout, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                TXCLog.d(TAG, "startLink onSuccess");
                if (callback != null) {
                    callback.onResult(TUIPlayerCallback.SUCCESS_CODE, TUIPlayerCallback.SUCCESS_MESSAGE);
                }
            }

            @Override
            public void onError(int code, String desc) {
                TXCLog.d(TAG, "startLink error: " + ", code:" + code + ", desc:" + desc);
                if (callback != null) {
                    callback.onResult(code, desc);
                }
            }
        });
        TXCLog.d(TAG, "inviteId:" + inviteID + ", json:" + json);
    }

    @Override
    public void stopLink(String roomId, String userId, int timeout, TUIPlayerCallback callback) {
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

    private class TUIPlayerIMSimpleMsgListener extends V2TIMSimpleMsgListener {

        public void onRecvC2CTextMessage(String msgID, V2TIMUserInfo sender, String text) {
            TXCLog.d(TAG, "onRecvC2CTextMessage");
        }

        public void onRecvC2CCustomMessage(String msgID, V2TIMUserInfo sender, byte[] customData) {
            TXCLog.d(TAG, "onRecvC2CCustomMessage");
        }

        @Override
        public void onRecvGroupTextMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender, String text) {
            TXCLog.d(TAG, "onRecvGroupTextMessage");
        }

        @Override
        public void onRecvGroupCustomMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender, byte[] customData) {
            TXCLog.d(TAG, "onRecvGroupCustomMessage");
        }
    }


    private class TUIPlayerIMGroupListener extends V2TIMGroupListener {
        @Override
        public void onMemberEnter(String groupID, List<V2TIMGroupMemberInfo> memberList) {
            TXCLog.d(TAG, "onMemberEnter");

        }

        @Override
        public void onMemberLeave(String groupID, V2TIMGroupMemberInfo member) {
            TXCLog.i(TAG, "onMemberLeave");
        }

        @Override
        public void onGroupDismissed(String groupID, V2TIMGroupMemberInfo opUser) {
            TXCLog.i(TAG, "onGroupDismissed");

        }

        @Override
        public void onGroupInfoChanged(String groupID, List<V2TIMGroupChangeInfo> changeInfos) {
            super.onGroupInfoChanged(groupID, changeInfos);
            TXCLog.i(TAG, "onGroupInfoChanged");
        }
    }


    /**
     * 信令监听器
     */
    private final class TUIPlayerIMSignalingListener extends V2TIMSignalingListener {

        /**
         * 收到邀请
         */
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

            if (CMD_JOIN_ANCHOR_STOP_REQ.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationReqBean bean = new InvitationReqBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(inviter);
                    bean.setGroupID(groupID);
                    bean.setInviteeList(inviteeList);
                    bean.setData(signallingData);
                    mListener.onStopJoinAnchor();
                    V2TIMManager.getSignalingManager().accept(inviteID, SignallingData.createSignallingJsonData(CMD_JOIN_ANCHOR_STOP_RES, inviter, ""), null);
                }
            }
        }

        /**
         * 被邀请者接受邀请
         */
        public void onInviteeAccepted(String inviteID, String invitee, String data) {
            TXCLog.d(TAG, "onInviteeAccepted inviteID:" + inviteID + ", invitee:" + invitee + ", data: " + data);
            SignallingData signallingData = SignallingData.convert2SignallingData(data);
            if (signallingData == null || signallingData.getData() == null) {
                return;
            }

            if (!VALUE_PLAYER_BUSINESS_ID.equals(signallingData.getBusinessID()) && !VALUE_PUSHER_BUSINESS_ID.equals(signallingData.getBusinessID())) {
                return;
            }
            if (CMD_JOIN_ANCHOR_RES.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationResBean bean = new InvitationResBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(invitee);
                    bean.setData(signallingData);
                    mListener.onResponseJoinAnchor(bean, ITUIPlayerSignallingListener.LinkResponseState.ACCEPT);
                }
            }
        }

        /**
         * 被邀请者拒绝邀请
         */
        public void onInviteeRejected(String inviteID, String invitee, String data) {
            TXCLog.d(TAG, "onInviteeRejected inviteID:" + inviteID + ", invitee:" + invitee + ", data: " + data);
            SignallingData signallingData = SignallingData.convert2SignallingData(data);
            if (signallingData == null || signallingData.getData() == null) {
                return;
            }

            if (!VALUE_PLAYER_BUSINESS_ID.equals(signallingData.getBusinessID()) && !VALUE_PUSHER_BUSINESS_ID.equals(signallingData.getBusinessID())) {
                return;
            }

            if (CMD_JOIN_ANCHOR_RES.equals(signallingData.getData().getCmd())) {
                if (mListener != null) {
                    InvitationResBean bean = new InvitationResBean();
                    bean.setInviteID(inviteID);
                    bean.setInviter(invitee);
                    bean.setData(signallingData);
                    mListener.onResponseJoinAnchor(bean, ITUIPlayerSignallingListener.LinkResponseState.REJECT);
                }
            }
        }

        /**
         * 邀请被取消
         */
        public void onInvitationCancelled(String inviteID, String inviter, String data) {
            TXCLog.d(TAG, "onInvitationCancelled inviteID:" + inviteID + ", inviter:" + inviter + ", data: " + data);
        }

        /**
         * 邀请超时
         */
        public void onInvitationTimeout(String inviteID, List<String> inviteeList) {
            TXCLog.d(TAG, "onInvitationTimeout inviteID:" + inviteID);
            if (inviteeList != null && inviteeList.size() > 0) {
                TXCLog.d(TAG, "onInvitationTimeout inviteID:" + inviteID + ", inviteeList: " + new Gson().toJson(inviteeList));
            }
            if (mListener != null) {
                InvitationResBean bean = new InvitationResBean();
                bean.setInviteID(inviteID);
                mListener.onResponseJoinAnchor(bean, ITUIPlayerSignallingListener.LinkResponseState.TIMEOUT);
            }
        }

    }

    @Override
    public void destory() {
        TXCLog.d(TAG, "destory");
        V2TIMManager.getSignalingManager().removeSignalingListener(mIMSignalingListener);
    }
}
