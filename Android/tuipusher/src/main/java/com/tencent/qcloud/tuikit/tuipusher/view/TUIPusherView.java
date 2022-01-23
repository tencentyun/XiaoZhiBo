package com.tencent.qcloud.tuikit.tuipusher.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.tuicore.TUIConfig;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuipusher.R;
import com.tencent.qcloud.tuikit.tuipusher.model.TUIPusherConfig;
import com.tencent.qcloud.tuikit.tuipusher.model.service.impl.TUIPusherSignallingService;
import com.tencent.qcloud.tuikit.tuipusher.model.utils.LinkURLUtils;
import com.tencent.qcloud.tuikit.tuipusher.presenter.ITUIPusherContract;
import com.tencent.qcloud.tuikit.tuipusher.presenter.TUIPusherPresenter;
import com.tencent.qcloud.tuikit.tuipusher.view.custom.ContainerView;
import com.tencent.qcloud.tuikit.tuipusher.view.custom.CountDownView;
import com.tencent.qcloud.tuikit.tuipusher.view.custom.StartPushView;
import com.tencent.qcloud.tuikit.tuipusher.view.listener.TUIPusherViewListener;
import com.tencent.qcloud.tuikit.tuipusher.view.videoview.TUIVideoView;

import java.util.List;

import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.State.COUNTDOWN;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.State.LINK;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.State.PK;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.State.PUSH;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.State.PUSH_SUCCESS;

/**
 * 推流相关逻辑封装，主要包含以下功能
 * <p>
 * - 开始预览{@link TUIPusherView#start(String)}
 * - 设置镜像{@link TUIPusherView#setMirror(boolean)}
 * - 切换摄像头{@link TUIPusherView#switchCamera(boolean)}
 * - 设置分辨率{@link TUIPusherView#setVideoResolution(TUIPusherVideoResolution)}
 * - 设置事件监听回调{@link TUIPusherView#setTUIPusherViewListener(TUIPusherViewListener)}
 * - 发送PK{@link TUIPusherView#sendPKRequest(String)}
 * - 取消PK{@link TUIPusherView#cancelPKRequest()}
 * - 停止PK{@link TUIPusherView#stopPK()}
 * - 停止连麦{@link TUIPusherView#stopJoinAnchor()}
 * </p>
 */
public class TUIPusherView extends FrameLayout implements ITUIPusherContract.ITUIPusherView {
    private static final String TAG = "TUIPusherView";

    private TUIPusherPresenter    mTUIPusherPresenter;
    private View                  mViewRoot;
    private TUIVideoView          mTUIVideoView;
    private CountDownView         mCountDownView;
    private StartPushView         mStartPushView;
    public  ContainerView         mContainerView;
    private String                mPushUrl;
    private TUIPusherViewListener mListener;
    private boolean               mIsFrontCamera = true;

    private volatile State     mState     = State.PREVIEW;
    private volatile PKState   mPKState   = PKState.IDLE;
    private volatile LinkState mLinkState = LinkState.IDLE;

    public TUIPusherView(Context context) {
        this(context, null);
    }

