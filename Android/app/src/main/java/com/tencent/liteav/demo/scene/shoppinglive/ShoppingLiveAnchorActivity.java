package com.tencent.liteav.demo.scene.shoppinglive;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.view.ConfirmDialogFragment;
import com.tencent.liteav.demo.scene.shoppinglive.view.ShoppingAnchorFunctionView;
import com.tencent.liteav.demo.services.RoomService;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;
import com.tencent.qcloud.tuikit.tuipusher.view.listener.TUIPusherViewListener;

/**
 * 联系人选择Activity，可以通过此界面搜索已注册用户，并发起通话；
 */
public class ShoppingLiveAnchorActivity extends AppCompatActivity {
    private static final String TAG = ShoppingLiveAnchorActivity.class.getSimpleName();

    private TUIPusherView              mTUIPusherView;
    private ShoppingAnchorFunctionView mShoppingAnchorFunctionView;
    private ConfirmDialogFragment      mPKDialog;
    private ConfirmDialogFragment      mLinkDialog;
    private boolean                    mIsEnterRoom = false;

    private void initFunctionView() {
        mShoppingAnchorFunctionView = new ShoppingAnchorFunctionView(this);
        mShoppingAnchorFunctionView.setTUIPusherView(mTUIPusherView);
        mTUIPusherView.addView(mShoppingAnchorFunctionView);
        mShoppingAnchorFunctionView.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        mTUIPusherView = new TUIPusherView(this);
        setContentView(mTUIPusherView);
        initFunctionView();
        mIsEnterRoom = false;
        initData();
        mTUIPusherView.setTUIPusherViewListener(new TUIPusherViewListener() {
            @Override
            public void onPushStarted(TUIPusherView pushView, String url) {
                mShoppingAnchorFunctionView.setVisibility(View.VISIBLE);
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
                UserModel userModel = UserModelManager.getInstance().getUserModel();
                RoomService.getInstance(ShoppingLiveAnchorActivity.this).createRoom(
                        userModel.userId,
                        HttpRoomManager.TYPE_MLVB_SHOPPING_LIVE,
                        userModel.userName, "",
                        new CommonCallback() {
                            @Override
                            public void onCallback(int code, String msg) {
                                Log.d(TAG, " RoomManager.getInstance().createRoom() code:" + code + ", msg:" + msg);
                                if (code == 0) {
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

            }

            @Override
            public void onCancelPKRequest(TUIPusherView pusherView) {
                if (mShoppingAnchorFunctionView != null) {
                    mShoppingAnchorFunctionView.setmButtonPKState(ShoppingAnchorFunctionView.PKState.PK);
                }
                dismissPKDialog();
            }

            @Override
            public void onStartPK(TUIPusherView pusherView) {
                if (mShoppingAnchorFunctionView != null) {
                    mShoppingAnchorFunctionView.setmButtonPKState(ShoppingAnchorFunctionView.PKState.STOP);
                }
            }

            @Override
            public void onStopPK(TUIPusherView pusherView) {
                dismissPKDialog();
                if (mShoppingAnchorFunctionView != null) {
                    mShoppingAnchorFunctionView.setmButtonPKState(ShoppingAnchorFunctionView.PKState.PK);
                }
            }

            @Override
            public void onPKTimeout(TUIPusherView pusherView) {
                dismissPKDialog();
                if (mShoppingAnchorFunctionView != null) {
                    mShoppingAnchorFunctionView.setmButtonPKState(ShoppingAnchorFunctionView.PKState.PK);
                }
            }

            @Override
            public void onReceiveJoinAnchorRequest(TUIPusherView pushView, String userId, ResponseCallback callback) {
                showLinkDialog(userId, callback);
            }

            @Override
            public void onCancelJoinAnchorRequest(TUIPusherView pusherView) {
                dismissLinkDialog();
            }

            @Override
            public void onStartJoinAnchor(TUIPusherView pusherView) {

            }

            @Override
            public void onStopJoinAnchor(TUIPusherView pusherView) {
                dismissLinkDialog();
            }

            @Override
            public void onJoinAnchorTimeout(TUIPusherView pusherView) {
                dismissLinkDialog();
            }
        });
    }

    private void initData() {
        String pushUrl = "trtc://cloud.tencent.com/push/"
                + TUILogin.getUserId()
                + "?sdkappid="
                + TUILogin.getSdkAppId()
                + "&userid="
                + TUILogin.getUserId()
                + "&usersig="
                + TUILogin.getUserSig();
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
        mPKDialog.show(getFragmentManager(), "confirm_fragment");
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
        mLinkDialog.show(getFragmentManager(), "confirm_fragment");
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
        if (mIsEnterRoom) {
            UserModel userModel = UserModelManager.getInstance().getUserModel();
            RoomService.getInstance(ShoppingLiveAnchorActivity.this).destroyRoom(
                    userModel.userId,
                    HttpRoomManager.TYPE_MLVB_SHOPPING_LIVE,
                    new CommonCallback() {
                        @Override
                        public void onCallback(int code, String msg) {
                            Log.d(TAG, "RoomService destroyRoom code:" + code + ", msg:" + msg);
                        }
                    });
        }
    }
}
