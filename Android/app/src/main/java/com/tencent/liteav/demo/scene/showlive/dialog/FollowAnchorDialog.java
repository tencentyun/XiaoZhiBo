package com.tencent.liteav.demo.scene.showlive.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.demo.R;

/**
 * 关注主播dialog
 */
public class FollowAnchorDialog extends BottomSheetDialog {
    private ImageView    mImageAvatar;
    private ImageView    mImageAdd;
    private TextView     mTextName;
    private TextView     mTextFollow;
    private LinearLayout mLinearLayout;
    private boolean      mIsFollow;
    private Context      mContext;
    private TextView     mTextAnchorFans;
    private TextView     mTextAnchorFollow;

    private OnFollowClickListener mListener;

    public FollowAnchorDialog(Context context, boolean isFollow, String anchorName, String anchorAvatar) {
        super(context, R.style.ShowLiveMoreDialogTheme);
        mContext = context;
        mIsFollow = isFollow;
        setContentView(R.layout.app_follow_anchor_dialog);
        mImageAvatar = findViewById(R.id.iv_dialog_anchor_avatar);
        mImageAdd = findViewById(R.id.iv_dialog_add);
        mTextName = findViewById(R.id.tv_dialog_anchor_name);
        mLinearLayout = findViewById(R.id.ll_dialog_follow);
        mTextFollow = findViewById(R.id.tv_dialog_follow);
        mTextAnchorFans = findViewById(R.id.tv_dialog_anchor_fans);
        mTextAnchorFollow = findViewById(R.id.tv_dialog_anchor_follow);
        mTextAnchorFans.setText(mTextAnchorFans.getText() + " 456");
        mTextAnchorFollow.setText(mTextAnchorFollow.getText() + " 456");
        ImageLoader.loadImage(getContext(), mImageAvatar, anchorAvatar, R.drawable.app_bg_cover);
        mTextName.setText(anchorName);
        if (isFollow) {
            mImageAdd.setVisibility(View.GONE);
            mLinearLayout.setBackgroundResource(R.drawable.app_bg_already_follow_anchor);
            mTextFollow.setText(mContext.getResources().getString(R.string.app_already_followed));
            mTextFollow.setTextColor(mContext.getResources().getColor(R.color.app_color_black));
        }
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFollow = !mIsFollow;
                if (mIsFollow) {
                    mTextFollow.setText(mContext.getResources().getString(R.string.app_already_followed));
                    mLinearLayout.setBackgroundResource(R.drawable.app_bg_already_follow_anchor);
                    mImageAdd.setVisibility(View.GONE);
                    mTextFollow.setTextColor(mContext.getResources().getColor(R.color.app_color_black));
                } else {
                    mTextFollow.setText(mContext.getResources().getString(R.string.app_follow));
                    mLinearLayout.setBackgroundResource(R.drawable.app_bg_follow_anchor);
                    mImageAdd.setVisibility(View.VISIBLE);
                    mTextFollow.setTextColor(mContext.getResources().getColor(R.color.app_color_white));
                }
                mListener.onClick(mIsFollow);
            }
        });
    }

    public interface OnFollowClickListener {
        void onClick(boolean isFollow);
    }

    public void setListener(OnFollowClickListener mListener) {
        this.mListener = mListener;
    }
}
