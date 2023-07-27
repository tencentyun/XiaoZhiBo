package com.tencent.qcloud.tuikit.tuiplayer.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.tuicore.TUIConfig;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuiplayer.R;
import com.tencent.qcloud.tuikit.tuiplayer.model.utils.LinkURLUtils;
import com.tencent.qcloud.tuikit.tuiplayer.model.utils.PermissionHelper;
import com.tencent.qcloud.tuikit.tuiplayer.presenter.TUIPlayerPresenter;
import com.tencent.qcloud.tuikit.tuiplayer.view.custom.ContainerView;
import com.tencent.qcloud.tuikit.tuiplayer.view.listener.TUIPlayerViewListener;
import com.tencent.qcloud.tuikit.tuiplayer.view.videoview.TUIVideoView;

import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_ERROR_FAILED;

/**
 * 拉流相关逻辑封装，主要包含功能有
 * <p>
 * - 开始拉流{@link TUIPlayerView#startPlay(String)}
 * - 停止拉流{@link TUIPlayerView#stopPlay()}
 * - 设置拉流状态监听{@link TUIPlayerView#setTUIPlayerViewListener(TUIPlayerViewListener)}
 * - 禁用连麦功能{@link TUIPlayerView#disableLinkMic()}
 * - 暂停视频流{@link TUIPlayerView#pauseVideo()}
 * - 恢复视频流{@link TUIPlayerView#resumeVideo()}
 * - 暂停音频流{@link TUIPlayerView#pauseAudio()}
 * - 恢复音频流{@link TUIPlayerView#resumeAudio()}
 */
public class TUIPlayerView extends FrameLayout implements ITUIPlayerView {
    private static final String TAG = "TUIPlayerView";

    private TUIPlayerPresenter    mTUIPlayerPresenter;
    private View                  mViewRoot;
    private TUIVideoView          mTUIVideoView;
    private ContainerView         mContainerView;
    private State                 mState     = State.PLAY;
    private LinkState             mLinkState = LinkState.LINK_IDLE;
    private String                mPlayUrl;
    private String                mRoomId;
    private String                mAnchorId;
    private TUIPlayerViewListener mListener;

    public TUIPlayerView(Context context) {
        this(context, null);
    }

