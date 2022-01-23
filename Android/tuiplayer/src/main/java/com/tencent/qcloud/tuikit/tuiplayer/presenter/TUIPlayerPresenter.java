package com.tencent.qcloud.tuikit.tuiplayer.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuikit.tuiplayer.R;
import com.tencent.qcloud.tuikit.tuiplayer.model.TUIPlayerCallback;
import com.tencent.qcloud.tuikit.tuiplayer.model.bean.im.InvitationReqBean;
import com.tencent.qcloud.tuikit.tuiplayer.model.bean.im.InvitationResBean;
import com.tencent.qcloud.tuikit.tuiplayer.model.listener.ITUIPlayerSignallingListener;
import com.tencent.qcloud.tuikit.tuiplayer.model.listener.ITUIPlayerStreamListener;
import com.tencent.qcloud.tuikit.tuiplayer.model.service.ITUIPlayerSignallingService;
import com.tencent.qcloud.tuikit.tuiplayer.model.service.ITUIPlayerStreamService;
import com.tencent.qcloud.tuikit.tuiplayer.model.service.impl.TUIPlayerSignallingService;
import com.tencent.qcloud.tuikit.tuiplayer.model.service.impl.TUIPlayerStreamService;
import com.tencent.qcloud.tuikit.tuiplayer.model.utils.LinkURLUtils;
import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class TUIPlayerPresenter implements ITUIPlayerContract.ITUIPlayerPresenter, ITUIPlayerStreamListener, ITUIPlayerSignallingListener {
    private static final String TAG = "TUIPlayerPresent";

    private ITUIPlayerSignallingService       mSignallingService;
    private ITUIPlayerStreamService           mStreamService;
    private ITUIPlayerContract.ITUIPlayerView mTUIPlayerView;
    private Context                           mContext;
    private String                            mStreamId;
    private String                            mLinkUserId;
    private String                            mLinkStreamId;
    private InvitationReqBean                 mInvitationReqBean;
    private InvitationResBean                 mInvitationResBean;
    private String                            mLinkInviteId;
    private String                            mPlayUrl;

    public TUIPlayerPresenter(ITUIPlayerContract.ITUIPlayerView playerView, Context context) {
        mContext = context;
        mStreamId = TUILogin.getUserId();
        mSignallingService = TUIPlayerSignallingService.getInstance(mContext);
        mSignallingService.setListener(this);
        mStreamService = TUIPlayerStreamService.getInstance(mContext);
        mStreamService.setListener(this);
        mTUIPlayerView = playerView;
    }

    @Override
    public int startPlay(String url, TXCloudVideoView view) {
        TXCLog.d(TAG, "startPlay url:" + url);
        mPlayUrl = url;
        int ret = mStreamService.startPlay(mPlayUrl, view);
        mSignallingService.login();
        return ret;
    }

    @Override
    public void stopPlay() {

    }

    @Override
    public void requestLink(String userId) {
        TXCLog.d(TAG, "requestLink userId:" + userId);
        mLinkStreamId = userId;
        mLinkUserId = userId;
        mLinkInviteId = mSignallingService.requestLink(userId, userId, 15, new TUIPlayerCallback() {
            @Override
            public void onResult(int code, String message) {
                TXCLog.d(TAG, "requestLink onResult code:" + code + ",message:" + message);
                if (code == 0) {
                    if (mTUIPlayerView != null) {
                        mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_REQ_SEND_SUCCESS, "");
                    }
                } else {
                    if (mTUIPlayerView != null) {
                        mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_REQ_SEND_FAIL, "");
                    }
                    resetLinkData();
                }
            }
        });
    }

    @Override
    public void cancelLink() {
        TXCLog.d(TAG, "cancelLink");
        mSignallingService.cancelLink(mLinkInviteId, mStreamId, new TUIPlayerCallback() {
            @Override
            public void onResult(int code, String message) {
                TXCLog.d(TAG, "cancelLink onResult code:" + code + ",message:" + message);
                if (code == 0) {
                    if (mTUIPlayerView != null) {
                        mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_CANCEL_SUCCESS, "");
                    }
                } else {
                    if (mTUIPlayerView != null) {
                        mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_CANCEL_FAIL, "");
                    }
                    resetLinkData();
                }
            }
        });
    }

    @Override
    public void startPush(boolean isFront, TXCloudVideoView txCloudVideoView) {
        String pushUrl = LinkURLUtils.generatePushUrl(mStreamId);
        mStreamService.startPush(pushUrl, isFront, txCloudVideoView);
        TXCLog.d(TAG, "startPush pushUrl:" + pushUrl);
    }

    @Override
    public void stopLink(int timeout) {
        TXCLog.d(TAG, "stopLink");
        mSignallingService.stopLink(mStreamId, mLinkUserId, timeout, new TUIPlayerCallback() {
            @Override
            public void onResult(int code, String message) {
                if (code == 0) {
                    ToastUtils.showShort(R.string.tuiplayer_stop_link_cmd_send_success);
                } else {
                    ToastUtils.showShort(mContext.getResources().getString(R.string.tuiplayer_stop_link_cmd_send_fail) + code);
                }
            }
        });

        mStreamService.stopPush();
        if (mTUIPlayerView != null) {
            mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_STOP, "");
        }
        resetLinkData();
    }

    @Override
    public void destory() {
        TXCLog.d(TAG, "destory");
        mStreamService.destory();
        mSignallingService.destory();
    }

    @Override
    public void onCommonResult(int code, String message) {

    }

    @Override
    public void onResponseJoinAnchor(InvitationResBean bean, LinkResponseState state) {
        TXCLog.d(TAG, "onResponseJoinAnchor state:" + state);
        switch (state) {
            case ACCEPT:
                mStreamService.stopPlay();
                mTUIPlayerView.onResponseJoinAnchor(bean.getData().getData().getStreamID());
                break;

            case REJECT:
                mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_REJECT, bean.getData().getData().getCmdInfo());
                resetLinkData();
                break;

            case TIMEOUT:
                mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_TIMEOUT, "");
                resetLinkData();
                break;
        }
    }

    @Override
    public void onStopJoinAnchor() {
        TXCLog.d(TAG, "onStopJoinAnchor");
        resetLinkData();
        mStreamService.stopPush();
        if (mTUIPlayerView != null) {
            mTUIPlayerView.onToastMessage(mContext.getString(R.string.tuiplayer_stop_link));
            mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_STOP, "");
        }
    }

    @Override
    public void onPushSuccess() {
        TXCLog.d(TAG, "onPushSuccess");
        if (mTUIPlayerView != null) {
            mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_PUSH_SEND, "");
        }
        mSignallingService.startLink(mStreamId, mLinkUserId, 5, new TUIPlayerCallback() {
            @Override
            public void onResult(int code, String message) {
                if (code == 0) {
                    mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_PUSH_SEND_SUCCESS, "");
                } else {
                    mTUIPlayerView.onNotifyLinkState(TUIPlayerView.LinkState.LINK_PUSH_SEND_FAIL, "");
                }
            }
        });
    }

    @Override
    public void onNotifyPlayStatus(TUIPlayerView.PlayStatus status) {
        TXCLog.d(TAG, "onNotifyPlayStatus status:" + status);
        if (mTUIPlayerView != null && TextUtils.isEmpty(mLinkInviteId)) {
            mTUIPlayerView.onNotifyPlayState(status);
        }
    }

    @Override
    public void pauseAudio() {
        mStreamService.pauseAudio();
    }

    @Override
    public void pauseVideo() {
        mStreamService.pauseVideo();
    }

    @Override
    public void resumeAudio() {
        mStreamService.resumeAudio();
    }

    @Override
    public void resumeVideo() {
        mStreamService.resumeVideo();
    }

    private void resetLinkData() {
        mLinkInviteId = "";
        mLinkStreamId = "";
        mLinkUserId = "";
    }
}
