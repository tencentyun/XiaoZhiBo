package com.tencent.liteav.login.ui;

import static com.tencent.liteav.login.model.ProfileManager.ERROR_CODE_NEED_REGISTER;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.login.R;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.rtmp.TXLiveBase;


public class RegisterActivity extends BaseActivity {
    private static final String TAG = RegisterActivity.class.getName();

    private EditText mEditUserName;
    private EditText mEditPassword;
    private Button   mButtonLogin;
    private Handler  mMainHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_register);
        initStatusBar();
        initView();
        mMainHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginBtnStatus();
    }

    private void initView() {
        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.login_app_version, getAppVersion(this)));

        initEditUsername();
        initEditPassword();
        initButtonLogin();
    }

    private void initEditUsername() {
        mEditUserName = findViewById(R.id.et_username);
        mEditUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginBtnStatus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initEditPassword() {
        mEditPassword = findViewById(R.id.et_password);
        mEditPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginBtnStatus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initButtonLogin() {
        mButtonLogin = findViewById(R.id.tv_login);
        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void updateLoginBtnStatus() {
        if (mEditUserName.length() == 0 || mEditPassword.length() == 0) {
            mButtonLogin.setEnabled(false);
            return;
        }
        mButtonLogin.setEnabled(true);
    }

    private void register() {
        String username = mEditUserName.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            ToastUtils.showLong(R.string.login_tips_input_correct_info);
            return;
        }

        final ProfileManager profileManager = ProfileManager.getInstance();
        profileManager.register(username, password, new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                ToastUtils.showShort(R.string.login_toast_register_success_and_logging_in);
                Intent starter = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(starter);
                finish();
            }

            @Override
            public void onFailed(int code, String msg) {
                if (code == ERROR_CODE_NEED_REGISTER) {
                    ToastUtils.showShort(R.string.login_toast_register_success_and_logging_in);
                    Intent starter = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(starter);
                    finish();
                } else {
                    ToastUtils.showLong(msg);
                }
            }
        });
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

    public String getAppVersion(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }
}
