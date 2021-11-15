package com.tencent.qcloud.tuikit.tuipusher.model.service.impl;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.tencent.qcloud.tuikit.tuipusher.model.listener.ITUIPusherStreamListener;
import com.tencent.qcloud.tuikit.tuipusher.model.service.ITUIPusherStreamService;
import com.tencent.qcloud.tuikit.tuipusher.model.utils.LinkURLUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.ArrayList;

import static com.tencent.live2.V2TXLiveDef.V2TXLiveMixInputType.V2TXLiveMixInputTypeAudioVideo;

public class TUIPusherStreamService implements ITUIPusherStreamService {
    private static final String TAG = TUIPusherStreamService.class.getSimpleName();

    private ITUIPusherStreamListener mListener;
    private V2TXLivePusher           mV2TXLivePusher;
    private V2TXLivePlayer           mV2TXLivePlayer;
    private boolean                  mIsPreview = false;

    public TUIPusherStreamService(Context context, LinkURLUtils.PushType pushType) {
        if (pushType == LinkURLUtils.PushType.RTC) {
            mV2TXLivePusher = new V2TXLivePusherImpl(context, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC);
        } else if (pushType == LinkURLUtils.PushType.RTMP) {
            mV2TXLivePusher = new V2TXLivePusherImpl(context, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP);
        }
        mV2TXLivePusher.setObserver(new TUILivePusherObserver());
        mV2TXLivePlayer = new V2TXLivePlayerImpl(context);
        mV2TXLivePlayer.setObserver(new TUILivePlayerObserver());
    }

    @Override
    public void setListener(ITUIPusherStreamListener listener) {
        mListener = listener;
    }

    @Override
    public int startCameraPreview(boolean isFront, TXCloudVideoView videoView) {
        TXCLog.d(TAG, "startCameraPreview isFront:" + isFront);
        mV2TXLivePusher.setRenderView(videoView);
        int result = mV2TXLivePusher.startCamera(isFront);
        mV2TXLivePusher.startMicrophone();
        mIsPreview = true;
        return 0;
    }

    @Override
    public int startPush(String url) {
        TXCLog.d(TAG, "startPush url:" + url);
        return mV2TXLivePusher.startPush(url);
    }

    @Override
    public int startPlay(TXCloudVideoView pkVideoView, String roomId, String userId) {
        TXCLog.d(TAG, "startPlay roomId:" + roomId + "userId:" + userId);
        mV2TXLivePlayer.setRenderView(pkVideoView);
        String playUrl = LinkURLUtils.generatePlayUrl(roomId);
        int ret = mV2TXLivePlayer.startPlay(playUrl);
        TXCLog.d(TAG, "startPlay: ret" + ret + ", playUrl:" + playUrl);
        return 0;
    }

    @Override
    public int stopPlay() {
        TXCLog.d(TAG, "stopPlay:");
        if (mV2TXLivePlayer != null && mV2TXLivePlayer.isPlaying() == 1) {
            mV2TXLivePlayer.stopPlay();
        }
        return 0;
    }

    @Override
    public int setPKMixTranscodingConfig(String myUserId, String myRoomId, String pkUserId, String pkRoomId) {
        TXCLog.d(TAG, "setPKMixTranscodingConfig myUserId:" + myUserId + ",myRoomId" + myRoomId + ",pkUserId" + pkUserId + ",pkRoomId" + pkRoomId);
        int ret = -1;
        if (mV2TXLivePusher != null && mV2TXLivePusher.isPushing() == 1) {
            if (TextUtils.isEmpty(myUserId)) {
                ret = mV2TXLivePusher.setMixTranscodingConfig(null);
            } else {
                ret = mV2TXLivePusher.setMixTranscodingConfig(createPKConfig(myUserId, myRoomId, pkUserId, pkRoomId));
            }
        }
        TXCLog.d(TAG, "setPKMixTranscodingConfig ret:" + ret + ", myUserId" + myUserId + ", myRoomId" + myRoomId + ", pkUserId" + pkUserId + ", pkRoomId" + pkRoomId);
        return 0;
    }

    @Override
    public int setLinkMixTranscodingConfig(String myUserId, String myRoomId, String linkUserId, String linkRoomId) {
        TXCLog.d(TAG, "setLinkMixTranscodingConfig myUserId:" + myUserId + ",myRoomId" + myRoomId + ",linkUserId" + linkUserId + ",linkRoomId" + linkRoomId);
        int ret = -1;
        if (mV2TXLivePusher != null && mV2TXLivePusher.isPushing() == 1) {
            if (TextUtils.isEmpty(myUserId)) {
                ret = mV2TXLivePusher.setMixTranscodingConfig(null);
            } else {
                ret = mV2TXLivePusher.setMixTranscodingConfig(createLinkConfig(myUserId, myRoomId, linkUserId, linkRoomId));
            }
        }
        TXCLog.d(TAG, "setLinkMixTranscodingConfig ret:" + ret);
        return 0;
    }

    @Override
    public int stopPush() {
        TXCLog.d(TAG, "stopPush");
        return mV2TXLivePusher.stopPush();
    }

    @Override
    public void switchCamera(boolean frontCamera) {
        TXCLog.d(TAG, "switchCamera frontCamera:" + frontCamera);
        mV2TXLivePusher.getDeviceManager().switchCamera(frontCamera);
    }

    @Override
    public void setMirror(boolean isMirror) {
        TXCLog.d(TAG, "setMirror isMirror:" + isMirror);
        if (isMirror) {
            mV2TXLivePusher.setRenderMirror(V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeEnable);
        } else {
            mV2TXLivePusher.setRenderMirror(V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeDisable);
        }
        mV2TXLivePusher.setEncoderMirror(isMirror);
    }

