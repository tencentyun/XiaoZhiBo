package com.tencent.liteav.showlive.ui.floatwindow;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.room.bean.RoomInfo;
import com.tencent.liteav.showlive.ui.ShowLiveAudienceActivity;
import com.tencent.liteav.showlive.ui.common.TCConstants;
import com.tencent.liteav.showlive.ui.utils.URLUtils;
import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;

import java.lang.reflect.Method;

public class FloatWindow implements IFloatWindowCallback {
    private static final String TAG = "FloatWindow";

    private Context mContext;

    private View          mRootView;
    private ImageView     mImageClose;
    private TUIPlayerView mTUIPlayerView;

    private WindowManager              mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private float    mStartX;   //最开始点击的X坐标
    private float    mStartY;   //最开始点击的Y坐标
    private float    mTouchX;   //开始移动时的X坐标
    private float    mTouchY;   //开始移动时的Y坐标
    private float    mCurX;     //X坐标
    private float    mCurY;     //Y坐标
    private int      mScreenWidth;
    private int      mScreenHeight;
    private boolean  mIsMove;
    private RoomInfo mRoomInfo;

    private static FloatWindow sInstance;

    public static boolean mIsShowing = false; //悬浮窗是否显示

    private OnCloseClickListener mOnCloseListener;

    int mOldX = 0;//原X
    int mOldY = 0;//原Y

    public static synchronized FloatWindow getInstance() {
        if (sInstance == null) {
            sInstance = new FloatWindow();
        }
        return sInstance;
    }

    @Override
    public void onAppBackground(boolean isBackground) {
        Log.d(TAG, "onAppBackground: isBackground = " + isBackground);
        if (isBackground) {
            show();
        } else {
            hide();
        }
    }

    public void setCloseClickListener(OnCloseClickListener listener) {
        mOnCloseListener = listener;
    }

    public String getRoomId() {
        if (mRoomInfo != null && !TextUtils.isEmpty(mRoomInfo.roomId)) {
            return mRoomInfo.roomId;
        }
        return "";
    }

    public TUIPlayerView getTUIPlayerView() {
        return mTUIPlayerView;
    }

