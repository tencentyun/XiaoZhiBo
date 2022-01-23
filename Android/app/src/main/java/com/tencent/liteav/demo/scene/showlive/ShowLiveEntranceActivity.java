package com.tencent.liteav.demo.scene.showlive;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.TCConstants;
import com.tencent.liteav.demo.common.utils.Utils;
import com.tencent.liteav.demo.scene.showlive.fragment.ShowLiveRoomListFragment;
import com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager;
import com.tencent.liteav.login.model.ProfileManager;

/**
 * 秀场直播 - 入口页面， 主要包含
 *
 * -直播列表页面{@link ShowLiveRoomListFragment}
 * -主播页面{@link ShowLiveAnchorActivity}
 * -观众页面{@link ShowLiveAudienceActivity}
 */
public class ShowLiveEntranceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HttpRoomManager.getInstance().initSdkAppId(ProfileManager.getInstance().getSdkAppId());
        setContentView(R.layout.app_activity_showlive_entrance);
        Utils.initStatusBar(this);
        initTitleEvent();
        initNavigationMenu();
        initLiveRoomListFragment();
    }

    private void initTitleEvent() {
        findViewById(R.id.tv_liveroom_title).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switchCDNPlayMode();
                return false;
            }
        });
    }

    private void initNavigationMenu() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_liveroom_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(TCConstants.TRTC_LIVE_ROOM_DOCUMENT_URL));
                startActivity(intent);
            }
        });

        boolean useCDNFirst = SPUtils.getInstance().getBoolean(TCConstants.USE_CDN_PLAY, false);
        if (useCDNFirst) {
            findViewById(R.id.tv_cdn_tag).setVisibility(View.VISIBLE);
        }
    }

    private void initLiveRoomListFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (!(fragment instanceof ShowLiveRoomListFragment)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            fragment = ShowLiveRoomListFragment.newInstance();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        }
    }

    private void switchCDNPlayMode() {
        final boolean useCDNFirst = SPUtils.getInstance().getBoolean(TCConstants.USE_CDN_PLAY, false);
        int targetResId = useCDNFirst ? R.string.app_switch_trtc_mode : R.string.app_switch_cdn_mode;
        new AlertDialog.Builder(this)
                .setMessage(targetResId)
                .setPositiveButton(R.string.app_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean switchMode = !useCDNFirst;
                        SPUtils.getInstance().put(TCConstants.USE_CDN_PLAY, switchMode);
                        ToastUtils.showLong(R.string.app_warning_switched_mode);
                    }
                })
                .setNegativeButton(R.string.app_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
