package com.tencent.liteav.demo.scene.showlive.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.demo.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowAnchorPreviewView extends FrameLayout {
    private View            mViewRoot;
    private TextView        mTextName;
    private EditText        mEditName;
    private CircleImageView mImageCover;

    public ShowAnchorPreviewView(@NonNull Context context) {
        this(context, null);
    }

    public ShowAnchorPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.app_show_anchor_preview_view, this, true);
        mTextName = mViewRoot.findViewById(R.id.tv_live_room_name);
        mEditName = mViewRoot.findViewById(R.id.et_live_room_name);
        mImageCover = mViewRoot.findViewById(R.id.img_live_room_cover);
    }

    public void setTitle(String text) {
        if (!TextUtils.isEmpty(text)) {
            mTextName.setText(text);
        }
    }

    public void setCoverImage(String url) {
        ImageLoader.loadImage(getContext(), mImageCover, url, R.drawable.app_bg_cover);
    }

    public String getRoomName() {
        return mEditName.getText().toString();
    }

    public void setRoomName(String text) {
        mEditName.setText(text);
    }
}
