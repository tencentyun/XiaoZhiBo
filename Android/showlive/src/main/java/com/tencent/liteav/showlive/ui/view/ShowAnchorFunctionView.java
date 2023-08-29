package com.tencent.liteav.showlive.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.showlive.R;
import com.tencent.liteav.showlive.model.services.room.bean.AudienceInfo;
import com.tencent.liteav.showlive.model.services.room.bean.RoomInfo;
import com.tencent.liteav.showlive.model.services.room.im.impl.IMRoomManager;
import com.tencent.liteav.showlive.ui.dialog.HourRankDialog;
import com.tencent.liteav.showlive.ui.dialog.MoreActionDialog;
import com.tencent.liteav.showlive.ui.dialog.OnlineAudienceDialog;
import com.tencent.liteav.showlive.ui.utils.Utils;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShowAnchorFunctionView extends FrameLayout implements View.OnClickListener {
    private View               mViewRoot;
    private ImageView          mImagePK;
    private ImageView          mImageClose;
    private ImageView          mImageMore;
    private TUIPusherView      mTUIPusherView;
    private AnchorPKSelectView mViewPKAnchorList;      // 显示可PK主播的列表
    private ImageView          mImagesAnchorHead;      // 显示房间主播头像
    private ImageView          mImageRecordBall;       // 表明正在录制的红点球
    private TextView           mTextBroadcastTime;     // 显示已经开播的时间
    private ObjectAnimator     mAnimatorRecordBall;    // 显示录制状态红点的闪烁动画
    private TextView           mTextRoomId;            // 显示当前房间号
    private TextView           mTextHourRank;          // 显示小时榜
    private ImageView          mImageAdvert;           // 显示外链广告
    private LinearLayout       mAudienceList;          // 显示观众列表
    private TextView           mTextAudienceAmount;      // 显示观众数量
    private ImageView          mImageAudience1;        // 显示第一名观众
    private ImageView          mImageAudience2;        // 显示第二名观众
    private ImageView          mImageAudience3;        // 显示第三名观众
    private PKState            mPKState = PKState.PK;
    private OnFunctionListener mListener;
    private boolean            mIsLink  = false;
    private String             mRoomId;

    private List<AudienceInfo> mAudienceInfoList = new ArrayList<>();

    public ShowAnchorFunctionView(@NonNull Context context) {
        this(context, null);
    }

    public ShowAnchorFunctionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.showlive_show_anchor_function_view, this, true);
        mImagePK = mViewRoot.findViewById(R.id.iv_pk);
        mImageClose = mViewRoot.findViewById(R.id.iv_close);
        mImageMore = mViewRoot.findViewById(R.id.iv_tools);

        mImagesAnchorHead = mViewRoot.findViewById(R.id.iv_anchor_head);
        mTextRoomId = mViewRoot.findViewById(R.id.tv_room_id);
        mImageRecordBall = mViewRoot.findViewById(R.id.iv_anchor_record_ball);
        mTextHourRank = mViewRoot.findViewById(R.id.tv_anchor_hour_rank);
        mImageAdvert = mViewRoot.findViewById(R.id.iv_anchor_ad);
        mAudienceList = mViewRoot.findViewById(R.id.ll_anchor_live_audience_list);
        mTextAudienceAmount = mViewRoot.findViewById(R.id.iv_audience_amount);
        mViewRoot.findViewById(R.id.iv_audience_4).setVisibility(GONE);
        mImageAudience1 = mViewRoot.findViewById(R.id.iv_audience_1);
        mImageAudience2 = mViewRoot.findViewById(R.id.iv_audience_2);
        mImageAudience3 = mViewRoot.findViewById(R.id.iv_audience_3);
        mImageAudience1.setVisibility(GONE);
        mImageAudience2.setVisibility(GONE);
        mImageAudience3.setVisibility(GONE);
        mTextBroadcastTime = mViewRoot.findViewById(R.id.tv_anchor_broadcasting_time);
        mTextBroadcastTime.setText(String.format(Locale.US, "%s", "00:00:00"));
        try {
            UserModel userModel = UserModelManager.getInstance().getUserModel();
            ImageLoader.loadImage(getContext(), mImagesAnchorHead, userModel.userAvatar, R.drawable.showlive_bg_cover);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mViewPKAnchorList = (AnchorPKSelectView) mViewRoot.findViewById(R.id.anchor_pk_select_view);
        mViewPKAnchorList.setOnPKSelectedCallback(new AnchorPKSelectView.OnPKSelectedCallback() {
            @Override
            public void onSelected(final RoomInfo roomInfo) {
                mViewPKAnchorList.setVisibility(View.GONE);
                mTUIPusherView.sendPKRequest(roomInfo.ownerId);
                mPKState = PKState.CANCEL;
                setmButtonPKState(mPKState);
            }

            @Override
            public void onCancel() {

            }
        });
        initListener();
    }

    public void setRoomId(String roomId) {
        if (mViewPKAnchorList != null) {
            mViewPKAnchorList.setSelfRoomId(roomId);
        }
        mTextRoomId.setText(getContext().getString(R.string.showlive_room_id) + roomId);
        mRoomId = roomId;
    }

    public void refreshAvatar() {
        try {
            UserModel userModel = UserModelManager.getInstance().getUserModel();
            ImageLoader.loadImage(getContext(), mImagesAnchorHead, userModel.userAvatar, R.drawable.showlive_bg_cover);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTUIPusherView(TUIPusherView view) {
        mTUIPusherView = view;
    }

    private void initListener() {
        mImagePK.setOnClickListener(this);
        mImageClose.setOnClickListener(this);
        mImageMore.setOnClickListener(this);
        mImageAdvert.setOnClickListener(this);
        mTextHourRank.setOnClickListener(this);
        mAudienceList.setOnClickListener(this);
        IMRoomManager.getInstance().setMemberChangeListener(new IMRoomManager.OnMemberChangeListener() {
            @Override
            public void onMemberEnter(String groupId, List<AudienceInfo> list) {
                if (TextUtils.isEmpty(groupId) || !groupId.equals(mRoomId)) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    if (!mAudienceInfoList.contains(list.get(i))) {
                        mAudienceInfoList.add(list.get(i));
                    }
                }
                if (mAudienceInfoList.size() > 3) {
                    mTextAudienceAmount.setText(String.valueOf(mAudienceInfoList.size()));
                } else {
                    updateAudienceListInfo(mAudienceInfoList);
                }
            }

            @Override
            public void onMemberLeave(String groupId, AudienceInfo info) {
                if (TextUtils.isEmpty(groupId) || !groupId.equals(mRoomId)) {
                    return;
                }
                mAudienceInfoList.remove(info);
                if (mAudienceInfoList.size() > 3) {
                    mTextAudienceAmount.setText(String.valueOf(mAudienceInfoList.size()));
                } else {
                    updateAudienceListInfo(mAudienceInfoList);
                }
            }
        });
    }

    private void updateAudienceListInfo(List<AudienceInfo> list) {
        mTextAudienceAmount.setText(String.valueOf(list.size()));
        if (list.size() >= 3) {
            mImageAudience1.setVisibility(VISIBLE);
            mImageAudience2.setVisibility(VISIBLE);
            mImageAudience3.setVisibility(VISIBLE);
            ImageLoader.loadImage(getContext(), mImageAudience1, list.get(0).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience2, list.get(1).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience3, list.get(2).getAvatar(), R.drawable.showlive_bg_cover);
        } else if (list.size() == 2) {
            mImageAudience1.setVisibility(VISIBLE);
            mImageAudience2.setVisibility(VISIBLE);
            mImageAudience3.setVisibility(GONE);
            ImageLoader.loadImage(getContext(), mImageAudience1, list.get(0).getAvatar(), R.drawable.showlive_bg_cover);
            ImageLoader.loadImage(getContext(), mImageAudience2, list.get(1).getAvatar(), R.drawable.showlive_bg_cover);
        } else if (list.size() == 1) {
            mImageAudience1.setVisibility(VISIBLE);
            mImageAudience2.setVisibility(GONE);
            mImageAudience3.setVisibility(GONE);
            ImageLoader.loadImage(getContext(), mImageAudience1, list.get(0).getAvatar(), R.drawable.showlive_bg_cover);
        } else {
            mImageAudience1.setVisibility(GONE);
            mImageAudience2.setVisibility(GONE);
            mImageAudience3.setVisibility(GONE);
        }
    }

    public void setListener(OnFunctionListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_pk) {
            if (isLink()) {
                ToastUtil.toastShortMessage(getContext().getString(R.string.showlive_busy_not_pk));
                return;
            }
            switch (mPKState) {
                case PK:
                    mViewPKAnchorList.setVisibility(VISIBLE);
                    break;
                case CANCEL:
                    mTUIPusherView.cancelPKRequest();
                    mPKState = PKState.PK;
                    setmButtonPKState(mPKState);
                    break;
                case STOP:
                    mPKState = PKState.PK;
                    mTUIPusherView.stopPK();
                    setmButtonPKState(mPKState);
                    break;
                default:
                    break;
            }

        } else if (v.getId() == R.id.iv_close) {
            if (mListener != null) {
                mListener.onClose();
            }
        } else if (v.getId() == R.id.iv_tools) {
            MoreActionDialog dialog = new MoreActionDialog(getContext(), mTUIPusherView);
            dialog.show();
        } else if (v.getId() == R.id.iv_anchor_ad) {
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra("url", "https://comm.qq.com/trtc-app-finding-page/");
            intent.putExtra("title", "");
            getContext().startActivity(intent);
        } else if (v.getId() == R.id.tv_anchor_hour_rank) {
            HourRankDialog hourRankDialog = new HourRankDialog(getContext(), mRoomId);
            hourRankDialog.show();
        } else if (v.getId() == R.id.ll_anchor_live_audience_list) {
            OnlineAudienceDialog dialog = new OnlineAudienceDialog(getContext(), mRoomId);
            dialog.show();
        }
    }

    public void updateBroadcasterTimeUpdate(long second) {
        mTextBroadcastTime.setText(Utils.formattedTime(second));
    }

    /**
     * 开启红点与计时动画
     */
    public void startRecordAnimation() {
        mAnimatorRecordBall = ObjectAnimator.ofFloat(mImageRecordBall, "alpha", 1f, 0f, 1f);
        mAnimatorRecordBall.setDuration(1000);
        mAnimatorRecordBall.setRepeatCount(-1);
        mAnimatorRecordBall.start();
    }

    /**
     * 关闭红点与计时动画
     */
    public void stopRecordAnimation() {
        if (null != mAnimatorRecordBall) {
            mAnimatorRecordBall.cancel();
        }
    }


    public void setmButtonPKState(PKState pkState) {
        mPKState = pkState;
        switch (pkState) {
            case PK:
                mImagePK.setImageResource(R.drawable.showlive_pk_start);
                break;
            case CANCEL:
            case STOP:
                mImagePK.setImageResource(R.drawable.showlive_pk_stop);
                break;
            default:
                break;
        }
    }

    public boolean isLink() {
        return mIsLink;
    }

    public void setLink(boolean mIsLink) {
        this.mIsLink = mIsLink;
    }

    public enum PKState {
        PK,
        CANCEL,
        STOP,
    }


    public interface OnFunctionListener {
        void onClose();
    }
}

