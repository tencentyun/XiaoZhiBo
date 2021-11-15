package com.tencent.qcloud.tuikit.tuibeauty.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.qcloud.tuikit.tuibeauty.R;

/**
 * 美颜功能展开按钮
 */
public class TUIBeautyButton extends FrameLayout {
    private static final String TAG = "TUIBeautyButton";

    private Context         mContext;
    private TXBeautyManager mBeautyManager;
    private TUIBeautyView   mBeautyPanel;

    public TUIBeautyButton(Context context) {
        super(context);
    }

    public TUIBeautyButton(Context context, TXBeautyManager beautyManager) {
        this(context);
        this.mContext = context;
        this.mBeautyManager = beautyManager;
        initView(context);
    }

    private void initView(final Context context) {
        View baseView = LayoutInflater.from(context).inflate(R.layout.tuibeauty_view_extention, this);

        mBeautyPanel = new TUIBeautyView(context, mBeautyManager);
        findViewById(R.id.iv_link_dialog).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBeautyPanel.isShowing()) {
                    mBeautyPanel.show();
                }
            }
        });
    }
}
