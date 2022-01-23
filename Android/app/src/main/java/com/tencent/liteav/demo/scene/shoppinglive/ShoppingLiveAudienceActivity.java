package com.tencent.liteav.demo.scene.shoppinglive;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.common.TCConstants;
import com.tencent.liteav.demo.services.RoomService;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.demo.utils.URLUtils;
import com.tencent.qcloud.tuikit.tuiplayer.view.listener.TUIPlayerViewListener;
import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;

import static com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOPPING_LIVE;

public class ShoppingLiveAudienceActivity extends AppCompatActivity {
    private static final String TAG = ShoppingLiveAudienceActivity.class.getSimpleName();

    private TUIPlayerView mTUIPlayerView;
    private String        mRoomName;
    private int           mRoomId;
    private boolean       mIsUserCNDPlay;
    private String        mAnchorId;
    private String        mAnchorName;
    private String        mCoverUrl;
    private String        mAvatarUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        initBundleData();
        mTUIPlayerView = new TUIPlayerView(this);
        setContentView(mTUIPlayerView);
        mTUIPlayerView.setTUIPlayerViewListener(new TUIPlayerViewListener() {
            @Override
            public void onPlayStarted(TUIPlayerView playView, String url) {
                ToastUtils.showShort("播放开始回调");
            }

            @Override
            public void onPlayStoped(TUIPlayerView playView, String url) {
                ToastUtils.showShort("播放结束回调");
            }

            @Override
            public void onPlayEvent(TUIPlayerView playView, TUIPlayerEvent event, String message) {
                ToastUtils.showShort("播放事件-> event:" + event + " ,message:" + message);
            }

            @Override
            public void onRejectJoinAnchorResponse(TUIPlayerView playView, int reason) {

            }
        });

        RoomService.getInstance(ShoppingLiveAudienceActivity.this).enterRoom(mRoomId,
                TYPE_MLVB_SHOPPING_LIVE, new CommonCallback() {
                    @Override
                    public void onCallback(int code, String msg) {
                        if (code == 0) {
                            String playUrl = URLUtils.generatePlayUrl(mRoomId + "", URLUtils.PlayType.WEBRTC);
                            mTUIPlayerView.startPlay(playUrl);
                        } else {
                            ToastUtils.showShort("加入群组失败");
                            finish();
                        }
                    }
                });

    }

    private void initBundleData() {
        mRoomName = getIntent().getStringExtra(TCConstants.ROOM_TITLE);
        mRoomId = getIntent().getIntExtra(TCConstants.GROUP_ID, 0);
        mIsUserCNDPlay = getIntent().getBooleanExtra(TCConstants.USE_CDN_PLAY, false);
        mAnchorId = getIntent().getStringExtra(TCConstants.PUSHER_ID);
        mAnchorName = getIntent().getStringExtra(TCConstants.PUSHER_NAME);
        mCoverUrl = getIntent().getStringExtra(TCConstants.COVER_PIC);
        mAvatarUrl = getIntent().getStringExtra(TCConstants.PUSHER_AVATAR);

    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTUIPlayerView.stopPlay();
        RoomService.getInstance(ShoppingLiveAudienceActivity.this).exitRoom(mRoomId, null);
    }
}
