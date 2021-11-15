package com.tencent.liteav.demo.scene.showlive;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.utils.URLUtils;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.view.ConfirmDialogFragment;
import com.tencent.liteav.demo.scene.showlive.view.ShowAnchorFunctionView;
import com.tencent.liteav.demo.scene.showlive.view.ShowAnchorPreviewView;
import com.tencent.liteav.demo.services.RoomService;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;
import com.tencent.qcloud.tuikit.tuipusher.view.listener.TUIPusherViewListener;

import java.util.Timer;
import java.util.TimerTask;

import static com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

/**
 * 秀场直播 - 主播页面
 *
 * 推流逻辑主要依赖TUIPusher组建中的{@link TUIPusherView} 实现
 */
public class ShowLiveAnchorActivity extends AppCompatActivity {
    private static final String TAG = ShowLiveAnchorActivity.class.getSimpleName();

    private TUIPusherView          mTUIPusherView;
    private ShowAnchorFunctionView mShowAnchorFunctionView;
    private ShowAnchorPreviewView  mShowAnchorPreviewView;
    private ConfirmDialogFragment  mPKDialog;
    private ConfirmDialogFragment  mLinkDialog;
    private ConfirmDialogFragment  mDialogClose;
    private boolean                mIsEnterRoom = false;
    private Timer                  mBroadcastTimer;        // 定时的 Timer
    private BroadcastTimerTask     mBroadcastTimerTask;    // 定时任务
    private long                   mSecond      = 0;            // 开播的时间，单位为秒

