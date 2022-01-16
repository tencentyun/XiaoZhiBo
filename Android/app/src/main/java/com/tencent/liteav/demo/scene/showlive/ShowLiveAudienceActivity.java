package com.tencent.liteav.demo.scene.showlive;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.common.utils.Utils;
import com.tencent.liteav.demo.scene.showlive.view.ShowAnchorFunctionView;
import com.tencent.liteav.demo.scene.showlive.view.ShowAudienceFunctionView;
import com.tencent.liteav.demo.scene.showlive.view.ShowAudienceFunctionView;
import com.tencent.liteav.demo.utils.URLUtils;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.TCConstants;
import com.tencent.liteav.demo.services.RoomService;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuiplayer.view.listener.TUIPlayerViewListener;
import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;

/**
 * 秀场直播 - 观众页面
 * <p>
 * 拉流逻辑主要依赖TUIPlayer组建中的{@link TUIPlayerView} 实现
 */
public class ShowLiveAudienceActivity extends AppCompatActivity {
    private static final String TAG = ShowLiveAudienceActivity.class.getSimpleName();

    private TUIPlayerView            mTUIPlayerView;
    private ShowAudienceFunctionView mShowAudienceFunctionView;

    private int    mRoomId;
    private String mRoomName;
    private String mAnchorId;
    private String mAnchorName;
    private String mRoomCoverUrl;
    private String mAnchorAvatarUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Utils.initStatusBar(this);
        initBundleData();

        mTUIPlayerView = new TUIPlayerView(this);
        setContentView(mTUIPlayerView);
        mTUIPlayerView.setTUIPlayerViewListener(new TUIPlayerViewListener() {
            @Override
            public void onPlayStarted(TUIPlayerView playView, String url) {
                mShowAudienceFunctionView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPlayStoped(TUIPlayerView playView, String url) {
                ToastUtil.toastShortMessage(getString(R.string.app_anchor_room_close));
                finish();
            }

            @Override
            public void onPlayEvent(TUIPlayerView playView, TUIPlayerEvent event, String message) {
                ToastUtils.showShort("onPlayEvent-> event:" + event + " ,message:" + message);
            }

            @Override
            public void onRejectJoinAnchorResponse(TUIPlayerView playView, int reason) {
                if (reason == 1) {
                    ToastUtil.toastShortMessage(getString(R.string.app_anchor_reject_request));
                } else if (reason == 2) {
                    ToastUtil.toastShortMessage(getString(R.string.app_anchor_busy));
                } else {
                    ToastUtil.toastShortMessage("error -> reason:" + reason);
                }
            }
        });

        initFunctionView();
        RoomService.getInstance(ShowLiveAudienceActivity.this).enterRoom(mRoomId, new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (code == 0) {
                    String playUrl = URLUtils.generatePlayUrl(mAnchorId, URLUtils.PlayType.WEBRTC);
                    mTUIPlayerView.startPlay(playUrl);
                } else {
                    ToastUtils.showShort(getString(R.string.app_join_group_fail));
                    finish();
                }
            }
        });

    }

    private void initBundleData() {
        mRoomName = getIntent().getStringExtra(TCConstants.ROOM_TITLE);
        mRoomId = getIntent().getIntExtra(TCConstants.GROUP_ID, 0);
        mAnchorId = getIntent().getStringExtra(TCConstants.PUSHER_ID);
        mAnchorName = getIntent().getStringExtra(TCConstants.PUSHER_NAME);
        mRoomCoverUrl = getIntent().getStringExtra(TCConstants.COVER_PIC);
        mAnchorAvatarUrl = getIntent().getStringExtra(TCConstants.PUSHER_AVATAR);

    }

    private void initFunctionView() {
        mShowAudienceFunctionView = new ShowAudienceFunctionView(this);
        mShowAudienceFunctionView.setTUIPlayerView(mTUIPlayerView);
        mShowAudienceFunctionView.setAnchorInfo(mAnchorAvatarUrl, mAnchorName, mRoomId);
        mShowAudienceFunctionView.setListener(new ShowAudienceFunctionView.OnFunctionListener() {
            @Override
            public void onClose() {
                finish();
            }
        });
        mTUIPlayerView.addView(mShowAudienceFunctionView);
        mShowAudienceFunctionView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTUIPlayerView.stopPlay();
        RoomService.getInstance(ShowLiveAudienceActivity.this).exitRoom(mRoomId, null);
    }
}
