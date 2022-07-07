package com.tencent.liteav.demo.app;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.showlive.ui.ShowLiveAudienceActivity;
import com.tencent.liteav.showlive.ui.floatwindow.IFloatWindowCallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 应用Application
 * <p>
 * 用于但不仅限于对第三方库做初始化操作、Activity的生命周期的管理等等
 * </p>
 */
public class DemoApplication extends MultiDexApplication {
    private static String          TAG = "DemoApplication";
    private static DemoApplication instance;

    private IFloatWindowCallback mCallback;

    @Override
    public void onCreate() {

        super.onCreate();
        instance = this;

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
        closeAndroidPDialog();
        ProfileManager.getInstance().initContext(this);
        getApplication().registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (activity instanceof ShowLiveAudienceActivity) {
                    if (mCallback != null) {
                        mCallback.onAppBackground(false);
                    }
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (activity instanceof ShowLiveAudienceActivity) {
                    if (mCallback != null) {
                        mCallback.onAppBackground(true);
                    }
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public static DemoApplication getApplication() {
        return instance;
    }

    private void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCallback(IFloatWindowCallback callback) {
        mCallback = callback;
        if (mCallback != null) {
            boolean is = mCallback instanceof IFloatWindowCallback;
        }
    }
}