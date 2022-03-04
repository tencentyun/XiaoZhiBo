package com.tencent.liteav.demo.scene.showlive.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.scene.showlive.dialog.SelectPhotoDialog;
import com.tencent.liteav.demo.services.RoomService;
import com.tencent.liteav.demo.services.room.bean.http.ShowLiveCosInfo;
import com.tencent.liteav.demo.services.room.callback.GetCosInfoCallback;
import com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager;

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowAnchorPreviewView extends FrameLayout {
    private static final String TAG = "ShowAnchorPreviewView";

    private View            mViewRoot;
    private TextView        mTextName;
    private EditText        mEditName;
    private CircleImageView mImageCover;
    private ShowLiveCosInfo mCosInfo;

    public ShowAnchorPreviewView(@NonNull Context context) {
        this(context, null);
    }

    public ShowAnchorPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mViewRoot = LayoutInflater.from(getContext()).inflate(R.layout.app_show_anchor_preview_view, this, true);
        mTextName = mViewRoot.findViewById(R.id.tv_live_room_name);
        mEditName = mViewRoot.findViewById(R.id.et_live_room_name);
        mImageCover = mViewRoot.findViewById(R.id.img_live_room_cover);
        mImageCover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomService.getInstance(getContext()).getRoomCosInfo(HttpRoomManager.RoomCosType.AVATAR.getType(),
                        new GetCosInfoCallback() {
                            @Override
                            public void onSuccess(ShowLiveCosInfo cosInfo) {
                                if (TextUtils.isEmpty(cosInfo.bucket)) {
                                    Log.i(TAG, cosInfo.toString());
                                    return;
                                }
                                mCosInfo = cosInfo;
                                Log.i(TAG, "getCosInfo success,cosInfo:" + cosInfo.toString());
                                SelectPhotoDialog dialog = new SelectPhotoDialog(getContext());
                                dialog.show();
                            }

                            @Override
                            public void onFailed(int code, String msg) {
                                Log.i(TAG, "getCosInfo failed! code: " + code + " ,msg: " + msg);
                            }
                        });
            }
        });
    }

    public ShowLiveCosInfo getCosInfo() {
        return mCosInfo;
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
        ImageLoader.loadImage(getContext(), mImageCover, url, R.drawable.app_bg_cover);
    }

    public String getRoomName() {
        return mEditName.getText().toString();
    }

    public void setRoomName(String text) {
        mEditName.setText(text);
    }
}
