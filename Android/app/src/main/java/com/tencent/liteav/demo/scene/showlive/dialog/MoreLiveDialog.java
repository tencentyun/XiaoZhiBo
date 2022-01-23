package com.tencent.liteav.demo.scene.showlive.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.scene.showlive.fragment.MoreLiveFragment;

public class MoreLiveDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mRootView = inflater.inflate(R.layout.app_more_live_dialog, container);
        initStatusBar(getDialog().getWindow());
        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setLocation(getDialog().getWindow());
        intiMoreLiveFragment(getChildFragmentManager());
    }

    private void initStatusBar(Window window) {
        if (window == null || getContext() == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window = getDialog().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void setLocation(Window window) {
        if (window == null || getContext() == null) {
            return;
        }
        window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setPadding(0, 0, 0, 0);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.RIGHT;
        params.width = getActivity().getWindow().getDecorView().getWidth() * 4 / 5;
        params.height = getActivity().getWindow().getDecorView().getHeight();
        window.setAttributes(params);
    }

    private void intiMoreLiveFragment(FragmentManager fragmentManager) {
        if (fragmentManager == null) {
            return;
        }
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container_more_live);

        if (!(fragment instanceof MoreLiveFragment)) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            fragment = MoreLiveFragment.newInstance();
            ft.replace(R.id.fragment_container_more_live, fragment);
            ft.commit();
        }
    }
}
