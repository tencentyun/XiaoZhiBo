package com.tencent.liteav.demo.scene.showlive.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.scene.showlive.ShowLiveAudienceActivity;
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

    private float   mStartX;   //最开始点击的X坐标
    private float   mStartY;   //最开始点击的Y坐标
    private float   mCurX;     //X坐标
    private float   mCurY;     //Y坐标
    private int     mScreenWidth;
    private int     mScreenHeight;
    private boolean mIsMove;
    private String  mRoomId;

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
        if (!TextUtils.isEmpty(mRoomId)) {
            return mRoomId;
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

    public void init(Context context, String roomId, TUIPlayerView playerView) {
        mContext = context;
        mRoomId = roomId;
        initLayoutParams();
        initView(playerView);
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

    private void initView(TUIPlayerView playerView) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.app_view_flaotwindow, null);
        mRootView = view;
        mTUIPlayerView = playerView;
        mTUIPlayerView.updatePlayerUIState(TUIPlayerView.TUIPlayerUIState.TUIPLAYER_UISTATE_VIDEOONLY);
        ((ViewGroup) mRootView).addView(mTUIPlayerView);
        mImageClose = view.findViewById(R.id.iv_close_float_window);
        ((ViewGroup) mRootView).bringChildToFront(mImageClose);
        mTUIPlayerView.setOnTouchListener(new FloatingOnTouchListener());
        mImageClose.setOnTouchListener(new FloatingOnTouchListener());
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


    //点击事件
    private void click(int i) {
        if (i == R.id.iv_close_float_window) {
            mOnCloseListener.close();
        } else {
            Intent intent = new Intent(mContext, ShowLiveAudienceActivity.class);
            mContext.getApplicationContext().startActivity(intent);
        }
    }

    public void destroy() {
        if (mWindowManager != null && mRootView != null) {
            Log.d(TAG, "destroy:  removeView ");
            ((ViewGroup) mRootView).removeView(mTUIPlayerView);
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
            mCurX = mLayoutParams.x;
            mCurY = mLayoutParams.y;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsMove = false;
                    mOldX = (int) event.getRawX();
                    mOldY = (int) event.getRawY();
                    //获取初始位置
                    mStartX = (event.getRawX() - mLayoutParams.x);
                    mStartY = (event.getRawY() - mLayoutParams.y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mCurX = event.getRawX();
                    mCurY = event.getRawY();
                    updateViewPosition();//更新悬浮窗口位置
                    if (Math.abs(mCurX - mOldX) <= 5 && Math.abs(mCurY - mOldY) <= 5) {
                    } else {
                        mIsMove = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mCurX = event.getRawX();
                    mCurY = event.getRawY();
                    //若位置变动不大,默认为点击
                    if (Math.abs(mCurX - mOldX) <= 5 && Math.abs(mCurY - mOldY) <= 5 && !mIsMove) {
                        click(v.getId());
                    }
                    move();
                    mOldX = (int) event.getRawX();
                    mOldY = (int) event.getRawY();
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    /**
     * 更新悬浮窗口位置
     */
    private void updateViewPosition() {
        mLayoutParams.x = (int) (mCurX - mStartX);
        mLayoutParams.y = (int) (mCurY - mStartY);
        if (mWindowManager != null) {
            mWindowManager.updateViewLayout(mRootView, mLayoutParams);
        }
    }


    public void move() {
        if (mHandler == null || mWindowManager == null) {
            return;
        }
        for (int i = 0; i < mWindowManager.getDefaultDisplay().getWidth(); i++) {
            //一毫秒更新一次，直到达到边缘了
            mHandler.sendEmptyMessageDelayed(i, 300);
        }
        mWindowManager.updateViewLayout(mRootView, mLayoutParams);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            moveToBeside();
        }
    };

    //靠边停驻
    private void moveToBeside() {
        if (!mIsShowing) {
            return;
        }
        if (mLayoutParams.x < 0) {
            mLayoutParams.x = 0;
        } else if (mLayoutParams.x + mLayoutParams.width > mScreenWidth) {
            mLayoutParams.x = mScreenWidth - mLayoutParams.width;
        }
        if (mLayoutParams.y < 0) {
            mLayoutParams.y = 0;
        } else if (mLayoutParams.y + mLayoutParams.height > mScreenHeight) {
            mLayoutParams.y = mScreenHeight - mLayoutParams.height;
        }
        if (mWindowManager != null) {
            mWindowManager.updateViewLayout(mRootView, mLayoutParams);
        }
    }

    public interface OnCloseClickListener {
        void close();
    }
}