    public void createDemoApplication(Context context, IFloatWindowCallback callback) {
        try {
            Class clz = Class.forName("com.tencent.liteav.demo.app.DemoApplication");
            Method method = clz.getMethod("setCallback", IFloatWindowCallback.class);
            Object obj = method.invoke(context.getApplicationContext(), callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Context context, RoomInfo roomInfo) {
        mContext = context;
        mRoomInfo = roomInfo;
        initLayoutParams();
        initView();
        mIsShowing = false;
        createDemoApplication(context, this);
    }

    public void showView(View view) {
        if (null != mWindowManager) {
            mWindowManager.addView(view, mLayoutParams);
        }
    }

    public void createView() {
        Log.d(TAG, "createView: mIsShowing = " + mIsShowing);
        if (!mIsShowing) {
            showView(mRootView);
            mIsShowing = true;
        }

    }

    public void show() {
        Log.d(TAG, "show: mIsShowing = " + mIsShowing);
        if (!mIsShowing && mRootView != null) {
            mRootView.setVisibility(View.VISIBLE);
            mIsShowing = true;
        }
    }

    public void hide() {
        Log.d(TAG, "hide: mIsShowing = " + mIsShowing);
        if (mIsShowing && mRootView != null) {
            mRootView.setVisibility(View.GONE);
            mIsShowing = false;
        }
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.showlive_view_flaotwindow, null);
        mRootView = view;
        mTUIPlayerView = new TUIPlayerView(mContext);
        mTUIPlayerView.updatePlayerUIState(TUIPlayerView.TUIPlayerUIState.TUIPLAYER_UISTATE_VIDEOONLY);
        mTUIPlayerView.setGroupId(getRoomId());
        String playUrl = URLUtils.generatePlayUrl(getRoomId(), URLUtils.PlayType.WEBRTC);
        mTUIPlayerView.startPlay(playUrl);
        ((ViewGroup) mRootView).addView(mTUIPlayerView);
        mImageClose = view.findViewById(R.id.iv_close_float_window);
        ((ViewGroup) mRootView).bringChildToFront(mImageClose);
        mTUIPlayerView.setOnTouchListener(new FloatingOnTouchListener());
        mImageClose.setOnTouchListener(new FloatingOnTouchListener());
        mTUIPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destroy();
                Intent intent = new Intent(mContext, ShowLiveAudienceActivity.class);
                intent.putExtra(TCConstants.ROOM_TITLE, mRoomInfo.roomName);
                intent.putExtra(TCConstants.GROUP_ID, Integer.valueOf(mRoomInfo.roomId));
                intent.putExtra(TCConstants.USE_CDN_PLAY, false);
                intent.putExtra(TCConstants.PUSHER_ID, mRoomInfo.ownerId);
                intent.putExtra(TCConstants.PUSHER_NAME, mRoomInfo.roomName);
                intent.putExtra(TCConstants.COVER_PIC, mRoomInfo.coverUrl);
                intent.putExtra(TCConstants.PUSHER_AVATAR, mRoomInfo.coverUrl);
                mContext.getApplicationContext().startActivity(intent);
            }
        });
        mImageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnCloseListener.close();
            }
        });
    }

    private void initLayoutParams() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;
        mLayoutParams.width = mScreenWidth / 3;
        mLayoutParams.height = mLayoutParams.width * 4 / 3;
        mLayoutParams.x = mScreenWidth - mLayoutParams.width - 20;
        mLayoutParams.y = mScreenHeight / 3;
    }

    public void destroy() {
        if (mWindowManager != null && mRootView != null) {
            Log.d(TAG, "destroy:  removeView ");
            mTUIPlayerView.stopPlay();
            ((ViewGroup) mRootView).removeView(mTUIPlayerView);
            mTUIPlayerView = null;
            mWindowManager.removeView(mRootView);
            mRootView = null;
            mWindowManager = null;
        }

        mIsShowing = false;
        Log.d(TAG, "destroy: WindowManager");

        createDemoApplication(mContext, null);
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsMove = false;
                    mStartX = (int) event.getRawX(); //初始点相对屏幕左上角的坐标
                    mStartY = (int) event.getRawY();
                    mTouchX = (int) event.getRawX(); //该值在move的时候变化
                    mTouchY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mCurX = event.getRawX();
                    mCurY = event.getRawY();
                    //更新悬浮窗口位置(跟手功能)
                    mLayoutParams.x += mCurX - mTouchX;
                    mLayoutParams.y += mCurY - mTouchY;
                    mWindowManager.updateViewLayout(mRootView, mLayoutParams);
                    //更新坐标
                    mTouchX = mCurX;
                    mTouchY = mCurY;
                    if (Math.abs(mCurX - mStartX) >= 5 || Math.abs(mCurY - mStartY) >= 5) {
                        mIsMove = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mCurX = event.getRawX();
                    mCurY = event.getRawY();
                    if ((Math.abs(mCurX - mStartX) >= 5 || Math.abs(mCurY - mStartY) >= 5) && mIsMove) {
                        if (mCurX < mScreenWidth / 2) {
                            startScrollLeft();
                        } else {
                            startScrollRight();
                        }
                    }
                    break;
                default:
                    break;
            }
            return mIsMove;
        }
    }

    //悬浮窗贴边动画,移动到左边
    public void startScrollLeft() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mCurX, 0).setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLayoutParams.x = (int) (mCurX * (1 - animation.getAnimatedFraction()));
                //防止悬浮窗上下越界
                calculateHeight();
                mWindowManager.updateViewLayout(mRootView, mLayoutParams);
            }
        });
        valueAnimator.start();
    }

    //悬浮窗贴边动画,移动到右边
    public void startScrollRight() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mCurX, mScreenWidth * 2 / 3).setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLayoutParams.x = (int) (mCurX + (mScreenWidth * 2 / 3 - mCurX) * animation.getAnimatedFraction());
                //防止悬浮窗上下越界
                calculateHeight();
                mWindowManager.updateViewLayout(mRootView, mLayoutParams);
            }
        });
        valueAnimator.start();
    }

    //计算高度,防止悬浮窗上下越界
    private void calculateHeight() {
        int height = mRootView.getHeight();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        //获取系统状态栏的高度
        int resourceId = mContext.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        int statusBarHeight = mContext.getResources().getDimensionPixelSize(resourceId);
        if (mLayoutParams.y < 0) {
            mLayoutParams.y = 0;
        } else if (mLayoutParams.y > (screenHeight - height - statusBarHeight)) {
            mLayoutParams.y = screenHeight - height - statusBarHeight;
        }
    }

    public interface OnCloseClickListener {
        void close();
    }
}
