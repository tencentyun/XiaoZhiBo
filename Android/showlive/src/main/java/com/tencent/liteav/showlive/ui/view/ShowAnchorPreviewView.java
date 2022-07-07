package com.tencent.liteav.showlive.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.RTCubeUtils;
import com.tencent.liteav.showlive.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowAnchorPreviewView extends FrameLayout {
    private static final String TAG = "ShowAnchorPreviewView";

    private View            mViewRoot;
    private TextView        mTextName;
    private EditText        mEditName;
    private CircleImageView mImageCover;

    public ShowAnchorPreviewView(@NonNull Context context) {
        this(context, null);
    }

    public ShowAnchorPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.showlive_show_anchor_preview_view, this, true);
        mTextName = mViewRoot.findViewById(R.id.tv_live_room_name);
        mEditName = mViewRoot.findViewById(R.id.et_live_room_name);
        mImageCover = mViewRoot.findViewById(R.id.img_live_room_cover);
        mEditName.setFocusableInTouchMode(!RTCubeUtils.isRTCubeApp(context));
    }

    public void setTitle(String text) {
        if (!TextUtils.isEmpty(text)) {
            mTextName.setText(text);
        }
    }

    public ImageView getImage() {
        return mImageCover;
    }

    public void setCoverImage(String url) {
        ImageLoader.loadImage(getContext(), mImageCover, url, R.drawable.showlive_bg_cover);
    }

    public String getRoomName() {
        return mEditName.getText().toString();
    }

    public void setRoomName(String text) {
        mEditName.setText(text);
    }
}
