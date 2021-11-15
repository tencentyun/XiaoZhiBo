package com.tencent.liteav.demo.app;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

/**
 * 主页Activity的三个Fragment适配器类
 *
 * -负责管理主页、发现页、我的页面三个Fragment
 */
public class FragmentTabAdapter {

    private List<Fragment>      mFragments;
    private FragmentActivity    mFragmentActivity;
    private int                 mFragmentContentId;
    private int                 mCurrentTab = 0;
    private OnTabChangeListener onTabChangeListener;

    public FragmentTabAdapter(FragmentActivity fragmentActivity,
                              List<Fragment> fragments, int fragmentContentId) {
        this.mFragments = fragments;
        this.mFragmentActivity = fragmentActivity;
        this.mFragmentContentId = fragmentContentId;

        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        ft.add(fragmentContentId, fragments.get(0));
        try {
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != onTabChangeListener)
            onTabChangeListener.OnTabChanged(0);
    }

    private void showTab(int idx) {
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment fragment = mFragments.get(i);
            FragmentTransaction ft = obtainFragmentTransaction(idx);

            if (idx == i) {
                ft.show(fragment);
            } else {
                ft.hide(fragment);
            }
            ft.commitAllowingStateLoss();
        }
        mCurrentTab = idx;
    }

    private FragmentTransaction obtainFragmentTransaction(int index) {
        FragmentTransaction ft = mFragmentActivity.getSupportFragmentManager().beginTransaction();
        return ft;
    }

    public int getCurrentTab() {
        return mCurrentTab;
    }

    public Fragment getCurrentFragment() {
        return mFragments.get(mCurrentTab);
    }

    public void setCurrentFragment(int idx) {
        Fragment fragment = mFragments.get(idx);
        FragmentTransaction ft = obtainFragmentTransaction(idx);

        getCurrentFragment().onPause();
        if (fragment.isAdded()) {
            fragment.onResume();
        } else {
            ft.add(mFragmentContentId, fragment);
        }
        showTab(idx);
        ft.commitAllowingStateLoss();

        if (null != onTabChangeListener) {
            onTabChangeListener.OnTabChanged(idx);
        }
    }

    public void setOnTabChangeListener(OnTabChangeListener onTabChangeListener) {
        this.onTabChangeListener = onTabChangeListener;
    }

    public interface OnTabChangeListener {
        public void OnTabChanged(int index);
    }
}
