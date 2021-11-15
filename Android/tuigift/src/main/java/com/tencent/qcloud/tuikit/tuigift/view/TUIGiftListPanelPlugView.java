package com.tencent.qcloud.tuikit.tuigift.view;


import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.qcloud.tuikit.tuigift.R;
import com.tencent.qcloud.tuikit.tuigift.model.TUIGiftModel;
import com.tencent.qcloud.tuikit.tuigift.presenter.TUIGiftPresenter;


/**
 * 礼物面板
 */
public class TUIGiftListPanelPlugView extends BottomSheetDialog {
    private Context              mContext;
    private LayoutInflater       mLayoutInflater;
    private TUIGiftListPanelView mPanelView;
    private TUIGiftPresenter     mPresenter;
    private String               mGroupId;

    public TUIGiftListPanelPlugView(Context context, String groupId) {
        super(context, R.style.TUIGiftListPanelViewTheme);
        mContext = context;
        setContentView(R.layout.tuigift_panel);
        this.mGroupId = groupId;
        init();
    }

    private void init() {
        mLayoutInflater = LayoutInflater.from(mContext);
        mPanelView = findViewById(R.id.gift_panel_view_pager);
        setCanceledOnTouchOutside(true);
    }

    public void show() {
        super.show();
        initGiftData();
    }

    /**
     * 初始化礼物面板
     */
    public void initGiftData() {
        if (mPresenter == null) {
            mPresenter = mPanelView.getPresenter(mGroupId);
        }
        mPresenter.initGiftData();
    }

    /**
     * 设置Listener处理发送礼物事件
     */
    public void setListener() {
        if (mPresenter == null) {
            mPresenter = mPanelView.getPresenter(mGroupId);
        }
        mPanelView.setListener(new TUIGiftListener() {
            @Override
            public void onSuccess(int code, String msg, TUIGiftModel giftModel) {
                if (mPresenter != null && mPresenter.getPlayView() != null) {
                    mPresenter.getPlayView().receiveGift(giftModel);
                }
            }

            @Override
            public void onFailed(int code, String msg) {

            }
        });

    }
}