    @Override
    public void setVideoQuality(V2TXLiveDef.V2TXLiveVideoEncoderParam param) {
        mV2TXLivePusher.setVideoQuality(param);
    }

    @Override
    public TXBeautyManager getTXBeautyManager() {
        if (mV2TXLivePusher != null) {
            return mV2TXLivePusher.getBeautyManager();
        }
        return null;
    }

    @Override
    public TXAudioEffectManager getTXAudioEffectManager() {
        if (mV2TXLivePusher != null) {
            return mV2TXLivePusher.getAudioEffectManager();
        }
        return null;
    }

    @Override
    public void destory() {
        TXCLog.d(TAG, "destory");
        if (mV2TXLivePusher == null) {
            return;
        }
        if (mIsPreview) {
            mV2TXLivePusher.stopCamera();
        }
        if (mV2TXLivePusher.isPushing() == 1) {
            mV2TXLivePusher.stopPush();
        }
        if (mV2TXLivePlayer.isPlaying() == 1) {
            mV2TXLivePlayer.stopPlay();
        }
    }

    public V2TXLivePusher getV2TXLivePusher(){
        return mV2TXLivePusher;
    }

    private V2TXLiveDef.V2TXLiveTranscodingConfig createPKConfig(String myUserId, String mySreamId, String pkUserId, String pkStreamId) {
        V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
        config.videoWidth = 360;
        config.videoHeight = 640;
        config.videoBitrate = 900;
        config.videoFramerate = 15;
        config.videoGOP = 2;
        config.backgroundColor = 0x000000;
        config.backgroundImage = null;
        config.audioSampleRate = 48000;
        config.audioBitrate = 64;
        config.audioChannels = 1;
        config.outputStreamId = null;
        config.mixStreams = new ArrayList<>();

        V2TXLiveDef.V2TXLiveMixStream mixStream = new V2TXLiveDef.V2TXLiveMixStream();
        mixStream.userId = myUserId;
        mixStream.streamId = mySreamId;
        mixStream.x = 0;
        mixStream.y = 160;
        mixStream.width = 180;
        mixStream.height = 320;
        mixStream.zOrder = 0;
        mixStream.inputType = V2TXLiveMixInputTypeAudioVideo;
        config.mixStreams.add(mixStream);

        V2TXLiveDef.V2TXLiveMixStream remote = new V2TXLiveDef.V2TXLiveMixStream();
        remote.userId = pkUserId;
        remote.streamId = pkStreamId;
        remote.x = 180;
        remote.y = 160;
        remote.width = 180;
        remote.height = 320;
        remote.zOrder = 1;
        remote.inputType = V2TXLiveMixInputTypeAudioVideo;
        config.mixStreams.add(remote);
        return config;
    }

    private V2TXLiveDef.V2TXLiveTranscodingConfig createLinkConfig(String myUserId, String myRoomId, String linkUserId, String linkRoomId) {
        V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
        config.videoWidth = 360;
        config.videoHeight = 640;
        config.videoBitrate = 900;
        config.videoFramerate = 15;
        config.videoGOP = 2;
        config.backgroundColor = 0x000000;
        config.backgroundImage = null;
        config.audioSampleRate = 48000;
        config.audioBitrate = 64;
        config.audioChannels = 1;
        config.outputStreamId = null;
        config.mixStreams = new ArrayList<>();

        V2TXLiveDef.V2TXLiveMixStream mixStream = new V2TXLiveDef.V2TXLiveMixStream();
        mixStream.userId = myUserId;
        mixStream.streamId = myRoomId;
        mixStream.x = 0;
        mixStream.y = 0;
        mixStream.width = 360;
        mixStream.height = 640;
        mixStream.zOrder = 0;
        mixStream.inputType = V2TXLiveMixInputTypeAudioVideo;
        config.mixStreams.add(mixStream);

        V2TXLiveDef.V2TXLiveMixStream remote = new V2TXLiveDef.V2TXLiveMixStream();
        remote.userId = linkUserId;
        remote.streamId = linkRoomId;
        remote.x = 225;
        remote.y = 100;
        remote.width = 90;
        remote.height = 160;
        remote.zOrder = 1;
        remote.inputType = V2TXLiveMixInputTypeAudioVideo;
        config.mixStreams.add(remote);
        return config;
    }

    class TUILivePusherObserver extends V2TXLivePusherObserver {
        @Override
        public void onSetMixTranscodingConfig(int code, String msg) {
            TXCLog.d(TAG, "TUILivePusherObserver onSetMixTranscodingConfig code:" + code + ", msg:" + msg);
        }

        public void onError(int code, String msg, Bundle extraInfo) {
            TXCLog.d(TAG, "TUILivePusherObserver onError code:" + code + ", msg:" + msg);
        }

        public void onWarning(int code, String msg, Bundle extraInfo) {
            TXCLog.d(TAG, "TUILivePusherObserver onWarning code:" + code + ", msg:" + msg);
        }

        @Override
        public void onCaptureFirstVideoFrame() {
            TXCLog.d(TAG, "TUILivePusherObserver onCaptureFirstVideoFrame");
            super.onCaptureFirstVideoFrame();
        }
    }


    class TUILivePlayerObserver extends V2TXLivePlayerObserver {
        public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
            TXCLog.d(TAG, "TUILivePlayerObserver onError code:" + code + ", msg:" + msg);
        }

        public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
            TXCLog.d(TAG, "TUILivePlayerObserver onWarning code:" + code + ", msg:" + msg);
        }
    }

}
