package com.tencent.liteav.demo.scene.showlive.dialog;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.liteav.demo.R;
import com.tencent.qcloud.tuikit.tuipusher.model.TUIPusherConfig;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;

import java.util.Locale;

public class MoreActionDialog extends BottomSheetDialog {
    protected AppCompatImageButton mButtonSwitchCamera;
    protected TextView             mTextSwitchCamera;
    private   TUIPusherView        mTuiPusherView;
    private   boolean              mIsFrontCamere = TUIPusherConfig.getInstance().isFrontCamera();

    public MoreActionDialog(@NonNull Context context, TUIPusherView tuiPusherView) {
        super(context, R.style.ShowLiveMoreDialogTheme);
        setContentView(R.layout.app_showlive_dialog_more_action);
        mTuiPusherView = tuiPusherView;
        initView();
    }

    private void initView() {
        mButtonSwitchCamera = findViewById(R.id.btn_switch_camera);
        mTextSwitchCamera = findViewById(R.id.tv_switch_camera);
        mButtonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFrontCamere = !mIsFrontCamere;
                TUIPusherConfig.getInstance().setFrontCamera(mIsFrontCamere);
                mTuiPusherView.switchCamera(mIsFrontCamere);
            }
        });
        if (!isZh(getContext())) {
            mTextSwitchCamera.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
        }
    }

    public boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
}
