package com.tencent.liteav.demo.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.login.ui.LoginActivity;
import com.tencent.liteav.login.ui.LoginWithoutServerActivity;

public class NavigationActivity extends AppCompatActivity {
    private static final String TAG = "NavigationActivity";

    private TextView mTextServerlessTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        initStatusBar();
        initView();
    }

    private void initView() {
        mTextServerlessTips = findViewById(R.id.tv_serverless_tips);
        findViewById(R.id.btn_without_server_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserModelManager.getInstance().setUsageModel(false);
                startActivity(new Intent(NavigationActivity.this, LoginWithoutServerActivity.class));
                finish();
            }
        });
        findViewById(R.id.btn_normal_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserModelManager.getInstance().setUsageModel(true);
                startActivity(new Intent(NavigationActivity.this, LoginActivity.class));
                finish();
            }
        });
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String backgroundService = getString(R.string.app_build_background_service);
        String serviceTips = getString(R.string.app_build_background_tips);
        builder.append(serviceTips);
        builder.append(backgroundService);
        int privacyStartIndex = serviceTips.length();
        int privacyEndIndex = privacyStartIndex + backgroundService.length();
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(com.tencent.liteav.login.R.color.login_color_blue));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent();
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setAction("com.tencent.liteav.action.webview");
                intent.setPackage(getPackageName());
                intent.putExtra("title", getString(R.string.app_build_background_service));
                intent.putExtra("url", "https://cloud.tencent.com/document/product/454/38625\n");
                IntentUtils.safeStartActivity(NavigationActivity.this, intent);
            }
        };
        builder.setSpan(privacyClickableSpan, privacyStartIndex, privacyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTextServerlessTips.setMovementMethod(LinkMovementMethod.getInstance());
        mTextServerlessTips.setText(builder);
        mTextServerlessTips.setHighlightColor(Color.TRANSPARENT);
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