    private void initFunctionView() {
        mShowAnchorFunctionView = new ShowAnchorFunctionView(this);
        mShowAnchorFunctionView.setTUIPusherView(mTUIPusherView);
        mShowAnchorFunctionView.setRoomId(getRoomId(TUILogin.getUserId()));
        mShowAnchorFunctionView.setListener(new ShowAnchorFunctionView.OnFunctionListener() {
            @Override
            public void onClose() {
                showCloseDialog();
            }
        });
        mTUIPusherView.addView(mShowAnchorFunctionView);
        mShowAnchorFunctionView.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initStatusBar();
        mTUIPusherView = new TUIPusherView(this);
        setContentView(mTUIPusherView);
        initPreviewView();
        initFunctionView();
        initData();
        mIsEnterRoom = false;
        mTUIPusherView.setTUIPusherViewListener(new TUIPusherViewListener() {
            @Override
            public void onPushStarted(TUIPusherView pushView, String url) {
                mShowAnchorFunctionView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPushStoped(TUIPusherView pushView, String url) {

            }

            @Override
            public void onPushEvent(TUIPusherView pusherView, TUIPusherEvent event, String message) {

            }

            @Override
            public void onClickStartPushButton(final TUIPusherView pushView, String url,
                                               final ResponseCallback callback) {
                final UserModel userModel = UserModelManager.getInstance().getUserModel();
                if (TextUtils.isEmpty(mShowAnchorPreviewView.getRoomName())) {
                    mShowAnchorPreviewView.setRoomName(
                            (TextUtils.isEmpty(userModel.userName)
                                    ? userModel.userId : userModel.userName)
                                    + getResources().getString(R.string.app_user_show_live_room));
                }
                RoomService.getInstance(ShowLiveAnchorActivity.this).createRoom(
                        getRoomId(userModel.userId), TYPE_MLVB_SHOW_LIVE,
                        mShowAnchorPreviewView.getRoomName(),
                        userModel.userAvatar,
                        new CommonCallback() {
                            @Override
                            public void onCallback(int code, String msg) {
                                Log.d(TAG, " RoomManager.getInstance().createRoom() code:" + code + ", msg:" + msg);
                                if (code == 0) {
                                    startTimer();
                                    mShowAnchorFunctionView.startRecordAnimation();
                                    mTUIPusherView.setGroupId(getRoomId(userModel.userId));
                                    mShowAnchorPreviewView.setVisibility(View.GONE);
                                    mIsEnterRoom = true;
                                    callback.response(true);
                                } else {

                                    mIsEnterRoom = false;
                                    callback.response(false);
                                }
                            }
                        });
            }

            @Override
            public void onReceivePKRequest(TUIPusherView pushView, String userId, ResponseCallback callback) {
                showPKDialog(userId, callback);
            }

            @Override
            public void onRejectPKResponse(TUIPusherView pusherView, int reason) {
                if (mShowAnchorFunctionView != null) {
                    mShowAnchorFunctionView.setmButtonPKState(ShowAnchorFunctionView.PKState.PK);
                }
                if (reason == 1) {
                    ToastUtil.toastShortMessage(getString(R.string.app_anchor_reject_request));
                } else if (reason == 2) {
                    ToastUtil.toastShortMessage(getString(R.string.app_anchor_busy));
                } else {
                    ToastUtil.toastShortMessage("error -> reason:" + reason);
                }
            }

            @Override
            public void onCancelPKRequest(TUIPusherView pusherView) {
                if (mShowAnchorFunctionView != null) {
                    mShowAnchorFunctionView.setmButtonPKState(ShowAnchorFunctionView.PKState.PK);
                }
                dismissPKDialog();
            }

            @Override
            public void onStartPK(TUIPusherView pusherView) {
                if (mShowAnchorFunctionView != null) {
                    mShowAnchorFunctionView.setmButtonPKState(ShowAnchorFunctionView.PKState.STOP);
                }
            }

            @Override
            public void onStopPK(TUIPusherView pusherView) {
                dismissPKDialog();
                if (mShowAnchorFunctionView != null) {
                    mShowAnchorFunctionView.setmButtonPKState(ShowAnchorFunctionView.PKState.PK);
                }
            }

            @Override
            public void onPKTimeout(TUIPusherView pusherView) {
                dismissPKDialog();
                if (mShowAnchorFunctionView != null) {
                    mShowAnchorFunctionView.setmButtonPKState(ShowAnchorFunctionView.PKState.PK);
                }
                ToastUtil.toastShortMessage(getString(R.string.app_pk_request_timeout));
            }

            @Override
            public void onReceiveJoinAnchorRequest(TUIPusherView pushView, String userId, ResponseCallback callback) {
                showLinkDialog(userId, callback);
            }

            @Override
            public void onCancelJoinAnchorRequest(TUIPusherView pusherView) {
                mShowAnchorFunctionView.setLink(false);
                dismissLinkDialog();
            }

            @Override
            public void onStartJoinAnchor(TUIPusherView pusherView) {
                mShowAnchorFunctionView.setLink(true);
            }

            @Override
            public void onStopJoinAnchor(TUIPusherView pusherView) {
                mShowAnchorFunctionView.setLink(false);
                dismissLinkDialog();
            }

            @Override
            public void onJoinAnchorTimeout(TUIPusherView pusherView) {
                mShowAnchorFunctionView.setLink(false);
                dismissLinkDialog();
                ToastUtil.toastShortMessage(getString(R.string.app_link_request_timeout));
            }
        });
    }

    private void initPreviewView() {
        mShowAnchorPreviewView = new ShowAnchorPreviewView(ShowLiveAnchorActivity.this);
        mShowAnchorPreviewView.setTitle(getRoomId(TUILogin.getUserId()));
        UserModel userModel = UserModelManager.getInstance().getUserModel();
        mShowAnchorPreviewView.setCoverImage(userModel.userAvatar);
        mTUIPusherView.addView(mShowAnchorPreviewView);
    }

    private void initData() {
        String pushUrl = URLUtils.generatePushUrl(TUILogin.getUserId(), URLUtils.PushType.RTC);
        mTUIPusherView.start(pushUrl);
    }


    private void dismissPKDialog() {
        if (mPKDialog != null && mPKDialog.isAdded()) {
            mPKDialog.dismiss();
        }
        mPKDialog = null;
    }

    private void showPKDialog(String userId, final TUIPusherViewListener.ResponseCallback callback) {
        if (mPKDialog == null) {
            mPKDialog = new ConfirmDialogFragment();
            mPKDialog.setMessage(userId + getString(R.string.app_request_pk));
            mPKDialog.setNegativeText(getString(R.string.app_reject));
            mPKDialog.setNegativeClickListener(new ConfirmDialogFragment.NegativeClickListener() {
                @Override
                public void onClick() {
                    callback.response(false);
                    mPKDialog.dismiss();
                }
            });
            mPKDialog.setPositiveText(getString(R.string.app_accept));
            mPKDialog.setPositiveClickListener(new ConfirmDialogFragment.PositiveClickListener() {
                @Override
                public void onClick() {
                    callback.response(true);
                    mPKDialog.dismiss();
                }
            });
        }
        mPKDialog.show(getFragmentManager(), "confirm_pk");
    }

    private void dismissLinkDialog() {
        if (mLinkDialog != null && mLinkDialog.isAdded()) {
            mLinkDialog.dismiss();
        }
        mLinkDialog = null;
    }

    private void showLinkDialog(String userId, final TUIPusherViewListener.ResponseCallback callback) {
        if (mLinkDialog == null) {
            mLinkDialog = new ConfirmDialogFragment();
            mLinkDialog.setMessage(userId + getString(R.string.app_request_link));
            mLinkDialog.setNegativeText(getString(R.string.app_reject));
            mLinkDialog.setNegativeClickListener(new ConfirmDialogFragment.NegativeClickListener() {
                @Override
                public void onClick() {
                    callback.response(false);
                    mLinkDialog.dismiss();
                }
            });
            mLinkDialog.setPositiveText(getString(R.string.app_accept));
            mLinkDialog.setPositiveClickListener(new ConfirmDialogFragment.PositiveClickListener() {
                @Override
                public void onClick() {
                    callback.response(true);
                    mLinkDialog.dismiss();
                }
            });
        }
        mLinkDialog.show(getFragmentManager(), "confirm_link");
    }

    private void showCloseDialog() {
        if (mDialogClose == null) {
            mDialogClose = new ConfirmDialogFragment();
            mDialogClose.setMessage(getString(R.string.app_close_room_tip));
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
                    if (mIsEnterRoom) {
                        UserModel userModel = UserModelManager.getInstance().getUserModel();
                        RoomService.getInstance(ShowLiveAnchorActivity.this).destroyRoom(
                                getRoomId(userModel.userId), TYPE_MLVB_SHOW_LIVE, new CommonCallback() {
                                    @Override
                                    public void onCallback(int code, String msg) {
                                        Log.d(TAG, "RoomService destroyRoom code:" + code + ", msg:" + msg);
                                        finish();
                                        mIsEnterRoom = false;
                                    }
                                });
                    } else {
                        finish();
                    }

                }
            });
        }
        mDialogClose.show(getFragmentManager(), "confirm_close");
    }

    @Override
    public void onBackPressed() {
        if (mIsEnterRoom) {
            showCloseDialog();
        } else {
            finish();
        }
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
        mTUIPusherView.stop();
        stopTimer();
        if (mShowAnchorFunctionView != null) {
            mShowAnchorFunctionView.stopRecordAnimation();
        }
        if (mIsEnterRoom) {
            UserModel userModel = UserModelManager.getInstance().getUserModel();
            RoomService.getInstance(ShowLiveAnchorActivity.this).destroyRoom(
                    getRoomId(userModel.userId),
                    TYPE_MLVB_SHOW_LIVE,
                    new CommonCallback() {
                        @Override
                        public void onCallback(int code, String msg) {
                            Log.d(TAG, "RoomService destroyRoom code:" + code + ", msg:" + msg);
                        }
                    });
        }
    }

    private void startTimer() {
        if (mBroadcastTimer == null) {
            mBroadcastTimer = new Timer(true);
            mBroadcastTimerTask = new BroadcastTimerTask();
            mBroadcastTimer.schedule(mBroadcastTimerTask, 1000, 1000);
        }
    }

    private void stopTimer() {
        if (null != mBroadcastTimer) {
            mBroadcastTimerTask.cancel();
        }
    }

    public String getRoomId(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return "";
        }
        return userId;
    }

    /**
     * 直播时间记时器类；
     */
    private class BroadcastTimerTask extends TimerTask {
        public void run() {
            ++mSecond;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mShowAnchorFunctionView.updateBroadcasterTimeUpdate(mSecond);
                }
            });
        }
    }

}
