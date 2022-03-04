package com.tencent.liteav.demo.aboutme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.app.KeepAliveService;
import com.tencent.liteav.demo.common.view.ConfirmDialogFragment;
import com.tencent.liteav.demo.scene.showlive.floatwindow.FloatWindow;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.ui.LoginActivity;

public class LogOffActivity extends AppCompatActivity {

    private Context               mContext;
    private ConfirmDialogFragment mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_log_off);
        initStatusBar();
        mContext = this;
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView tvAccound = (TextView) findViewById(R.id.tv_account);
        String phone = UserModelManager.getInstance().getUserModel().phone;
        tvAccound.setText(getString(R.string.app_logoff_cur_account, phone));

        Button mBtnLogOff = (Button) findViewById(R.id.btn_logoff);
        mBtnLogOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogOffDialog();
            }
        });
    }

    private void showLogOffDialog() {
        if (mAlertDialog == null) {
            mAlertDialog = new ConfirmDialogFragment();
        }
        if (mAlertDialog.isAdded()) {
            mAlertDialog.dismiss();
        }
        mAlertDialog.setMessage(mContext.getString(R.string.app_logoff_confirm));
        mAlertDialog.setNegativeClickListener(new ConfirmDialogFragment.NegativeClickListener() {
            @Override
            public void onClick() {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.setPositiveClickListener(new ConfirmDialogFragment.PositiveClickListener() {
            @Override
            public void onClick() {
                if (FloatWindow.mIsShowing) {
                    FloatWindow.getInstance().destroy();
                }
                mAlertDialog.dismiss();
                logoff();
            }
        });
        mAlertDialog.show(this.getFragmentManager(), "confirm_logoff_fragment");
    }

    private void logoff() {
        final ProfileManager profileManager = ProfileManager.getInstance();
        profileManager.logoff(new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                KeepAliveService.stop(LogOffActivity.this);
                if (FloatWindow.mIsShowing) {
                    FloatWindow.getInstance().destroy();
                }
                ToastUtils.showShort(getString(R.string.app_logoff_account_ok));
                startLoginActivity();
            }

            @Override
            public void onFailed(int code, String msg) {

            }
        });
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}