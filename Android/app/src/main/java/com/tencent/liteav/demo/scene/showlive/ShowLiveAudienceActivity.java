package com.tencent.liteav.demo.scene.showlive;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.demo.scene.showlive.view.ShowAudienceFunctionView;
import com.tencent.liteav.demo.utils.URLUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.common.utils.Utils;
import com.tencent.liteav.demo.common.view.ConfirmDialogFragment;
import com.tencent.liteav.demo.scene.showlive.floatwindow.FloatActivity;
import com.tencent.liteav.demo.scene.showlive.floatwindow.FloatWindow;
import com.tencent.liteav.demo.scene.showlive.floatwindow.PermissionListener;
import com.tencent.liteav.demo.scene.showlive.swipeplayer.ShowLiveAdapter;
import com.tencent.liteav.demo.scene.showlive.swipeplayer.ShowLiveLayoutManager;
import com.tencent.liteav.demo.services.room.bean.RoomInfo;
import com.tencent.liteav.demo.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.TCConstants;
import com.tencent.liteav.demo.services.RoomService;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;
import com.tencent.qcloud.tuikit.tuiplayer.view.listener.TUIPlayerViewListener;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

/**
 * 秀场直播 - 观众页面
 * <p>
 * 拉流逻辑主要依赖TUIPlayer组建中的{@link TUIPlayerView} 实现
 * </P>
 */
public class ShowLiveAudienceActivity extends AppCompatActivity {
    private static final String TAG = ShowLiveAudienceActivity.class.getSimpleName();

    private int                   mRoomId;
    private int                   mPlayerIndex;
    private int                   mCurrentRoomIndex;
    private String                mRoomName;
    private String                mAnchorId;
    private String                mAnchorName;
    private String                mRoomCoverUrl;
    private String                mAnchorAvatarUrl;
    private String                mCurrentRoomId = "";
    private boolean               mIsAnchorLeave = false;
    private boolean               mIsLinkMic     = false;
    private boolean               mIsInIMGroup   = false;
    private List<RoomInfo>        mRoomInfoList  = new ArrayList<>();
    private ShowLiveAdapter       mShowLiveAdapter;
    private ShowLiveLayoutManager mLiveLayoutManager;
    private RecyclerView          mRecycleLiveRoom;
    private ConfirmDialogFragment mDialogClose;
    private TUIPlayerView         mTUIPlayerView;
    private ViewGroup             mViewGroup;

