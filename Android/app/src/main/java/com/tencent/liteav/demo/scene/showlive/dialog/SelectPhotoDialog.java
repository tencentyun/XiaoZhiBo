package com.tencent.liteav.demo.scene.showlive.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.liteav.demo.R;

import java.io.File;

public class SelectPhotoDialog extends BottomSheetDialog {
    public static final int CODE_CAMERA  = 1001;
    public static final int CODE_GALLERY = 1002;

    private Context  mContext;
    private TextView mTextCamera;
    private TextView mTextPhotoLibrary;

    public SelectPhotoDialog(Context context) {
        super(context, R.style.ShowLiveMoreDialogTheme);
        setContentView(R.layout.app_show_live_anchor_select_photo);
        mContext = context;
        mTextCamera = findViewById(R.id.tv_anchor_camera);
        mTextPhotoLibrary = findViewById(R.id.tv_anchor_photo_library);
        mTextCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(mContext,
                        "com.tencent.liteav.demo.fileprovider",
                        new File(mContext.getExternalCacheDir(), "xiaozhibo_avatar.jpg")));
                ((Activity) mContext).startActivityForResult(camera, CODE_CAMERA);
                dismiss();
            }
        });
        mTextPhotoLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                ((Activity) mContext).startActivityForResult(gallery, CODE_GALLERY);
                dismiss();
            }
        });
        findViewById(R.id.tv_anchor_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
