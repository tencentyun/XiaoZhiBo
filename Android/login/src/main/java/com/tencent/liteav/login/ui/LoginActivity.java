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


import com.tencent.liteav.basic.AvatarConstant;
import com.tencent.liteav.login.R;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.ui.view.LoginStatusLayout;

import java.util.Random;


public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getName();

    private static final int STATUS_WITHOUT_LOGIN = 0;   // 未登录
    private static final int STATUS_LOGGING_IN    = 1;   // 正在登录
    private static final int STATUS_LOGIN_SUCCESS = 2;   // 登录成功
    private static final int STATUS_LOGIN_FAIL    = 3;   // 登录失败

    private EditText mEditUserName;
    private EditText mEditPassword;
    private TextView mTextStartRegister;
    private View     mProgressBar;
    private Button   mButtonLogin;

    private LoginStatusLayout mLayoutLoginStatus;        // 登录状态的提示栏

    private Handler  mMainHandler;
    private Runnable mResetLoginStatusRunnable = new Runnable() {
        @Override
        public void run() {
            handleLoginStatus(STATUS_WITHOUT_LOGIN);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_login);
        initStatusBar();
        mMainHandler = new Handler();
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginBtnStatus();
    }

    private void initData() {
        String token = ProfileManager.getInstance().getToken();
        if (!TextUtils.isEmpty(token)) {
            mProgressBar.setVisibility(View.VISIBLE);
            handleLoginStatus(STATUS_LOGGING_IN);
            ProfileManager.getInstance().autoLogin(token, new ProfileManager.ActionCallback() {
                @Override
                public void onSuccess() {
                    handleLoginStatus(STATUS_LOGIN_SUCCESS);
                    startMainActivity();
                }

                @Override
                public void onFailed(int code, String msg) {
                    mProgressBar.setVisibility(View.GONE);
                    if (code == ERROR_CODE_NEED_REGISTER) {
                        String username = ProfileManager.getInstance().getUserId();
                        initializeUserProfile(username);
                        finish();
                    } else {
                        handleLoginStatus(STATUS_LOGIN_FAIL);
                    }
                }
            });
        }
    }

    private void initView() {
        mLayoutLoginStatus = findViewById(R.id.cl_login_status);
        mProgressBar = findViewById(R.id.progress_group);

        initEditUsername();
        initEditPassword();
        initTextStartRegister();
        initButtonLogin();

        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.login_app_version, getAppVersion(this)));
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

    private void initTextStartRegister() {
        mTextStartRegister = findViewById(R.id.tv_start_register);
        mTextStartRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent starter = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(starter);
                finish();
            }
        });
    }

    private void initButtonLogin() {
        mButtonLogin = findViewById(R.id.tv_login);
        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
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

    private void login() {
        final String username = mEditUserName.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            ToastUtil.toastLongMessage(R.string.login_tips_input_correct_info);
            return;
        }
        handleLoginStatus(STATUS_LOGGING_IN);

        final ProfileManager profileManager = ProfileManager.getInstance();
        profileManager.login(username, password, new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                handleLoginStatus(STATUS_LOGIN_SUCCESS);
                startMainActivity();
                finish();
            }

            @Override
            public void onFailed(int code, String msg) {
                if (code == ERROR_CODE_NEED_REGISTER) {
                    handleLoginStatus(STATUS_LOGIN_SUCCESS);
                    initializeUserProfile(username);
                } else {
                    handleLoginStatus(STATUS_LOGIN_FAIL);
                }
            }
        });
    }

    public void handleLoginStatus(int loginStatus) {
        mLayoutLoginStatus.setLoginStatus(loginStatus);
        if (STATUS_LOGGING_IN == loginStatus) {
            mMainHandler.removeCallbacks(mResetLoginStatusRunnable);
            mMainHandler.postDelayed(mResetLoginStatusRunnable, 6000);
        }
    }

    private void initializeUserProfile(String userName) {
        String[] avatarArr = AvatarConstant.USER_AVATAR_ARRAY;
        String avatarUrl = avatarArr[new Random().nextInt(avatarArr.length)];
        ProfileManager.getInstance().setNicknameAndAvatar(userName, avatarUrl, new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                ToastUtil.toastLongMessage(getString(R.string.login_toast_register_success_and_logging_in));
                startMainActivity();
                finish();
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtil.toastLongMessage(getString(R.string.login_toast_failed_to_set_username, msg));
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction("com.tencent.liteav.action.portal");
        intent.setPackage(getPackageName());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainHandler.removeCallbacks(mResetLoginStatusRunnable);
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
