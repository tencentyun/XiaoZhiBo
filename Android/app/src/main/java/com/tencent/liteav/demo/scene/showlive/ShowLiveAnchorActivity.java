package com.tencent.liteav.demo.scene.showlive;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.services.room.bean.http.ShowLiveCosInfo;
import com.tencent.liteav.demo.services.room.callback.ActionCallback;
import com.tencent.liteav.demo.utils.URLUtils;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.view.ConfirmDialogFragment;
import com.tencent.liteav.demo.scene.showlive.view.ShowAnchorFunctionView;
import com.tencent.liteav.demo.scene.showlive.view.ShowAnchorPreviewView;
import com.tencent.liteav.demo.services.RoomService;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;
import com.tencent.qcloud.tuikit.tuipusher.view.listener.TUIPusherViewListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.tencent.liteav.demo.scene.showlive.dialog.SelectPhotoDialog.CODE_CAMERA;
import static com.tencent.liteav.demo.scene.showlive.dialog.SelectPhotoDialog.CODE_GALLERY;
import static com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

/**
 * 秀场直播 - 主播页面
 * <p>
 * 推流逻辑主要依赖TUIPusher组建中的{@link TUIPusherView} 实现
 * </p>
 */
public class ShowLiveAnchorActivity extends AppCompatActivity {
    private static final String TAG       = ShowLiveAnchorActivity.class.getSimpleName();
    public static final  String HMAC_SHA1 = "HmacSHA1";

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
    private ShowLiveCosInfo        mCosInfo;

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
        setContentView(R.layout.app_show_live_anchor_activity);
        mTUIPusherView = findViewById(R.id.anchor_pusher_view);
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
        if (isFinishing()) {
            return;
        }
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
        if (isFinishing()) {
            return;
        }
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
        if (isFinishing()) {
            return;
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CODE_CAMERA: {
                // 拍照获得图片
                Uri uri = FileProvider.getUriForFile(this,
                        "com.tencent.liteav.demo.fileprovider",
                        new File(getExternalCacheDir(), "xiaozhibo_avatar.jpg"));
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    uploadAvatar(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
            case CODE_GALLERY: {
                // 相册获取图片
                try {
                    Uri imageUri = data.getData();
                    if (imageUri != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        uploadAvatar(bitmap);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }

    public static String bytesToHexStr(byte[] bytes) {
        StringBuilder hexStr = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexStr.append(hex);
        }
        return hexStr.toString();
    }

    /**
     * HMAC加密算法
     *
     * @param input     待加密字符
     * @param key       加密的key
     * @param algorithm 算法类型如sha1/sha256
     * @return 加密结果
     */
    public static String encryptHMAC(String input, String key, String algorithm) {
        String cipher = "";
        try {
            byte[] data = key.getBytes(StandardCharsets.UTF_8);
            //根据给定的字节数组构造一个密钥，第二个参数指定一个密钥的算法名称，生成HmacSHA1专属密钥
            SecretKey secretKey = new SecretKeySpec(data, algorithm);

            //生成一个指定Mac算法的Mac对象
            Mac mac = Mac.getInstance(algorithm);
            //用给定密钥初始化Mac对象
            mac.init(secretKey);
            byte[] text = input.getBytes(StandardCharsets.UTF_8);
            byte[] encryptByte = mac.doFinal(text);
            cipher = bytesToHexStr(encryptByte);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    /**
     * SHA加密算法
     *
     * @param data      待加密数据
     * @param algorithm 加密算法
     * @return 加密结果
     * @throws Exception 加密异常
     */
    public static String sha(byte[] data, String algorithm) throws Exception {
        // 1. 根据算法名称获实现了算法的加密实例
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        // 2. 加密数据, 计算数据的哈希值
        byte[] cipher = digest.digest(data);
        // 3. 将结果转换为十六进制小写
        return bytesToHexStr(cipher);
    }

    private String getCosSignature(String keyTime, String secretKey, String policy) throws Exception {
        // 1. 生成 SignKey
        String signKey = encryptHMAC(keyTime, secretKey, HMAC_SHA1);
        // 2. 生成 StringToSign
        Log.i("getCosInfo", "signKey " + signKey + "");
        String stringToSign = null;
        stringToSign = sha(policy.getBytes(StandardCharsets.UTF_8), "SHA-1");
        Log.i("getCosInfo", "stringToSign " + stringToSign + "");
        // 3. 生成 Signature
        return encryptHMAC(stringToSign, signKey, HMAC_SHA1);
    }

    private String getCosPolicy(String expirationDate, String secretId, String keyTime) {
        String cosPolicy = "{\n"
                + "  \"expiration\": \"" + expirationDate + "\",\n"
                + "  \"conditions\": [\n"
                + "    { \"q-sign-algorithm\": \"sha1\" },\n"
                + "    { \"q-ak\": \"" + secretId + "\" },\n"
                + "    { \"q-sign-time\": \"" + keyTime + "\" }\n"
                + "  ]\n"
                + "}";
        return cosPolicy;
    }

    private Map<String, Object> getCosParams(ShowLiveCosInfo cosInfo) {
        Map<String, Object> param = new HashMap<>();
        param.put("x-cos-security-token", cosInfo.credential.credentials.sessionToken);
        param.put("q-sign-algorithm", "sha1");
        param.put("q-ak", cosInfo.credential.credentials.tmpSecretId);
        param.put("q-key-time", cosInfo.getKeyTime());
        param.put("key", cosInfo.fileName + ".jpg");
        param.put("success_action_status", 200);
        long timeStamp = ((long) cosInfo.credential.expiredTime) * 1000;
        Date date = new Date(timeStamp);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String nowAsISO = df.format(date);
        // 构造策略
        String policy = getCosPolicy(nowAsISO, cosInfo.credential.credentials.tmpSecretId, cosInfo.getKeyTime());
        param.put("policy", Base64.encodeToString(policy.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP));
        try {
            param.put("q-signature", getCosSignature(cosInfo.getKeyTime(),
                    cosInfo.credential.credentials.tmpSecretKey, policy));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return param;
    }

    public void uploadAvatar(Bitmap bitmap) {
        mCosInfo = mShowAnchorPreviewView.getCosInfo();
        String uploadURL = "https://" + mCosInfo.bucket + ".cos." + mCosInfo.region + ".myqcloud.com/";
        // 构造头像地址
        final String avatarURL = uploadURL + mCosInfo.fileName + ".jpg";
        RoomService.getInstance(this).uploadRoomAvatar(bitmap, uploadURL, null, getCosParams(mCosInfo),
                new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "uploadRoomAvatar success!");
                        ProfileManager.getInstance().setAvatar(avatarURL, new ProfileManager.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                mShowAnchorPreviewView.setCoverImage(avatarURL);
                                mShowAnchorFunctionView.refreshAvatar();
                                Log.i(TAG, "IM setAvatar success!");
                            }

                            @Override
                            public void onFailed(int code, String msg) {
                                Log.i(TAG, "IM setAvatar failed! code:" + code + ",msg:" + msg);
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code, String msg) {
                        Log.i(TAG, "uploadRoomAvatar failed! code:" + code + ",msg:" + msg);
                    }
                });
    }
}
