package com.tencent.qcloud.tuikit.tuiplayer.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuikit.tuiplayer.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一管理跨TUI组建通信的View
 */
public class ContainerView extends FrameLayout {
    private static final String TAG = "ContainerView";

    private View           mViewRoot;
    private ImageView      mImageLink;
    private RelativeLayout mLayoutGift;
    private RelativeLayout mLayoutLike;
    private RelativeLayout mLayoutGiftShow;
    private RelativeLayout mLayoutBarrage;
    private RelativeLayout mLayoutBarrageShow;
    private int            mIconWidth;
    private int            mIconHeight;
    private OnLinkListener mListener;

    public ContainerView(@NonNull Context context) {
        this(context, null);
    }

    public ContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.tuiplayer_container_view, this, true);
        mImageLink = mViewRoot.findViewById(R.id.iv_link);
        mLayoutBarrage = mViewRoot.findViewById(R.id.rl_barrage);
        mLayoutBarrageShow = mViewRoot.findViewById(R.id.rl_barrage_show);
        mLayoutGift = mViewRoot.findViewById(R.id.rl_gift);
        mLayoutLike = mViewRoot.findViewById(R.id.rl_like);
        mLayoutGiftShow = mViewRoot.findViewById(R.id.rl_gift_show);
        mIconWidth = dip2px(44);
        mIconHeight = dip2px(44);
        initExtentionView();
    }

    public void setListener(OnLinkListener mListener) {
        this.mListener = mListener;
    }

    private void initExtentionView() {
        mImageLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onLink();
                }
            }
        });
    }

    public void setGroupId(String groupId) {
        //TODO 礼物
        HashMap<String, Object> giftParaMap = new HashMap<>();
        giftParaMap.put("context", getContext());
        giftParaMap.put("groupId", groupId);
        Map<String, Object> giftRetMap = TUICore.getExtensionInfo("com.tencent.qcloud.tuikit.tuigift.core.TUIGiftExtension", giftParaMap);
        if (giftRetMap != null && giftRetMap.size() > 0) {
            Object giftSendView = giftRetMap.get("TUIExtensionView");
            if (giftSendView != null && giftSendView instanceof View) {
                setGiftView((View) giftSendView);
                TXCLog.d(TAG, "TUIGift TUIExtensionView getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIGift TUIExtensionView getExtensionInfo not find");
            }

            Object giftDisplayView = giftRetMap.get("TUIGiftPlayView");
            if (giftDisplayView != null && giftDisplayView instanceof View) {
                setGiftShowView((View) giftDisplayView);
                TXCLog.d(TAG, "TUIGift TUIGiftPlayView getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIGift TUIGiftPlayView getExtensionInfo not find");
            }

            Object likeView = giftRetMap.get("TUILikeButton");
            if (likeView != null && likeView instanceof View) {
                setLikeView((View) likeView);
                TXCLog.d(TAG, "TUIGift TUILikeButton getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIGift TUILikeButton getExtensionInfo not find");
            }
        } else {
            TXCLog.d(TAG, "TUIGift getExtensionInfo null");
        }

        //TODO 弹幕
        HashMap<String, Object> barrageParaMap = new HashMap<>();
        barrageParaMap.put("context", getContext());
        barrageParaMap.put("groupId", groupId);
        Map<String, Object> barrageRetMap = TUICore.getExtensionInfo("com.tencent.qcloud.tuikit.tuibarrage.core.TUIBarrageExtension", barrageParaMap);
        if (barrageRetMap != null && barrageRetMap.size() > 0) {
            Object barrageSendView = barrageRetMap.get("TUIBarrageButton");
            if (barrageSendView != null && barrageSendView instanceof View) {
                setBarrageView((View) barrageSendView);
                TXCLog.d(TAG, "TUIBarrage barrageSendView getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIBarrage barrageSendView getExtensionInfo not find");
            }

            Object barrageDisplayView = barrageRetMap.get("TUIBarrageDisplayView");
            if (barrageDisplayView != null && barrageDisplayView instanceof View) {
                setBarrageShowView((View) barrageDisplayView);
                TXCLog.d(TAG, "TUIBarrage TUIBarrageDisplayView getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIBarrage TUIBarrageDisplayView getExtensionInfo not find");
            }
        } else {
            TXCLog.d(TAG, "TUIBarrage getExtensionInfo null");
        }
    }

    public void setBarrageView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mIconWidth, mIconHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayoutBarrage.addView(view, params);
    }

    public void setBarrageShowView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLayoutBarrageShow.addView(view, params);
    }

    public void setLikeView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mIconWidth, mIconHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayoutLike.addView(view);
    }

    public void setGiftView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mIconWidth, mIconHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayoutGift.addView(view, params);
    }

    public void setGiftShowView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLayoutGiftShow.addView(view, params);
    }

    public void setLinkImage(int resId) {
        if (resId > 0) {
            mImageLink.setBackgroundResource(resId);
        }
    }

    public void setLinkVisible(int visible) {
        mImageLink.setVisibility(visible);
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public interface OnLinkListener {
        void onLink();
    }
}

