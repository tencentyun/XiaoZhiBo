package com.tencent.qcloud.tuikit.tuiplayer.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.tuicore.TUIConfig;
import com.tencent.qcloud.tuikit.tuiplayer.R;
import com.tencent.qcloud.tuikit.tuiplayer.model.utils.LinkURLUtils;
import com.tencent.qcloud.tuikit.tuiplayer.presenter.ITUIPlayerContract;
import com.tencent.qcloud.tuikit.tuiplayer.presenter.TUIPlayerPresenter;
import com.tencent.qcloud.tuikit.tuiplayer.view.custom.ContainerView;
import com.tencent.qcloud.tuikit.tuiplayer.view.listener.TUIPlayerViewListener;
import com.tencent.qcloud.tuikit.tuiplayer.view.videoview.TUIVideoView;

import java.util.List;

/**
 * 拉流相关逻辑封装，主要包含功能有
 * 
 * - 开始拉流{@link TUIPlayerView#startPlay(String)}
 * - 停止拉流{@link TUIPlayerView#stopPlay()}
 * - 设置拉流状态监听{@link TUIPlayerView#setTUIPlayerViewListener(TUIPlayerViewListener)}
 * - 禁用连麦功能{@link TUIPlayerView#disableLinkMic(boolean)}
 */
public class TUIPlayerView extends FrameLayout implements ITUIPlayerContract.ITUIPlayerView {
    private static final String TAG = "TUIPusherView";

    private TUIPlayerPresenter    mTUIPusherPresenter;
    public  View                  mViewRoot;
    public  TUIVideoView          mTUIVideoView;
    public  ContainerView         mContainerView;
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
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.tuiplayer_player_view, this, true);
        initTUIVideoView();
        initFunctionView();
    }

    private void initPresenter() {
        mTUIPusherPresenter = new TUIPlayerPresenter(this, getContext());
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
                        ToastUtils.showShort(R.string.tuiplayer_linking_please_wait);
                    } else if (mLinkState == LinkState.LINK_REQ_SEND_SUCCESS) {
                        mTUIPusherPresenter.cancelLink();
                        mLinkState = LinkState.LINK_CANCEL_SEND;
                        updateLinkState(mLinkState, "");
                    } else if (mLinkState == LinkState.LINK_PUSH_SEND_SUCCESS) {
                        mTUIPusherPresenter.stopLink(15);
                    }
                } else if (mState == State.PLAY) {
                    mState = State.LINK;
                    mLinkState = LinkState.LINK_REQ_SEND;
                    updateLinkState(mLinkState, "");
                    mTUIPusherPresenter.requestLink(mAnchorId);
                }
            }
        });
        addView(mContainerView);
    }

    public void updateState(State state) {

    }

    public void updateLinkState(LinkState state, String reason) {
        switch (state) {
            case LINK_REQ_SEND:
                break;

            case LINK_REQ_SEND_SUCCESS:
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_off_icon);
                break;

            case LINK_REQ_SEND_FAIL:
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                ToastUtils.showShort(R.string.tuiplayer_link_request_send_fail);
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                break;

            case LINK_CANCEL_SEND:
                break;

            case LINK_CANCEL_SUCCESS:
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                break;

            case LINK_CANCEL_FAIL:
                ToastUtils.showShort(R.string.tuiplayer_link_cancel_cmd_send_fail);
                break;

            case LINK_ACCAPT:
                mTUIVideoView.showLinkMode(true);
                break;

            case LINK_REJECT:
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                mTUIVideoView.showLinkMode(false);
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                if(mListener != null){
                    mListener.onRejectJoinAnchorResponse(this, RejectReason.getRejectReasonCode(reason));
                }
                break;
            case LINK_PUSH_SEND:

                break;

            case LINK_PUSH_SEND_SUCCESS:
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_off_icon);
                break;

            case LINK_PUSH_SEND_FAIL:
                ToastUtils.showShort(getContext().getResources().getString(R.string.tuiplayer_push_cmd_send_fail));
                break;

            case LINK_STOP:
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                mTUIVideoView.showLinkMode(false);
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                mTUIPusherPresenter.startPlay(mPlayUrl, mTUIVideoView.getMainVideoView());
                break;

            case LINK_TIMEOUT:
                ToastUtils.showShort(getContext().getResources().getString(R.string.tuiplayer_link_request_timeout));
                mState = State.PLAY;
                mLinkState = LinkState.LINK_IDLE;
                mTUIVideoView.showLinkMode(false);
                mContainerView.setLinkImage(R.drawable.tuiplayer_link_on_icon);
                break;
        }
    }

    @Override
    public void onToastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponseJoinAnchor(String streamId) {
        mTUIVideoView.showLinkMode(true);
        mTUIPusherPresenter.startPlay(LinkURLUtils.generatePlayUrl(streamId), mTUIVideoView.getMainVideoView());
        mTUIPusherPresenter.startPush(true, mTUIVideoView.getLinkVideoView());
    }

    @Override
    public void onNotifyState(State state) {
        TXCLog.d(TAG, "onNotiyState:" + state);
        mState = state;
        updateState(mState);
    }

    @Override
    public void onNotifyLinkState(LinkState state, String reason) {
        TXCLog.d(TAG, "onNotifyLinkState:" + state);
        mLinkState = state;
        updateLinkState(mLinkState, reason);
    }

    @Override
    public void onNotifyPlayState(PlayStatus status) {
        TXCLog.d(TAG, "onNotifyPlayState:" + status);
        if (mListener != null) {
            switch (status) {
                case START_PLAY:
                    mListener.onPlayStarted(TUIPlayerView.this, mPlayUrl);
                    break;
                case STOP_PLAY:
                    mListener.onPlayStoped(TUIPlayerView.this, mPlayUrl);
                    break;
            }
        }
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

    /**
     * 开始拉流
     *
     * @param url 流地址
     */
    public void startPlay(String url) {
        TXCLog.d(TAG, "start url:" + url);
        if (!LinkURLUtils.checkPlayURL(url)) {
            ToastUtils.showShort(R.string.tuiplayer_url_empty);
            return;
        }
        mPlayUrl = url;
        mAnchorId = LinkURLUtils.getStreamIdByPushUrl(mPlayUrl);
        mRoomId = getRoomId(mAnchorId);
        mContainerView.setGroupId(mRoomId);
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                mTUIPusherPresenter.startPlay(mPlayUrl, mTUIVideoView.getMainVideoView());
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                ToastUtils.showShort(R.string.tuiplayer_tips_start_camera_audio);
            }
        }).request();
    }

    /**
     * 停止拉流
     */
    public void stopPlay() {
        TXCLog.d(TAG, "stopPlay");
        if (mLinkState == LinkState.LINK_PUSH_SEND_SUCCESS) {
            mTUIPusherPresenter.stopLink(15);
        }
        mTUIPusherPresenter.destory();
    }

    /**
     * 是否支持连麦功能
     *
     * @param enable true:不支持 false:支持
     */
    public void disableLinkMic(boolean enable) {
        TXCLog.d(TAG, "disableLinkMic");
        if (enable) {
            mContainerView.setLinkVisible(false);
        } else {
            mContainerView.setLinkVisible(true);
        }
    }

    public String getRoomId(String userId) {
        if(TextUtils.isEmpty(userId)){
            return "";
        }
        return userId;
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
