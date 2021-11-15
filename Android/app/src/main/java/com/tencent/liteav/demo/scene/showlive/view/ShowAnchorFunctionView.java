package com.tencent.liteav.demo.scene.showlive.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.utils.Utils;
import com.tencent.liteav.demo.scene.showlive.dialog.MoreActionDialog;
import com.tencent.liteav.demo.services.room.bean.RoomInfo;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;

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
    private PKState            mPKState = PKState.PK;
    private OnFunctionListener mListener;
    private boolean            mIsLink  = false;

    public ShowAnchorFunctionView(@NonNull Context context) {
        this(context, null);
    }

    public ShowAnchorFunctionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.app_show_anchor_function_view, this, true);
        mImagePK = mViewRoot.findViewById(R.id.iv_pk);
        mImageClose = mViewRoot.findViewById(R.id.iv_close);
        mImageMore = mViewRoot.findViewById(R.id.iv_tools);

        mImagesAnchorHead = mViewRoot.findViewById(R.id.iv_anchor_head);
        mTextRoomId = mViewRoot.findViewById(R.id.tv_room_id);
        mImageRecordBall = mViewRoot.findViewById(R.id.iv_anchor_record_ball);
        mTextBroadcastTime = mViewRoot.findViewById(R.id.tv_anchor_broadcasting_time);
        mTextBroadcastTime.setText(String.format(Locale.US, "%s", "00:00:00"));
        try {
            UserModel userModel = UserModelManager.getInstance().getUserModel();
            ImageLoader.loadImage(getContext(), mImagesAnchorHead, userModel.userAvatar, R.drawable.app_bg_cover);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mViewPKAnchorList = (AnchorPKSelectView) mViewRoot.findViewById(R.id.anchor_pk_select_view);
        mViewPKAnchorList.setOnPKSelectedCallback(new AnchorPKSelectView.onPKSelectedCallback() {
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
        mTextRoomId.setText(getContext().getString(R.string.app_room_id) + roomId);
    }

    public void setTUIPusherView(TUIPusherView view) {
        mTUIPusherView = view;
    }

    public void initListener() {
        mImagePK.setOnClickListener(this);
        mImageClose.setOnClickListener(this);
        mImageMore.setOnClickListener(this);
    }

    public void setListener(OnFunctionListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_pk) {
            if (isLink()) {
                ToastUtils.showShort(R.string.app_busy_not_pk);
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
            }

        } else if (v.getId() == R.id.iv_close) {
            if (mListener != null) {
                mListener.onClose();
            }
        } else if (v.getId() == R.id.iv_tools) {
            MoreActionDialog dialog = new MoreActionDialog(getContext(), mTUIPusherView);
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
        if (null != mAnimatorRecordBall)
            mAnimatorRecordBall.cancel();
    }


    public void setmButtonPKState(PKState pkState) {
        mPKState = pkState;
        switch (pkState) {
            case PK:
                mImagePK.setImageResource(R.drawable.app_pk_start);
                break;
            case CANCEL:
            case STOP:
                mImagePK.setImageResource(R.drawable.app_pk_stop);
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