    public TUIPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        initPresenter();
        TUIConfig.setSceneOptimizParams("TUIPlayer");
    }

    private void initView() {
        mViewRoot = LayoutInflater.from(getContext()).inflate(
                R.layout.tuiplayer_player_view, this, true);
        initTUIVideoView();
        initFunctionView();
    }

    private void initPresenter() {
        mTUIPlayerPresenter = new TUIPlayerPresenter(this, getContext());
        setTUIPlayerPresenterListener();
    }


    private void initTUIVideoView() {
        mTUIVideoView = new TUIVideoView(getContext());
        addView(mTUIVideoView);
    }

    private void initFunctionView() {
        mContainerView = new ContainerView(getContext());
        mContainerView.setListener(new ContainerView.OnLinkListener() {
            @Override
            public void onLink() {
                if (mState == State.LINK) {
                    if (mLinkState == LinkState.LINK_REQ_SEND) {
                        ToastUtil.toastShortMessage(getContext()
                                .getResources().getString(R.string.tuiplayer_linking_please_wait));
                    } else if (mLinkState == LinkState.LINK_REQ_SEND_SUCCESS) {
                        mTUIPlayerPresenter.cancelLink();
                        mLinkState = LinkState.LINK_CANCEL_SEND;
                        updateLinkState(mLinkState, "");
                    } else if (mLinkState == LinkState.LINK_PUSH_SEND_SUCCESS) {
                        mTUIPlayerPresenter.stopLink(15);
                    }
                } else if (mState == State.PLAY) {
                    PermissionHelper.requestPermission(getContext(),
                            PermissionHelper.PERMISSION_CAMERA, new PermissionHelper.PermissionCallback() {
                                @Override
                                public void onGranted() {
                                    PermissionHelper.requestPermission(getContext(),
                                            PermissionHelper.PERMISSION_MICROPHONE,
                                            new PermissionHelper.PermissionCallback() {

                                                public void onGranted() {
                                                    mState = State.LINK;
                                                    mLinkState = LinkState.LINK_REQ_SEND;
                                                    updateLinkState(mLinkState, "");
                                                    mTUIPlayerPresenter.requestLink(mAnchorId);
                                                }

                                                @Override
                                                public void onDenied() {
                                                }
                                            });
                                }

                                @Override
                                public void onDenied() {
                                }

                            });
                }
            }
        });
        addView(mContainerView);
    }

    private void updateLinkState(LinkState state, String reason) {
        switch (state) {
            case LINK_REQ_SEND:
                if (mListener != null) {
                    mListener.onPlayEvent(this,
                            TUIPlayerViewListener.TUIPlayerEvent.TUIPLAYER_EVENT_LINKMIC_START,
                            "LinkMic start request");
                }
                break;

            case LINK_REQ_SEND_SUCCESS:
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_off_icon);
                break;

            case LINK_REQ_SEND_FAIL:
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                ToastUtil.toastShortMessage(getContext()
                        .getResources().getString(R.string.tuiplayer_link_request_send_fail));
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                if (mListener != null) {
                    mListener.onPlayEvent(this,
                            TUIPlayerViewListener.TUIPlayerEvent.TUIPLAYER_EVENT_LINKMIC_STOP,
                            "LinkMic request failed");
                }
                break;

            case LINK_CANCEL_SEND:
                break;

            case LINK_CANCEL_SUCCESS:
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                if (mListener != null) {
                    mListener.onPlayEvent(this,
                            TUIPlayerViewListener.TUIPlayerEvent.TUIPLAYER_EVENT_LINKMIC_STOP,
                            "LinkMic cancel");
                }
                break;

            case LINK_CANCEL_FAIL:
                ToastUtil.toastShortMessage(getContext()
                        .getResources().getString(R.string.tuiplayer_link_cancel_cmd_send_fail));
                break;

            case LINK_ACCAPT:
                mTUIVideoView.showLinkMode(true);
                break;

            case LINK_REJECT:
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                mTUIVideoView.showLinkMode(false);
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                if (mListener != null) {
                    mListener.onRejectJoinAnchorResponse(this, RejectReason.getRejectReasonCode(reason));
                }
                break;
            case LINK_PUSH_SEND:

                break;

            case LINK_PUSH_SEND_SUCCESS:
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_off_icon);
                break;

            case LINK_PUSH_SEND_FAIL:
                ToastUtil.toastShortMessage(getContext().getResources()
                        .getString(R.string.tuiplayer_push_cmd_send_fail));
                break;

            case LINK_STOP:
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                mTUIVideoView.showLinkMode(false);
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                mTUIPlayerPresenter.startPlay(mPlayUrl, mTUIVideoView.getMainVideoView());
                mListener.onPlayEvent(this, TUIPlayerViewListener.TUIPlayerEvent.TUIPLAYER_EVENT_LINKMIC_STOP,
                        "LinkMic stop");
                break;

            case LINK_TIMEOUT:
                ToastUtil.toastShortMessage(getContext()
                        .getResources().getString(R.string.tuiplayer_link_request_timeout));
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                mTUIVideoView.showLinkMode(false);
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                break;
            default:
                break;
        }
    }

    private void setTUIPlayerPresenterListener() {
        mTUIPlayerPresenter.setListener(new TUIPlayerPresenter.TUIPlayerPresenterListener() {
            @Override
            public void onToastMessage(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponseJoinAnchor(String streamId) {
                mTUIVideoView.showLinkMode(true);
                mTUIPlayerPresenter.startPlay(LinkURLUtils.generatePlayUrl(streamId), mTUIVideoView.getMainVideoView());
                mTUIPlayerPresenter.startPush(true, mTUIVideoView.getLinkVideoView());
            }

            @Override
            public void onNotifyState(State state) {
                TXCLog.i(TAG, "onNotifyState:" + state);
                mState = state;
            }

            @Override
            public void onNotifyLinkState(LinkState state, String reason) {
                TXCLog.i(TAG, "onNotifyLinkState:" + state);
                mLinkState = state;
                updateLinkState(mLinkState, reason);
            }

            @Override
            public void onNotifyPlayState(PlayStatus status) {
                TXCLog.i(TAG, "onNotifyPlayState:" + status);
                if (mListener != null) {
                    switch (status) {
                        case START_PLAY:
                            mListener.onPlayStarted(TUIPlayerView.this, mPlayUrl);
                            break;
                        case STOP_PLAY:
                            mListener.onPlayStopped(TUIPlayerView.this, mPlayUrl);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    public void setTUIPlayerViewListener(TUIPlayerViewListener mListener) {
        this.mListener = mListener;
    }

    /**
     * UI 状态
     */
    public enum State {
        PLAY,       // 正常拉流中
        LINK,       // 连麦中
    }


    /**
     * 播放状态变更
     */
    public enum PlayStatus {
        START_PLAY,
        STOP_PLAY,
    }


    /**
     * 连麦 状态
     */
    public enum LinkState {
        LINK_IDLE,                  //连麦 空闲 状态
        LINK_REQ_SEND,              //发起连麦请求
        LINK_REQ_SEND_SUCCESS,      //连麦请求发送成功
        LINK_REQ_SEND_FAIL,         //连麦请求发送失败
        LINK_CANCEL_SEND,           //发起取消连麦请求
        LINK_CANCEL_SUCCESS,        //连麦请求发送成功
        LINK_CANCEL_FAIL,           //连麦请求发送失败
        LINK_ACCAPT,                //主播接受连麦请求
        LINK_REJECT,                //主播拒绝连麦请求
        LINK_PUSH_SEND,             //观众成功推流发送请求
        LINK_PUSH_SEND_SUCCESS,     //观众成功推流发送请求成功
        LINK_PUSH_SEND_FAIL,        //观众成功推流发送请求失败
        LINK_STOP,                  //主播停止连麦请求
        LINK_TIMEOUT,               //主播连麦请求超时
    }

    @Override
    public void setGroupId(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            mRoomId = "";
        }
        mRoomId = groupId;
    }

    @Override
    public int startPlay(String url) {
        TXCLog.i(TAG, "start url:" + url);
        if (!LinkURLUtils.checkPlayURL(url)) {
            ToastUtil.toastShortMessage(getContext().getResources().getString(R.string.tuiplayer_url_empty));
            return V2TXLIVE_ERROR_FAILED;
        }
        mPlayUrl = url;
        mAnchorId = LinkURLUtils.getStreamIdByPushUrl(mPlayUrl);
        mContainerView.setGroupId(mRoomId);
        int ret = mTUIPlayerPresenter.startPlay(mPlayUrl, mTUIVideoView.getMainVideoView());
        if (mListener == null) {
            return ret;
        }
        if (ret == 0) {
            mListener.onPlayEvent(TUIPlayerView.this,
                    TUIPlayerViewListener.TUIPlayerEvent.TUIPLAYER_EVENT_SUCCESS,
                    "Start play success");
        } else if (ret == -4) {
            mListener.onPlayEvent(TUIPlayerView.this,
                    TUIPlayerViewListener.TUIPlayerEvent.TUIPLAYER_EVENT_URL_NOTSUPPORT, "URL not support");
        } else if (ret == -5) {
            mListener.onPlayEvent(TUIPlayerView.this,
                    TUIPlayerViewListener.TUIPlayerEvent.TUIPLAYER_EVENT_INVALID_LICENSE, "License invalid");
        } else {
            mListener.onPlayEvent(TUIPlayerView.this,
                    TUIPlayerViewListener.TUIPlayerEvent.TUIPLAYER_EVENT_FAILED,
                    "Start play failed");
        }
        return ret;
    }

    @Override
    public void stopPlay() {
        TXCLog.i(TAG, "stopPlay");
        if (mLinkState == LinkState.LINK_PUSH_SEND_SUCCESS) {
            mTUIPlayerPresenter.stopLink(15);
        } else if (mLinkState == LinkState.LINK_REQ_SEND_SUCCESS) {
            mTUIPlayerPresenter.cancelLink();
        }
        mTUIPlayerPresenter.destory();
    }

    @Override
    public void disableLinkMic() {
        TXCLog.i(TAG, "disableLinkMic");
        mContainerView.setLinkVisible(View.GONE);
    }

    @Override
    public void resumeAudio() {
        mTUIPlayerPresenter.resumeAudio();
    }

    @Override
    public void resumeVideo() {
        mTUIPlayerPresenter.resumeVideo();
    }

    @Override
    public void pauseAudio() {
        mTUIPlayerPresenter.pauseAudio();
    }

    @Override
    public void pauseVideo() {
        mTUIPlayerPresenter.pauseVideo();
    }

    @Override
    public void updatePlayerUIState(TUIPlayerUIState state) {
        if (state == TUIPlayerUIState.TUIPLAYER_UISTATE_DEFAULT) {
            mContainerView.setVisibility(VISIBLE);
        } else {
            mContainerView.setVisibility(GONE);
        }
    }

    private enum RejectReason {
        NORMAL("1"),        //正常拒绝PK
        BUSY("2");          //忙线中(PK或者连麦中)

        RejectReason(String reason) {
            this.reason = reason;
        }

        private String reason;
        private String code;

        public String getReason() {
            return reason;
        }

        public static boolean isRejectReason(String reason) {
            if (TextUtils.isEmpty(reason)) {
                return false;
            }
            return NORMAL.getReason().equals(reason) || BUSY.getReason().equals(reason);
        }

        public static int getRejectReasonCode(String reason) {
            if (TextUtils.isEmpty(reason)) {
                return -1;
            }
            if (NORMAL.getReason().equals(reason)) {
                return 1;
            }
            if (BUSY.getReason().equals(reason)) {
                return 2;
            }
            return -1;
        }
    }
}
