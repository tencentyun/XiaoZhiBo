package com.tencent.liteav.login.model;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;

import java.util.ArrayList;
import java.util.List;

public class IMManager {
    private static final String    TAG = "IMManager";
    private static       IMManager sInstance;
    private              boolean   mIsLogin;

    public static synchronized IMManager sharedInstance() {
        if (sInstance == null) {
            sInstance = new IMManager();
        }
        return sInstance;
    }

    private void setLogin(boolean isLogin) {
        mIsLogin = isLogin;
    }

    public boolean isLogin() {
        return mIsLogin;
    }

    public void setNickname(final String userName, final Callback callback) {
        V2TIMUserFullInfo v2TIMUserFullInfo = new V2TIMUserFullInfo();
        v2TIMUserFullInfo.setNickname(userName);
        V2TIMManager.getInstance().setSelfInfo(v2TIMUserFullInfo, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                Log.e(TAG, "set profile code:" + code + " msg:" + desc);
                if (callback != null) {
                    callback.onCallback(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "set profile success.");
                if (callback != null) {
                    callback.onCallback(0, "set profile success.");
                }
            }
        });
    }

    public void setAvatar(final String avatarUrl, final Callback callback) {
        V2TIMUserFullInfo v2TIMUserFullInfo = new V2TIMUserFullInfo();
        v2TIMUserFullInfo.setFaceUrl(avatarUrl);
        V2TIMManager.getInstance().setSelfInfo(v2TIMUserFullInfo, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                Log.e(TAG, "set profile code:" + code + " msg:" + desc);
                if (callback != null) {
                    callback.onCallback(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "set profile success.");
                if (callback != null) {
                    callback.onCallback(0, "set profile success.");
                }
            }
        });
    }

    public void setNicknameAndAvatar(final String nickname, final String avatarUrl, final Callback callback) {
        V2TIMUserFullInfo v2TIMUserFullInfo = new V2TIMUserFullInfo();
        v2TIMUserFullInfo.setFaceUrl(avatarUrl);
        v2TIMUserFullInfo.setNickname(nickname);
        V2TIMManager.getInstance().setSelfInfo(v2TIMUserFullInfo, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                Log.e(TAG, "set profile code:" + code + " msg:" + desc);
                if (callback != null) {
                    callback.onCallback(code, desc);
                }
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "set profile success.");
                if (callback != null) {
                    callback.onCallback(0, "set profile success.");
                }
            }
        });
    }

    public void getUserInfo(final String userId, final UserCallback callback) {
        if (TextUtils.isEmpty(userId)) {
            Log.e(TAG, "get user info list fail, user list is empty.");
            if (callback != null) {
                callback.onCallback(-1, "get user info list fail, user list is empty.", null);
            }
            return;
        }
        List<String> userList = new ArrayList<>();
        userList.add(userId);
        Log.i(TAG, "get user info list " + userList);
        V2TIMManager.getInstance().getUsersInfo(userList, new V2TIMValueCallback<List<V2TIMUserFullInfo>>() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "get user info list fail, code:" + i);
                if (callback != null) {
                    callback.onCallback(i, s, null);
                }
            }

            @Override
            public void onSuccess(List<V2TIMUserFullInfo> v2TIMUserFullInfos) {
                List<IMUserInfo> list = new ArrayList<>();
                if (v2TIMUserFullInfos != null && v2TIMUserFullInfos.size() != 0) {
                    for (int i = 0; i < v2TIMUserFullInfos.size(); i++) {
                        IMUserInfo userInfo = new IMUserInfo();
                        userInfo.userName = v2TIMUserFullInfos.get(i).getNickName();
                        userInfo.userId = v2TIMUserFullInfos.get(i).getUserID();
                        userInfo.userAvatar = v2TIMUserFullInfos.get(i).getFaceUrl();
                        list.add(userInfo);
                    }
                }
                if (callback != null) {
                    callback.onCallback(0, "success", list.get(0));
                }
            }
        });
    }

    public void login(String userId, String userSig, final ActionCallback callback) {
        V2TIMManager.getInstance().login(userId, userSig, new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                callback.onFailed(i, s);
            }

            @Override
            public void onSuccess() {
                setLogin(true);
                callback.onSuccess();
            }
        });
    }

    public interface ActionCallback {
        void onSuccess();

        void onFailed(int code, String msg);
    }


    public interface Callback {
        void onCallback(int errorCode, String message);
    }


    public interface UserCallback {
        void onCallback(int code, String msg, IMUserInfo userInfo);
    }
}
