package com.tencent.liteav.login.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.liteav.basic.AvatarConstant;
import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.debug.GenerateGlobalConfig;
import com.tencent.liteav.login.R;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.ui.view.ProfileActivity;
import com.tencent.qcloud.tuicore.util.ToastUtil;

import java.util.Random;

public class LoginWithoutServerActivity extends AppCompatActivity {
    private static final String TAG = "LoginWithoutServerless";

    private EditText mEditUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_without_serverless);
        initStatusBar();
        initView();
    }

    private void initView() {
        mEditUserId = (EditText) findViewById(R.id.et_userId);
        Button buttonLogin = (Button) findViewById(R.id.tv_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mEditUserId.getText().toString().trim())) {
                    ToastUtil.toastShortMessage(getString(R.string.login_hint_user_id));
                    return;
                }
                login();
            }
        });
    }

    private void login() {
        String userId = mEditUserId.getText().toString().trim();
        final UserModel userModel = new UserModel();
        userModel.userId = userId;
        userModel.userName = userId;
        int index = new Random().nextInt(AvatarConstant.USER_AVATAR_ARRAY.length);
        userModel.userAvatar = AvatarConstant.USER_AVATAR_ARRAY[index];
        userModel.userSig = GenerateGlobalConfig.genTestUserSig(userId);
        UserModelManager.getInstance().setUserModel(userModel);
        V2TIMSDKConfig config = new V2TIMSDKConfig();
        config.setLogLevel(V2TIMSDKConfig.V2TIM_LOG_DEBUG);
        ProfileManager.getInstance().loginIMWithoutServer(userModel, new ProfileManager.ActionCallback() {
            @Override
            public void onSuccess() {
                UserModel model = UserModelManager.getInstance().getUserModel();
                if (TextUtils.isEmpty(model.userName) || TextUtils.isEmpty(model.userAvatar)) {
                    Intent intent = new Intent(LoginWithoutServerActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setAction("com.tencent.liteav.action.portal");
                    intent.setPackage(getPackageName());
                    startActivity(intent);
                }
                finish();
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtil.toastShortMessage(msg);
                Log.d(TAG, "login fail code: " + code + " msg:" + msg);
            }
        });

    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}