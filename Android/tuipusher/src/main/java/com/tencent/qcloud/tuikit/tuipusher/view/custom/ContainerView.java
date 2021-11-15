package com.tencent.qcloud.tuikit.tuipusher.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuikit.tuipusher.R;

import java.util.HashMap;
import java.util.Map;

public class ContainerView extends FrameLayout {
    private static final String TAG = "ContainerView";

    private View           mViewRoot;
    private RelativeLayout mLayoutAudio;
    private RelativeLayout mLayoutBeauty;
    private RelativeLayout mLayoutBarrage;
    private RelativeLayout mLayoutBarrageShow;
    private RelativeLayout mLayoutGiftShow;
    private int            mIconWidth;
    private int            mIconHeight;

    public ContainerView(@NonNull Context context) {
        this(context, null);
    }

    public ContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.tuipusher_container_view, this, true);
        mLayoutAudio = mViewRoot.findViewById(R.id.rl_audio_effect);
        mLayoutBeauty = mViewRoot.findViewById(R.id.rl_beauty);
        mLayoutBarrage = mViewRoot.findViewById(R.id.rl_barrage);
        mLayoutBarrageShow = mViewRoot.findViewById(R.id.rl_barrage_show);
        mLayoutGiftShow = mViewRoot.findViewById(R.id.rl_gift_show);

        mIconWidth = dip2px(44);
        mIconHeight = dip2px(44);
    }

    public void initExtentionView(TXAudioEffectManager audioEffectManager, TXBeautyManager beautyManager) {
        HashMap<String, Object> audioParaMap = new HashMap<>();
        audioParaMap.put("context", getContext());
        audioParaMap.put("audioeffectmanager", audioEffectManager);
        Map<String, Object> audioRetMap = TUICore.getExtensionInfo("extension_audioeffect", audioParaMap);
        if (audioRetMap != null && audioRetMap.size() > 0) {
            Object object = audioRetMap.get("audioEffectExtension");
            if (object != null && object instanceof View) {
                setAudioView((View) object);
                TXCLog.d(TAG, "TUIAudio getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIAudio getExtensionInfo not find");
            }
        } else {
            TXCLog.d(TAG, "TUIAudio getExtensionInfo null");
        }

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

    public void setGroupId(String groupId) {
        //TODO 礼物
        HashMap<String, Object> giftParaMap = new HashMap<>();
        giftParaMap.put("context", getContext());
        giftParaMap.put("groupId", groupId);
        Map<String, Object> giftRetMap = TUICore.getExtensionInfo("com.tencent.qcloud.tuikit.tuigift.core.TUIGiftExtension", giftParaMap);
        if (giftRetMap != null && giftRetMap.size() > 0) {
            Object giftDisplayView = giftRetMap.get("TUIGiftPlayView");
            if (giftDisplayView != null && giftDisplayView instanceof View) {
                setGiftShowView((View) giftDisplayView);
                TXCLog.d(TAG, "TUIGift TUIGiftPlayView getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIGift TUIGiftPlayView getExtensionInfo not find");
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
                setBarrage((View) barrageSendView);
                TXCLog.d(TAG, "TUIBarrage barrageSendView getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIBarrage barrageSendView getExtensionInfo not find");
            }

            Object barrageDisplayView = barrageRetMap.get("TUIBarrageDisplayView");
            if (barrageDisplayView != null && barrageDisplayView instanceof View) {
                setBarrageShow((View) barrageDisplayView);
                TXCLog.d(TAG, "TUIBarrage TUIBarrageDisplayView getExtensionInfo success");
            } else {
                TXCLog.d(TAG, "TUIBarrage TUIBarrageDisplayView getExtensionInfo not find");
            }
        } else {
            TXCLog.d(TAG, "TUIBarrage getExtensionInfo null");
        }
    }

    public void setBarrage(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mIconWidth, mIconHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayoutBarrage.addView(view, params);
    }

    public void setBarrageShow(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLayoutBarrageShow.addView(view, params);
    }

    public void setBeautyView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mIconWidth, mIconHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayoutBeauty.addView(view, params);
    }

    public void setAudioView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mIconWidth, mIconHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayoutAudio.addView(view, params);
    }

    public void setGiftShowView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLayoutGiftShow.addView(view, params);
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

