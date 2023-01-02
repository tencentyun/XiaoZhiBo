package com.tencent.liteav.showlive.ui;

import static com.tencent.liteav.debug.GenerateTestUserSig.XMAGIC_LICENSE_KEY;
import static com.tencent.liteav.debug.GenerateTestUserSig.XMAGIC_LICENSE_URL;
import static com.tencent.liteav.showlive.model.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

import android.content.Context;
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

import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.RoomService;
import com.tencent.liteav.showlive.model.services.room.callback.CommonCallback;
import com.tencent.liteav.showlive.model.services.room.im.impl.IMRoomManager;
import com.tencent.liteav.showlive.ui.utils.URLUtils;
import com.tencent.liteav.showlive.ui.view.ConfirmDialogFragment;
import com.tencent.liteav.showlive.ui.view.ShowAnchorFunctionView;
import com.tencent.liteav.showlive.ui.view.ShowAnchorPreviewView;
import com.tencent.qcloud.tuicore.TUIConstants;
import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.interfaces.TUILoginListener;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;
import com.tencent.qcloud.tuikit.tuipusher.view.listener.TUIPusherViewListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 秀场直播 - 主播页面
 * <p>
 * 推流逻辑主要依赖TUIPusher组建中的{@link TUIPusherView} 实现
 * </p>
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
    private long                   mSecond      = 0;       // 开播的时间，单位为秒

    private void initFunctionView() {
        mShowAnchorFunctionView = findViewById(R.id.anchor_function_view);
        mShowAnchorFunctionView.setTUIPusherView(mTUIPusherView);
        mShowAnchorFunctionView.setRoomId(getRoomId(TUILogin.getUserId()));
        mShowAnchorFunctionView.setListener(new ShowAnchorFunctionView.OnFunctionListener() {
            @Override
            public void onClose() {
                showCloseDialog();
            }
        });
        mShowAnchorFunctionView.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initStatusBar();
        setContentView(R.layout.showlive_anchor_activity);
        Map<String, Object> map = new HashMap<>();
        map.put(TUIConstants.TUIBeauty.PARAM_NAME_CONTEXT, ShowLiveAnchorActivity.this);
        map.put(TUIConstants.TUIBeauty.PARAM_NAME_LICENSE_URL, XMAGIC_LICENSE_URL);
        map.put(TUIConstants.TUIBeauty.PARAM_NAME_LICENSE_KEY, XMAGIC_LICENSE_KEY);
        TUICore.callService(TUIConstants.TUIBeauty.SERVICE_NAME, TUIConstants.TUIBeauty.METHOD_INIT_XMAGIC, map);
        mTUIPusherView = findViewById(R.id.anchor_pusher_view);
        initPreviewView();
        initFunctionView();
        initData();
        mIsEnterRoom = false;
        mTUIPusherView.setTUIPusherViewListener(new TUIPusherViewListener() {
            @Override
            public void onPushStarted(TUIPusherView pushView, String url) {
                mShowAnchorFunctionView.setVisibility(View.VISIBLE);
                showAlertUserLiveTips();
            }

            @Override
            public void onPushStopped(TUIPusherView pushView, String url) {

            }

            @Override
            public void onPushEvent(TUIPusherView pusherView, TUIPusherEvent event, String message) {

            }

            @Override
            public void onClickStartPushButton(final TUIPusherView pushView, String url,
                                               final ResponseCallback callback) {
                if (TextUtils.isEmpty(mShowAnchorPreviewView.getRoomName())) {
                    mShowAnchorPreviewView.setRoomName(
                            (TextUtils.isEmpty(TUILogin.getNickName())
                                    ? TUILogin.getUserId() : TUILogin.getNickName())
                                    + getResources().getString(R.string.showlive_room));
                }
                RoomService.getInstance().createRoom(
                        getRoomId(TUILogin.getUserId()), TYPE_MLVB_SHOW_LIVE,
                        mShowAnchorPreviewView.getRoomName(),
                        TUILogin.getFaceUrl(),
                        new CommonCallback() {
                            @Override
                            public void onCallback(int code, String msg) {
                                Log.d(TAG, " RoomManager.getInstance().createRoom() code:" + code + ", msg:" + msg);
                                if (code == 0) {
                                    startTimer();
                                    mShowAnchorFunctionView.startRecordAnimation();
                                    mTUIPusherView.setGroupId(getRoomId(TUILogin.getUserId()));
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
                    ToastUtil.toastShortMessage(getString(R.string.showlive_anchor_reject_request));
                } else if (reason == 2) {
                    ToastUtil.toastShortMessage(getString(R.string.showlive_anchor_busy));
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
                ToastUtil.toastShortMessage(getString(R.string.showlive_pk_request_timeout));
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
                ToastUtil.toastShortMessage(getString(R.string.showlive_link_request_timeout));
            }
        });

        IMRoomManager.getInstance().setIMServiceListener(new IMRoomManager.IMServiceListener() {
            @Override
            public void onGroupDismissed(String groupId) {
                Log.e(TAG, "onGroupDismissed");
                mTUIPusherView.stop();
                if (!isFinishing()) {
                    showDestroyDialog();
                }
            }
        });
        TUILogin.addLoginListener(mTUILoginListener);
    }

    private void initPreviewView() {
        mShowAnchorPreviewView = new ShowAnchorPreviewView(ShowLiveAnchorActivity.this);
        mShowAnchorPreviewView.setTitle(getRoomId(TUILogin.getUserId()));
        mShowAnchorPreviewView.setCoverImage(TUILogin.getFaceUrl());
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
        if (isFinishing()) {
            return;
        }
        if (mPKDialog == null) {
            mPKDialog = new ConfirmDialogFragment();
            mPKDialog.setMessage(userId + getString(R.string.showlive_request_pk));
            mPKDialog.setNegativeText(getString(R.string.showlive_reject));
            mPKDialog.setNegativeClickListener(new ConfirmDialogFragment.NegativeClickListener() {
                @Override
                public void onClick() {
                    callback.response(false);
                    mPKDialog.dismiss();
                }
            });
            mPKDialog.setPositiveText(getString(R.string.showlive_accept));
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
        if (isFinishing()) {
            return;
        }
        if (mLinkDialog == null) {
            mLinkDialog = new ConfirmDialogFragment();
            mLinkDialog.setMessage(userId + getString(R.string.showlive_request_link));
            mLinkDialog.setNegativeText(getString(R.string.showlive_reject));
            mLinkDialog.setNegativeClickListener(new ConfirmDialogFragment.NegativeClickListener() {
                @Override
                public void onClick() {
                    callback.response(false);
                    mLinkDialog.dismiss();
                }
            });
            mLinkDialog.setPositiveText(getString(R.string.showlive_accept));
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
        if (isFinishing()) {
            return;
        }
        if (mDialogClose == null) {
            mDialogClose = new ConfirmDialogFragment();
            mDialogClose.setMessage(getString(R.string.showlive_close_room_tip));
            mDialogClose.setNegativeText(getString(R.string.showlive_wait));
            mDialogClose.setNegativeClickListener(new ConfirmDialogFragment.NegativeClickListener() {
                @Override
                public void onClick() {
                    mDialogClose.dismiss();
                }
            });
            mDialogClose.setPositiveText(getString(R.string.showlive_me_ok));
            mDialogClose.setPositiveClickListener(new ConfirmDialogFragment.PositiveClickListener() {
                @Override
                public void onClick() {
                    mDialogClose.dismiss();
                    if (mIsEnterRoom) {
                        RoomService.getInstance().destroyRoom(
                                getRoomId(TUILogin.getUserId()), TYPE_MLVB_SHOW_LIVE, new CommonCallback() {
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
        mTUIPusherView.stop();
        stopTimer();
        if (mShowAnchorFunctionView != null) {
            mShowAnchorFunctionView.stopRecordAnimation();
        }
        if (mIsEnterRoom) {
            RoomService.getInstance().destroyRoom(
                    getRoomId(TUILogin.getUserId()),
                    TYPE_MLVB_SHOW_LIVE,
                    new CommonCallback() {
                        @Override
                        public void onCallback(int code, String msg) {
                            Log.d(TAG, "RoomService destroyRoom code:" + code + ", msg:" + msg);
                        }
                    });
        }
        super.onDestroy();
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

    private void showAlertUserLiveTips() {
        if (!isFinishing()) {
            try {
                Class clz = Class.forName("com.tencent.liteav.privacy.util.RTCubeAppLegalUtils");
                Method method = clz.getDeclaredMethod("showAlertUserLiveTips", Context.class);
                method.invoke(null, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showDestroyDialog() {
        try {
            Class clz = Class.forName("com.tencent.liteav.privacy.util.RTCubeAppLegalUtils");
            Method method = clz.getDeclaredMethod("showRoomDestroyTips", Context.class);
            method.invoke(null, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    private TUILoginListener mTUILoginListener = new TUILoginListener() {
        @Override
        public void onKickedOffline() {
            Log.e(TAG, "onKickedOffline");
            mTUIPusherView.stop();
            finish();
        }
    };
}
