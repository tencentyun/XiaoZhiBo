package com.tencent.liteav.demo.scene.showlive.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.demo.R;
import com.tencent.qcloud.tuikit.tuiplayer.view.TUIPlayerView;

/**
 * 秀场直播 - 观众页面等功能性View，包含：
 *
 * - 顶部房间信息的View；
 * - 底部退出直播间的View;
 *
 */
public class ShowAudienceFunctionView extends FrameLayout {
    private View               mViewRoot;
    private ImageView          mImageClose;
    private ImageView          mImagesAnchorHead;      // 显示房间主播头像
    private TextView           mTextAnchorName;        // 显示房间主播名称
    private TextView           mTextRoomId;            // 显示当前房间号
    private TUIPlayerView      mTUIPlayerView;
    private OnFunctionListener mOnFunctionListener;

    public ShowAudienceFunctionView(@NonNull Context context) {
        this(context, null);
    }

    public ShowAudienceFunctionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.app_show_audience_function_view, this, true);

        mImagesAnchorHead = mViewRoot.findViewById(R.id.iv_anchor_head);
        mTextAnchorName = mViewRoot.findViewById(R.id.tv_anchor_name);
        mTextRoomId = mViewRoot.findViewById(R.id.tv_room_id);

        mImageClose = mViewRoot.findViewById(R.id.iv_close);
        mImageClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnFunctionListener != null) {
                    mOnFunctionListener.onClose();
                }
            }
        });
    }

    public void setListener(OnFunctionListener listener) {
        mOnFunctionListener = listener;
    }

    public void setAnchorInfo(String avatarUrl, String anchorName, int roomId) {
        ImageLoader.loadImage(getContext(), mImagesAnchorHead, avatarUrl, R.drawable.app_bg_cover);
        mTextAnchorName.setText(anchorName);
        mTextRoomId.setText(getContext().getString(R.string.app_room_id) + roomId);
    }

    public void setTUIPlayerView(TUIPlayerView view) {
        mTUIPlayerView = view;
    }

    public interface OnFunctionListener {
        void onClose();
    }
}