    public TUIPusherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        TUIConfig.setSceneOptimizParams("TUIPusher");
    }

    private void initView() {
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.tuipusher_pusher_view, this, true);
        initTUIVideoView();
    }

    private void initFunctionView() {
        initStartView();
        initCountDownView();
        initContainerView();
    }

    private void initContainerView() {
        mContainerView = new ContainerView(getContext());
        mContainerView.initExtentionView(mTUIPusherPresenter.getTXAudioEffectManager(), mTUIPusherPresenter.getTXBeautyManager());
        addView(mContainerView);
        mContainerView.setVisibility(GONE);
    }

    private void initTUIVideoView() {
        mTUIVideoView = new TUIVideoView(getContext());
        mTUIVideoView.setListener(new TUIVideoView.OnCloseListener() {
            @Override
            public void onClose() {
                stopJoinAnchor();
            }
        });
        mTUIVideoView.showLinkMode(false);
        addView(mTUIVideoView);
    }

    private void initStartView() {
        mStartPushView = new StartPushView(getContext());
        mStartPushView.initExtentionView(mTUIPusherPresenter.getTXBeautyManager());
        mStartPushView.setListener(new StartPushView.OnStartPushListener() {
            @Override
            public void onClickStartPush() {
                if (mListener != null) {
                    mListener.onClickStartPushButton(TUIPusherView.this, mPushUrl, new TUIPusherViewListener.ResponseCallback() {
                        @Override
                        public void response(boolean isAgree) {
                            if (isAgree) {
                                updateState(COUNTDOWN);
                            } else {
                                ToastUtil.toastShortMessage(getContext().getString(R.string.tuipusher_user_no_agree_push));
                            }
                        }
                    });
                } else {
                    updateState(COUNTDOWN);
                }
            }

            @Override
            public void onSwitchCamera() {
                mIsFrontCamera = !mIsFrontCamera;
                TUIPusherConfig.getInstance().setFrontCamera(mIsFrontCamera);
                switchCamera(mIsFrontCamera);
            }
        });
        addView(mStartPushView);
    }

    private void initCountDownView() {
        mCountDownView = new CountDownView(getContext());
        mCountDownView.setListener(new CountDownView.OnTimeEndListener() {
            @Override
            public void onTimeEnd() {
                mState = PUSH;
                if (mTUIPusherPresenter != null) {
                    mTUIPusherPresenter.startPush(mPushUrl);
                }
                mCountDownView.setVisibility(GONE);
            }
        });
        addView(mCountDownView);
        mCountDownView.setVisibility(GONE);
    }

    private void startPreview() {
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                mTUIPusherPresenter.startPreview(true, mTUIVideoView.getPushVideoView());
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                ToastUtil.toastShortMessage(getContext().getString(R.string.tuipusher_tips_start_camera_audio));
            }
        }).request();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            mTUIPusherPresenter.stopVirtualCamera();
        } else if (visibility == INVISIBLE) {
            mTUIPusherPresenter.startVirtualCamera(BitmapFactory.decodeResource(getResources(), R.drawable.tuipusher_private));
        }
    }

    private void updateState(State state) {
        switch (state) {
            case PREVIEW:
                break;
            case COUNTDOWN:
                mStartPushView.setVisibility(GONE);
                mCountDownView.setVisibility(VISIBLE);
                mCountDownView.start();
                break;

            case PUSH_SUCCESS:
                mState = PUSH_SUCCESS;
                mContainerView.setVisibility(VISIBLE);
                if (mListener != null) {
                    mListener.onPushStarted(this, mPushUrl);
                }
                break;

        }
    }

    public void setGroupId(String groupId) {
        mContainerView.setGroupId(groupId);
    }

    private void updatePKState(PKState pkState, String userId, String reason) {
        TXCLog.d(TAG, "updatePKState pkState:" + pkState + ", userId:" + userId);
        switch (pkState) {
            case PK_RECEIVE_REQ:
                if (mListener != null) {
                    mListener.onReceivePKRequest(this, userId, new TUIPusherViewListener.ResponseCallback() {
                        @Override
                        public void response(boolean isAgree) {
                            if (isAgree) { //同意
                                mTUIPusherPresenter.responsePK(true, "", 10);
                                mTUIVideoView.showPKMode(true);
                                mTUIPusherPresenter.startPK(mTUIVideoView.getPKVideoView());
                            } else {       //拒绝
                                mState = PUSH_SUCCESS;
                                mPKState = PKState.IDLE;
                                mTUIPusherPresenter.responsePK(false, TUIPusherSignallingService.RejectReason.NORMAL.getReason(), 10);
                                mTUIVideoView.showPKMode(false);
                            }
                        }
                    });
                }
                break;
            case PK_ACCAPT:
                mTUIVideoView.showPKMode(true);
                mTUIPusherPresenter.startPK(mTUIVideoView.getPKVideoView());
                break;

            case PK_CANCEL:
                mState = PUSH_SUCCESS;
                mPKState = PKState.IDLE;
                if (mListener != null) {
                    mListener.onCancelPKRequest(TUIPusherView.this);
                }
                break;
            case PK_REJECT:
                mState = PUSH_SUCCESS;
                mPKState = PKState.IDLE;
                if (mListener != null) {
                    mListener.onRejectPKResponse(this, TUIPusherSignallingService.RejectReason.getRejectReasonCode(reason));
                }
                break;
            case PK_TIMEOUT:
                mState = PUSH_SUCCESS;
                mPKState = PKState.IDLE;
                if (mListener != null) {
                    mListener.onPKTimeout(this);
                }
                break;
            case PK_STSRT:
                if (mListener != null) {
                    mListener.onStartPK(this);
                }
                break;
            case PK_STOP:
                mState = PUSH_SUCCESS;
                mPKState = PKState.IDLE;
                mTUIVideoView.showPKMode(false);
                if (mListener != null) {
                    mListener.onStopPK(TUIPusherView.this);
                }
        }
    }

    private void updateLinkState(LinkState linkState, String userId, String reason) {
        TXCLog.d(TAG, "updateLinkState linkState:" + linkState + ", userId:" + userId);
        switch (linkState) {
            case LINK_REQ:
                if (mListener != null) {
                    mListener.onReceiveJoinAnchorRequest(this, userId, new TUIPusherViewListener.ResponseCallback() {
                        @Override
                        public void response(boolean isAgree) {
                            if (isAgree) { //同意
                                mTUIPusherPresenter.responseLink(true, "", 10);
                            } else {       //拒绝
                                mState = PUSH_SUCCESS;
                                mLinkState = LinkState.IDLE;
                                mTUIPusherPresenter.responseLink(false, TUIPusherSignallingService.RejectReason.NORMAL.getReason(), 10);
                            }
                        }
                    });
                }
                break;
            case LINK_START:
                if (mListener != null) {
                    mListener.onStartJoinAnchor(this);
                }
                break;
            case LINK_TIMEOUT:
                mState = PUSH_SUCCESS;
                mLinkState = LinkState.IDLE;
                mTUIVideoView.showLinkMode(false);
                if (mListener != null) {
                    mListener.onJoinAnchorTimeout(this);
                }
                break;
            case LINK_STOP:
                mState = PUSH_SUCCESS;
                mLinkState = LinkState.IDLE;
                mTUIVideoView.showLinkMode(false);
                if (mListener != null) {
                    mListener.onStopJoinAnchor(this);
                }
                break;

            case LINK_CANCEL:
                mState = PUSH_SUCCESS;
                mLinkState = LinkState.IDLE;
                mTUIVideoView.showLinkMode(false);
                if (mListener != null) {
                    mListener.onCancelJoinAnchorRequest(this);
                }
                break;
        }
    }

    @Override
    public void onToastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotifyState(State state) {
        TXCLog.d(TAG, "onNotifyState state:" + state);
        mState = state;
        updateState(mState);
    }

    @Override
    public void onNotifyPKState(PKState pkState, String userId, String reason) {
        TXCLog.d(TAG, "onNotifyPKState pkState:" + pkState);
        mPKState = pkState;
        if ((mState == PUSH_SUCCESS || mState == PK) && mLinkState == LinkState.IDLE) {
            mState = PK;
            updatePKState(mPKState, userId, reason);
        } else {
            if (mPKState == PKState.PK_RECEIVE_REQ) {
                mState = PUSH_SUCCESS;
                mPKState = PKState.IDLE;
                mTUIPusherPresenter.responsePK(false, TUIPusherSignallingService.RejectReason.BUSY.getReason(), 10);
            }
            TXCLog.d(TAG, "onNotifyPKState mState:" + mState + ", mPKState:" + mPKState + ", mLinkState:" + mLinkState + ", userId:" + userId);
        }

    }

    @Override
    public void onNotifyLinkState(LinkState linkState, String userId, String reason) {
        TXCLog.d(TAG, "onNotifyLinkState linkState:" + linkState);
        mLinkState = linkState;
        if ((mState == PUSH_SUCCESS || mState == LINK) && mPKState == PKState.IDLE) {
            mState = LINK;
            updateLinkState(mLinkState, userId, reason);
        } else {
            if (mLinkState == LinkState.LINK_REQ) {
                mState = PUSH_SUCCESS;
                mLinkState = LinkState.IDLE;
                mTUIPusherPresenter.responseLink(false, TUIPusherSignallingService.RejectReason.BUSY.getReason(), 10);
            }
            TXCLog.d(TAG, "onNotifyLinkState mState:" + mState + ", mPKState:" + mPKState + ", mLinkState:" + mLinkState + ", userId:" + userId);
        }
    }

    @Override
    public void onStartLink() {
        TXCLog.d(TAG, "onStartLink");
        mTUIVideoView.showLinkMode(true);
        mTUIPusherPresenter.startLink(mTUIVideoView.getLinkVideoView());
    }

    /**
     * 设置TUIPusherView 事件的回调对象
     *
     * @param mListener
     */
    public void setTUIPusherViewListener(TUIPusherViewListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 设置镜像模式
     *
     * @param isMirror true:开启镜像模式; false:关闭镜像模式
     */
    public void setMirror(boolean isMirror) {
        TXCLog.d(TAG, "setMirror isMirror:" + isMirror);
        mTUIPusherPresenter.setMirror(isMirror);
    }

    /**
     * 切换前后摄像头
     *
     * @param isFrontCamera true:使用前置摄像头； false:使用后置摄像头
     */
    public void switchCamera(boolean isFrontCamera) {
        TXCLog.d(TAG, "switchCamera isFrontCamera:" + isFrontCamera);
        mTUIPusherPresenter.switchCamera(isFrontCamera);
    }

    /**
     * 设置分辨率
     *
     * @param resolution
     */
    public void setVideoResolution(TUIPusherVideoResolution resolution) {
        TXCLog.d(TAG, "setVideoResolution resolution:" + resolution);
        int tempResolution = 1;
        switch (resolution) {
            case TUIPUSHER_VIDEO_RES_360:
                tempResolution = 1;
                break;
            case TUIPUSHER_VIDEO_RES_540:
                tempResolution = 2;
                break;
            case TUIPUSHER_VIDEO_RES_720:
                tempResolution = 3;
                break;
            case TUIPUSHER_VIDEO_RES_1080:
                tempResolution = 4;
                break;
        }
        mTUIPusherPresenter.setResolution(tempResolution);
    }

    /**
     * 开始摄像头预览
     *
     * @param url
     */
    public void start(String url) {
        TXCLog.d(TAG, "start url:" + url);
        if (!TUILogin.isUserLogined()) {
            ToastUtil.toastShortMessage(getContext().getString(R.string.tuipusher_please_login));
            return;
        }
        if (url.startsWith(LinkURLUtils.RTMP)) {
            mTUIPusherPresenter = new TUIPusherPresenter(this, getContext(), LinkURLUtils.PushType.RTMP);
        } else if (url.startsWith(LinkURLUtils.TRTC)) {
            mTUIPusherPresenter = new TUIPusherPresenter(this, getContext(), LinkURLUtils.PushType.RTC);
        } else {
            ToastUtil.toastShortMessage(getContext().getString(R.string.tuipusher_url_error));
            return;
        }
        initFunctionView();
        mPushUrl = url;
        startPreview();
    }

    /**
     * 停止直播
     */
    public void stop() {
        TXCLog.d(TAG, "stop");
        TUIPusherConfig.getInstance().destory();
        if (mState == State.PK) {
            mTUIPusherPresenter.stopPK();
        }
        if (mTUIPusherPresenter != null) {
            mTUIPusherPresenter.destory();
        }
    }

    /**
     * 发送 PK 请求
     *
     * @param userID
     * @return
     */
    public synchronized boolean sendPKRequest(String userID) {
        TXCLog.d(TAG, "sendPKRequest userID:" + userID);
        if (mState == PUSH_SUCCESS && mPKState == PKState.IDLE && mLinkState == LinkState.IDLE) {
            mState = State.PK;
            mPKState = PKState.PK_REQ;
            if (mTUIPusherPresenter.requestPK(userID)) {
                mState = PUSH_SUCCESS;
                return true;
            } else {
                return false;
            }
        } else {
            TXCLog.d(TAG, "sendPKRequest fail, mState:" + mState + ", mPKState： " + mPKState + ", mLinkState：" + mLinkState);
            return false;
        }
    }

    /**
     * 取消 PK 请求
     */
    public synchronized void cancelPKRequest() {
        TXCLog.d(TAG, "cancelPKRequest");
        mState = PUSH_SUCCESS;
        mPKState = PKState.IDLE;
        mTUIPusherPresenter.cancelPK();
    }

    /**
     * 发送 停止PK 请求
     */
    public void stopPK() {
        TXCLog.d(TAG, "stopPK");
        mState = PUSH_SUCCESS;
        mPKState = PKState.IDLE;
        mTUIPusherPresenter.stopPK();
        mTUIVideoView.showPKMode(false);
    }

    /**
     * 发送 停止连麦 请求
     */
    public void stopJoinAnchor() {
        TXCLog.d(TAG, "stopJoinAnchor");
        mState = PUSH_SUCCESS;
        mLinkState = LinkState.IDLE;
        mTUIPusherPresenter.stopLink(15);
        mTUIVideoView.showLinkMode(false);
    }

    public enum TUIPusherVideoResolution {
        TUIPUSHER_VIDEO_RES_360,
        TUIPUSHER_VIDEO_RES_540,
        TUIPUSHER_VIDEO_RES_720,
        TUIPUSHER_VIDEO_RES_1080,
    }


    /**
     * View 的内部状态
     */
    public enum State {
        PREVIEW,
        COUNTDOWN,
        PUSH,
        PUSH_SUCCESS,
        PK,
        LINK,
    }


    public enum LinkState {
        IDLE,
        LINK_REQ,
        LINK_START,
        LINK_STOP,
        LINK_CANCEL,
        LINK_TIMEOUT,
    }


    public enum PKState {
        IDLE,
        PK_REQ,
        PK_RECEIVE_REQ,
        PK_ACCAPT,
        PK_CANCEL,
        PK_REJECT,
        PK_STSRT,
        PK_TIMEOUT,
        PK_STOP,
    }
}
