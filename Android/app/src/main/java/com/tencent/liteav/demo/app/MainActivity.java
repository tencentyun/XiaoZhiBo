package com.tencent.liteav.demo.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.aboutme.UserInfoFragment;
import com.tencent.liteav.demo.common.view.ConfirmDialogFragment;
import com.tencent.liteav.demo.discover.DiscoverFragment;
import com.tencent.liteav.demo.scene.MainFragment;
import com.tencent.liteav.demo.scene.showlive.floatwindow.FloatWindow;
import com.tencent.rtmp.TXLiveBase;

import java.util.ArrayList;

import static com.tencent.liteav.debug.GenerateGlobalConfig.LICENSEURL;
import static com.tencent.liteav.debug.GenerateGlobalConfig.LICENSEURLKEY;

/**
 * APP主页，主要包含三个Fragment功能
 * <p>
 * -主页{@link MainFragment}
 * -发现页{@link DiscoverFragment}
 * -我的{@link UserInfoFragment}
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG               = MainActivity.class.getName();
    private static final String TAB_ENTERTAINMENT = "tab_entertainment";
    private static final String TAB_FIND          = "tab_find";
    private static final String TAB_MY            = "tab_my";
    private static final String ENTERTAINMENT     = "entertainment";
    private static final String TYPE              = "type";
    private static final String TAB_TAG_ARRAY[]   = {
            TAB_ENTERTAINMENT,
            TAB_FIND,
            TAB_MY
    };

    private ConfirmDialogFragment mAlertDialog;
    private FragmentTabAdapter    mTabAdapter;
    private LinearLayout          mLayoutEntertainment;
    private LinearLayout          mLayoutFind;
    private LinearLayout          mLayoutMe;
    private ImageView             mImageTabEntertainment;
    private ImageView             mImageTabFind;
    private ImageView             mImageTabMe;
    private TextView              mTextTabEntertainment;
    private TextView              mTextTabMe;
    private TextView              mTextTabFind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_trtc_main);
        initStatusBar();
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            Log.d(TAG, "brought to front");
            finish();
            return;
        }
        initTabView();
        mAlertDialog = new ConfirmDialogFragment();
        initTXLiveBase();
    }


    // 初始化过程中会获取手机型号，手机型号属于个人数据，需用户同意隐私政策后才能采集。
    private void initTXLiveBase() {
        TXLiveBase.setConsoleEnabled(true);
        TXLiveBase.getInstance().setLicence(getApplicationContext(), LICENSEURL, LICENSEURLKEY);
    }

    private void initTabView() {

        ArrayList<Fragment> fragments = new ArrayList<>();

        Bundle entertainmentBundle = new Bundle();
        MainFragment entertainmentFragment = new MainFragment();
        entertainmentBundle.putString(TYPE, ENTERTAINMENT);
        entertainmentFragment.setArguments(entertainmentBundle);

        fragments.add(entertainmentFragment);
        fragments.add(new DiscoverFragment());
        fragments.add(new UserInfoFragment());
        mTabAdapter = new FragmentTabAdapter(this, fragments, R.id.contentPanel);

        mLayoutEntertainment = (LinearLayout) findViewById(R.id.ll_tab_main);
        mLayoutEntertainment.setOnClickListener(this);

        mLayoutFind = (LinearLayout) findViewById(R.id.ll_tab_find);
        mLayoutFind.setOnClickListener(this);
        mLayoutMe = (LinearLayout) findViewById(R.id.ll_tab_me);
        mLayoutMe.setOnClickListener(this);

        mImageTabEntertainment = mLayoutEntertainment.findViewById(R.id.img_main);
        mTextTabEntertainment = mLayoutEntertainment.findViewById(R.id.tv_main);
        mImageTabMe = mLayoutMe.findViewById(R.id.img_me);
        mTextTabMe = mLayoutMe.findViewById(R.id.tv_me);
        mImageTabFind = mLayoutFind.findViewById(R.id.img_find);
        mTextTabFind = mLayoutFind.findViewById(R.id.tv_find);
        updateItemView(TAB_ENTERTAINMENT);
        mTabAdapter.setOnTabChangeListener(new FragmentTabAdapter.OnTabChangeListener() {
            @Override
            public void OnTabChanged(int index) {
                updateItemView(TAB_TAG_ARRAY[index]);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_tab_main:
                mTabAdapter.setCurrentFragment(0);
                break;
            case R.id.ll_tab_find:
                mTabAdapter.setCurrentFragment(1);
                break;
            case R.id.ll_tab_me:
                mTabAdapter.setCurrentFragment(2);
                break;
        }
    }

    @Override
    protected void onResume() {
        KeepAliveService.start(this);
        super.onResume();
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

    @Override
    public void onBackPressed() {
        if (mAlertDialog.isAdded()) {
            mAlertDialog.dismiss();
        }
        mAlertDialog.setMessage(getString(R.string.app_dialog_exit_app));
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
                KeepAliveService.stop(MainActivity.this);
                finish();
            }
        });
        mAlertDialog.show(getFragmentManager(), "confirm_fragment");
    }

    private void updateItemView(String type) {
        if (type.equals(TAB_ENTERTAINMENT)) {
            mTextTabEntertainment.setTextColor(getResources().getColor(R.color.app_color_tab_select));
            mImageTabEntertainment.setImageResource(R.drawable.app_home_hover);
            mTextTabMe.setTextColor(getResources().getColor(R.color.app_color_tab_normal));
            mImageTabMe.setImageResource(R.drawable.app_user_normal);
            mTextTabFind.setTextColor(getResources().getColor(R.color.app_color_tab_normal));
            mImageTabFind.setImageResource(R.drawable.app_find_normal);
        } else if (type.equals(TAB_MY)) {
            mTextTabEntertainment.setTextColor(getResources().getColor(R.color.app_color_tab_normal));
            mImageTabEntertainment.setImageResource(R.drawable.app_home_normal);
            mTextTabMe.setTextColor(getResources().getColor(R.color.app_color_tab_select));
            mImageTabMe.setImageResource(R.drawable.app_user_hover);
            mTextTabFind.setTextColor(getResources().getColor(R.color.app_color_tab_normal));
            mImageTabFind.setImageResource(R.drawable.app_find_normal);
        } else {
            mTextTabEntertainment.setTextColor(getResources().getColor(R.color.app_color_tab_normal));
            mImageTabEntertainment.setImageResource(R.drawable.app_home_normal);
            mTextTabMe.setTextColor(getResources().getColor(R.color.app_color_tab_normal));
            mImageTabMe.setImageResource(R.drawable.app_user_normal);
            mTextTabFind.setTextColor(getResources().getColor(R.color.app_color_tab_select));
            mImageTabFind.setImageResource(R.drawable.app_find_hover);
        }
    }
}
