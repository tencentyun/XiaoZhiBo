package com.tencent.qcloud.tuikit.tuigift.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuikit.tuigift.R;
import com.tencent.qcloud.tuikit.tuigift.model.TUIGiftConstants;
import com.tencent.qcloud.tuikit.tuigift.model.TUIGiftIMService;
import com.tencent.qcloud.tuikit.tuigift.model.TUIGiftModel;
import com.tencent.qcloud.tuikit.tuigift.view.TUIGiftListPanelView;
import com.tencent.qcloud.tuikit.tuigift.view.TUIGiftPlayView;

import java.util.List;

/**
 * 礼物Presenter
 */
public class TUIGiftPresenter {
    private static TUIGiftPresenter sInstance;

    private TUIGiftPlayView      mPlayView;
    private TUIGiftListPanelView mPanelView;
    private String               mGroupId;
    private TUIGiftIMService     mImService;
    private Context              mContext;
    private TUIGiftDataDownload  mGiftDataDownload;

    public TUIGiftPresenter() {
        mGiftDataDownload = new TUIGiftDataDownload();
        mGiftDataDownload.setGiftListQuery(new TUIGiftListQueryImpl());
    }

    public static synchronized TUIGiftPresenter getInstance() {
        if (sInstance == null) {
            sInstance = new TUIGiftPresenter();
        }
        return sInstance;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void initGiftPlayView(TUIGiftPlayView playView, String groupId) {
        this.mPlayView = playView;
        mGroupId = groupId;
        initIMService();
    }

    public void initGiftPanelView(TUIGiftListPanelView panelView, String groupId) {
        this.mPanelView = panelView;
        mGroupId = groupId;
        initIMService();
    }

    public TUIGiftPlayView getPlayView() {
        return mPlayView;
    }

    /**
     * 初始化IM
     */
    private void initIMService() {
        if (mImService == null) {
            mImService = new TUIGiftIMService(mGroupId);
            mImService.setPresenter(this);
        }
        mImService.setGroupId(mGroupId);
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    /**
     * 发送礼物消息
     *
     * @param giftModel 待发送礼物信息
     * @param callback  发送结果回调
     */
    public void sendGroupGiftMessage(final TUIGiftModel giftModel, final TUIGiftCallBack.GiftSendCallBack callback) {
        mImService.sendGroupGiftMessage(giftModel, new TUIGiftCallBack.ActionCallBack() {
            @Override
            public void onCallback(int code, String msg) {
                if (code != 0) {
                    callback.onFailed(code, msg);
                } else {
                    giftModel.extInfo.put(TUIGiftConstants.KEY_USER_NAME, mContext.getString(R.string.tuigift_me));
                    giftModel.extInfo.put(TUIGiftConstants.KEY_USER_AVATAR, TUILogin.getFaceUrl());
                    callback.onSuccess(code, msg, giftModel);
                }
            }
        });
    }

    /**
     * 接收礼物消息
     *
     * @param groupId   接收的群组groupId
     * @param giftModel 接收到的礼物信息
     */
    public void recvGroupGiftMessage(String groupId, TUIGiftModel giftModel) {
        if (mGroupId != null && mGroupId.equals(groupId)) {
            mPlayView.receiveGift(giftModel);
        }
    }

    /**
     * 初始化礼物面板
     */
    public void initGiftData() {
        mGiftDataDownload.queryGiftInfoList(new TUIGiftDataDownload.GiftQueryCallback() {
            @Override
            public void onQuerySuccess(final List<TUIGiftModel> giftInfoList) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mPanelView.setGiftModelSource(giftInfoList);
                        mPanelView.init(mGroupId);
                    }
                });

            }

            @Override
            public void onQueryFailed(String errorMsg) {
            }
        });
    }
}