    private static final int REASON_NORMAL = 1;     //正常拒绝PK
    private static final int REASON_BUSY   = 2;     //忙线中

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Utils.initStatusBar(this);
        setContentView(R.layout.app_show_audience_view);
        initBundleData();
        RoomInfo roomInfo = new RoomInfo(mRoomId + "", mRoomName, mAnchorId, mRoomCoverUrl);
        mRoomInfoList.add(roomInfo);
        initView();
    }

    private void initView() {
        initRecyclerView();
        FloatWindow.getInstance().setCloseClickListener(new FloatWindow.OnCloseClickListener() {
            @Override
            public void close() {
                ShowLiveAudienceActivity.this.finish();
                if (FloatWindow.mIsShowing) {
                    FloatWindow.getInstance().destroy();
                }
            }
        });
    }

    private void initRecyclerView() {
        mRecycleLiveRoom = findViewById(R.id.recycler_live_room);
        mShowLiveAdapter = new ShowLiveAdapter(this, mRoomInfoList);
        mLiveLayoutManager = new ShowLiveLayoutManager(this,
                OrientationHelper.VERTICAL, false);
        mRecycleLiveRoom.setLayoutManager(mLiveLayoutManager);
        mRecycleLiveRoom.setAdapter(mShowLiveAdapter);
        getRoomList();
        mLiveLayoutManager.setOnViewPagerListener(new ShowLiveLayoutManager.OnViewPagerListener() {
            @Override
            public void onPageRelease(boolean isNext, int position) {
                Log.d(TAG, "onPageRelease position:" + position);
                stopPlay(position);
            }

            @Override
            public void onPageSelected(int position, boolean isBottom) {
                Log.d(TAG, "onPageSelected position:" + position);
                startPlay(position);
                if (isBottom) {
                    getRoomList();
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


    /**
     * 刷新直播列表
     */
    private void getRoomList() {
        // 从后台获取房间列表
        RoomService.getInstance(this).getRoomList(TYPE_MLVB_SHOW_LIVE,
                HttpRoomManager.RoomOrderType.CREATE_UTC, new RoomInfoCallback() {
                    @Override
                    public void onCallback(int code, String msg, List<RoomInfo> list) {
                        if (ShowLiveAudienceActivity.this.isFinishing()) {
                            return;
                        }
                        if (code == 0) {
                            for (int i = 0; i < list.size(); i++) {
                                if (!mRoomInfoList.contains(list.get(i))) {
                                    mRoomInfoList.add(list.get(i));
                                }
                            }
                            mShowLiveAdapter.notifyDataSetChanged();
                        } else {
                            ToastUtils.showLong(getString(R.string.app_toast_obtain_list_failed, msg));
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    protected void onResume() {
        if (FloatWindow.mIsShowing) {
            FloatWindow.getInstance().destroy();
            if (FloatWindow.getInstance().getRoomId().equals(mRoomInfoList.get(mCurrentRoomIndex).roomId)) {
                mTUIPlayerView.updatePlayerUIState(TUIPlayerView.TUIPlayerUIState.TUIPLAYER_UISTATE_DEFAULT);
                mViewGroup.addView(mTUIPlayerView, mPlayerIndex);
            }
        }
        super.onResume();
    }

    private void showFloatWindow() {
        if (mViewGroup == null) {
            mViewGroup = (ViewGroup) mTUIPlayerView.getParent();
            mPlayerIndex = mViewGroup.indexOfChild(mTUIPlayerView);
        }
        mViewGroup.removeView(mTUIPlayerView);
        FloatWindow.getInstance().init(this, mRoomInfoList.get(mCurrentRoomIndex));
        FloatWindow.getInstance().createView();
    }

    private void startPlay(final int position) {
        mTUIPlayerView = mRecycleLiveRoom.getLayoutManager()
                .findViewByPosition(position).findViewById(R.id.recycler_item_player_view);
        final ShowAudienceFunctionView mShowAudienceFunctionView = mRecycleLiveRoom.getLayoutManager()
                .findViewByPosition(position).findViewById(R.id.recycler_item_function_view);
        mRoomId = Integer.parseInt(mRoomInfoList.get(position).ownerId);
        mShowAudienceFunctionView.setAnchorInfo(mRoomInfoList.get(position).coverUrl,
                mRoomInfoList.get(position).roomName, mRoomId);
        mShowAudienceFunctionView.setVisibility(View.VISIBLE);
        mShowAudienceFunctionView.setListener(new ShowAudienceFunctionView.OnFunctionListener() {
            @Override
            public void onClose() {
                exit();
            }
        });
        mTUIPlayerView.setGroupID(mRoomId + "");
        String playUrl = URLUtils.generatePlayUrl(mRoomId + "", URLUtils.PlayType.WEBRTC);
        mTUIPlayerView.startPlay(playUrl);
        mTUIPlayerView.pauseAudio();
        mTUIPlayerView.setTUIPlayerViewListener(new TUIPlayerViewListener() {
            @Override
            public void onPlayStarted(TUIPlayerView playView, String url) {
                mShowAudienceFunctionView.setVisibility(View.VISIBLE);
                mIsAnchorLeave = false;
            }

            @Override
            public void onPlayStoped(TUIPlayerView playView, String url) {
                ToastUtil.toastShortMessage(ShowLiveAudienceActivity.this.getResources()
                        .getString(R.string.app_anchor_room_close));
                View view = LayoutInflater.from(ShowLiveAudienceActivity.this)
                        .inflate(R.layout.app_show_live_anchor_leave, null);
                ImageView imageView = view.findViewById(R.id.iv_anchor_leave);
                ImageLoader.loadImage(ShowLiveAudienceActivity.this, imageView,
                        mRoomInfoList.get(position).coverUrl, R.drawable.app_bg_cover);
                playView.addView(view);
                mIsAnchorLeave = true;
            }

            @Override
            public void onPlayEvent(TUIPlayerView playView, TUIPlayerEvent event, String message) {
                if (event == TUIPlayerEvent.TUIPLAYER_EVENT_LINKMIC_START) {
                    mIsLinkMic = true;
                    mLiveLayoutManager.setCanScroll(false);
                } else if (event == TUIPlayerEvent.TUIPLAYER_EVENT_LINKMIC_STOP) {
                    mIsLinkMic = false;
                    mLiveLayoutManager.setCanScroll(true);
                }
            }

            @Override
            public void onRejectJoinAnchorResponse(TUIPlayerView playView, int reason) {
                if (reason == REASON_NORMAL) {
                    ToastUtil.toastShortMessage(ShowLiveAudienceActivity.this.getResources()
                            .getString(R.string.app_anchor_reject_request));
                } else if (reason == REASON_BUSY) {
                    ToastUtil.toastShortMessage(ShowLiveAudienceActivity.this.getResources()
                            .getString(R.string.app_anchor_busy));
                } else {
                    ToastUtil.toastShortMessage("error -> reason:" + reason);
                }
                mIsLinkMic = false;
                mLiveLayoutManager.setCanScroll(true);
            }
        });

        mCurrentRoomIndex = position;
        mTUIPlayerView.resumeVideo();
        mTUIPlayerView.resumeAudio();
        if (!mCurrentRoomId.equals(mRoomInfoList.get(position).ownerId)) {
            exitRoom();
            mCurrentRoomId = mRoomInfoList.get(position).ownerId;
            enterRoom();
        }
        mViewGroup = (ViewGroup) mTUIPlayerView.getParent();
    }

    private void enterRoom() {
        RoomService.getInstance(this).enterRoom(Integer.parseInt(mCurrentRoomId),
                TYPE_MLVB_SHOW_LIVE, new CommonCallback() {
                    @Override
                    public void onCallback(int code, String msg) {
                        Log.i(TAG, "enterRoom code:" + code + "msg:" + msg + "roomId:" + mCurrentRoomId);
                        if (code == 0) {
                            mIsInIMGroup = true;
                        } else {
                            ToastUtils.showShort(getString(R.string.app_join_group_fail));
                        }
                    }
                });
    }

    private void exitRoom() {
        if (!mIsInIMGroup) {
            return;
        }
        RoomService.getInstance(this).exitRoom(Integer.parseInt(mCurrentRoomId), new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                Log.i(TAG, "exitRoom code:" + code + "msg:" + msg + "roomId:" + mCurrentRoomId);
                if (code == 0) {
                    mIsInIMGroup = false;
                }
            }
        });
    }

    private void stopPlay(final int index) {
        mTUIPlayerView = mRecycleLiveRoom.getLayoutManager()
                .findViewByPosition(index).findViewById(R.id.recycler_item_player_view);
        mTUIPlayerView.pauseAudio();
    }

    @Override
    public void finish() {
        mTUIPlayerView.stopPlay();
        if (mIsInIMGroup) {
            RoomService.getInstance(this).exitRoom(Integer.parseInt(mRoomInfoList.get(mCurrentRoomIndex).ownerId),
                    null);
        }
        super.finish();
    }

    private void exit() {
        if (mIsLinkMic && !isFinishing()) {
            showCloseDialog();
            return;
        }
        if (mIsAnchorLeave) {
            finish();
            return;
        }
        if (!FloatWindow.mIsShowing) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                FloatActivity.request(this, new PermissionListener() {
                    @Override
                    public void onSuccess() {
                        ShowLiveAudienceActivity.this.finish();
                        showFloatWindow();
                    }

                    @Override
                    public void onFail() {
                        finish();
                    }
                });
            } else {
                ShowLiveAudienceActivity.this.finish();
                showFloatWindow();
            }
        }
    }

    private void showCloseDialog() {
        if (mDialogClose == null) {
            mDialogClose = new ConfirmDialogFragment();
            mDialogClose.setMessage(getString(R.string.app_show_live_link_mic_exit));
            mDialogClose.setNegativeText(getString(R.string.app_wait));
            mDialogClose.setNegativeClickListener(new ConfirmDialogFragment.NegativeClickListener() {
                @Override
                public void onClick() {
                    mDialogClose.dismiss();
                }
            });
            mDialogClose.setPositiveText(getString(R.string.app_me_ok));
            mDialogClose.setPositiveClickListener(new ConfirmDialogFragment.PositiveClickListener() {
                @Override
                public void onClick() {
                    mDialogClose.dismiss();
                    finish();
                }
            });
        }
        mDialogClose.show(getFragmentManager(), "audience_confirm_close");
    }
}
