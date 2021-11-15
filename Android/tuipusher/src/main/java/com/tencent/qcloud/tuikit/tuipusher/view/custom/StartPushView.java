package com.tencent.qcloud.tuikit.tuipusher.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuikit.tuipusher.R;

import java.util.HashMap;
import java.util.Map;

public class StartPushView extends FrameLayout {
    private static final String TAG = "StartPushView";

    private Button              mButtonStartPush;
    private View                mViewRoot;
    private OnStartPushListener mListener;
    private RelativeLayout      mLayoutBeauty;
    private int                 mIconWidth;
    private int                 mIconHeight;

    public StartPushView(@NonNull Context context) {
        this(context, null);
    }

    public StartPushView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.tuipusher_start_push_view, this, true);
        mButtonStartPush = mViewRoot.findViewById(R.id.btn_start_push);
        mLayoutBeauty = mViewRoot.findViewById(R.id.rl_beauty);
        mViewRoot.findViewById(R.id.iv_camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSwitchCamera();
                }
            }
        });
        mButtonStartPush.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickStartPush();
                }
            }
        });
        mIconWidth = dip2px(44);
        mIconHeight = dip2px(44);
    }

    public void initExtentionView(TXBeautyManager beautyManager) {
        HashMap<String, Object> beautyParaMap = new HashMap<>();
        beautyParaMap.put("context", getContext());
        beautyParaMap.put("beautyManager", beautyManager);
        Map<String, Object> beautyRetMap = TUICore.getExtensionInfo("com.tencent.qcloud.tuikit.tuibeauty.view.TUIBeautyButton", beautyParaMap);
        if (beautyRetMap != null && beautyRetMap.size() > 0) {
            Object object = beautyRetMap.get("TUIBeauty");
            if (object != null && object instanceof View) {
                setBeautyView((View) object);
                TXCLog.d(TAG, "TUIBeauty getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIBeauty getExtensionInfo not find");
            }
        } else {
            TXCLog.d(TAG, "TUIBeauty getExtensionInfo null");
        }
    }

    public void setBeautyView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mIconWidth, mIconHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayoutBeauty.addView(view, params);
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 点击开始 按钮调用
     *
     * @param listener
     */
    public void setListener(OnStartPushListener listener) {
        mListener = listener;
    }

    public interface OnStartPushListener {
        void onClickStartPush();

        void onSwitchCamera();
    }
}
