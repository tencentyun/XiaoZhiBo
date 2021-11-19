package com.tencent.liteav.login.model;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.imsdk.v2.V2TIMSDKListener;
import com.tencent.liteav.basic.UserModel;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.debug.GenerateGlobalConfig;
import com.tencent.liteav.login.R;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.util.MD5Utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public class ProfileManager {
    private static final ProfileManager mOurInstance = new ProfileManager();

    private static final String TAG                      = "ProfileManager";
    public static final  int    ERROR_CODE_UNKNOWN       = -1;
    public static final  int    ERROR_CODE_NEED_REGISTER = -2;

    private static final String PER_DATA         = "per_profile_manager";
    private static final String PER_USER_PHONE   = "per_user_phone";
    private static final String PER_USER_ID      = "per_user_id";
    private static final String PER_APAASUSER_ID = "per_apaasuser_id";
    private static final String PER_TOKEN        = "per_user_token";
    private static final String PER_SDK_APP_ID   = "per_sdk_app_id";
    private static final String BASE_URL         = GenerateGlobalConfig.SERVERLESSURL;
    private static final int    MSG_KEEP_ALIVE   = 1001;
    private static final int    INTERVAL_TIME    = 10000; //10s

    private UserInfo                       mUserInfo;
    private String                         mUserPhone;
    private String                         mToken;
    private String                         mSessionId;
    private Retrofit                       mRetrofit;
    private Api                            mApi;
    private Call<ResponseEntityEmpty>      mRegisterCall;
    private Call<ResponseEntity<UserInfo>> mLoginCall;
    private Call<ResponseEntityEmpty>      mLoginOffCall;
    private boolean                        isLogin = false;
    private Context                        mContext;
    private String                         mUserId;
    private String                         mSdkAppid;
    private String                         mApaasUserId;
    private Handler                        mBackhandler;

    public static ProfileManager getInstance() {
        return mOurInstance;
    }

    public void initContext(Context context) {
        mContext = context;
    }

    private ProfileManager() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new HttpLogInterceptor());
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mApi = mRetrofit.create(Api.class);
        initBackHandler();

    }

    private void initBackHandler() {
        if (mBackhandler != null) {
            return;
        }
        //创建异步HandlerThread
        HandlerThread mHandlerThread = new HandlerThread("backHandler", Process.THREAD_PRIORITY_BACKGROUND);
        //必须先开启线程
        mHandlerThread.start();
        //子线程Handler
        mBackhandler = new Handler(mHandlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                keepAlive();
                if (mBackhandler != null) {
                    mBackhandler.sendEmptyMessageDelayed(MSG_KEEP_ALIVE, INTERVAL_TIME);
                }
                return false;
            }
        });
    }

    public boolean isLogin() {
        return isLogin;
    }

    public String getToken() {
        if (mToken == null) {
            loadToken();
        }
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
        SPUtils.getInstance(PER_DATA).put(PER_TOKEN, mToken);
    }

    private void loadToken() {
        mToken = SPUtils.getInstance(PER_DATA).getString(PER_TOKEN, "");
    }

    public void setApaasUserId(String apaasUserId) {
        mApaasUserId = apaasUserId;
        SPUtils.getInstance(PER_DATA).put(PER_APAASUSER_ID, mApaasUserId);
    }

    public String getApaasUserId() {
        if (mApaasUserId == null) {
            mApaasUserId = SPUtils.getInstance(PER_DATA).getString(PER_APAASUSER_ID, "");
        }
        return mApaasUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
        SPUtils.getInstance(PER_DATA).put(PER_USER_ID, mUserId);
    }

    public String getUserId() {
        if (mUserId == null) {
            mUserId = SPUtils.getInstance(PER_DATA).getString(PER_USER_ID, "");
        }
        return mUserId;
    }

    public void setSdkAppId(String sdkAppid) {
        mSdkAppid = sdkAppid;
        SPUtils.getInstance(PER_DATA).put(PER_SDK_APP_ID, mSdkAppid);
    }

    public int getSdkAppId() {
        if (mSdkAppid == null) {
            mSdkAppid = SPUtils.getInstance(PER_DATA).getString(PER_SDK_APP_ID, "");
        }
        if (mSdkAppid == "") {
            return 0;
        }
        return Integer.parseInt(mSdkAppid);
    }

    public void cancelRequest() {
        if (mRegisterCall != null) {
            mRegisterCall.cancel();
            mSessionId = null;
        }
        if (mLoginCall != null) {
            mLoginCall.cancel();
            mUserInfo = null;
        }
    }

    //注销登录
    public void logoff(final ActionCallback callback) {
        if (mLoginOffCall != null) {
            mLoginOffCall.cancel();
            mUserInfo = null;
        }
        // 构造注销请求参数
        if (TextUtils.isEmpty(mToken)) {
            mToken = getToken();
        }

        if (TextUtils.isEmpty(mApaasUserId)) {
            mApaasUserId = getApaasUserId();
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("token", mToken);
        internalLogOff(data, callback);
    }

    private void internalLogOff(Map<String, String> data, final ActionCallback callback) {
        mLoginOffCall = mApi.deleteUser(data);
        mLoginOffCall.enqueue(new Callback<ResponseEntityEmpty>() {
            @Override
            public void onResponse(Call<ResponseEntityEmpty> call, retrofit2.Response<ResponseEntityEmpty> response) {
                ResponseEntityEmpty res = response.body();
                if (res == null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "data is null");
                    return;
                }
                //注销失败
                if (res.errorCode != 0) {
                    callback.onFailed(res.errorCode, res.errorMessage);
                    return;
                }
                //注销登录后,需停止向服务器保活
                if (mBackhandler != null) {
                    mBackhandler.removeCallbacksAndMessages(null);
                    mBackhandler = null;
                }
                cancelRequest();
                setToken("");
                setUserId("");
                setApaasUserId("");
                setSdkAppId("");
                isLogin = false;
                // 注销登录成功后,将IM的头像和昵称设为空
                IMManager.sharedInstance().setNicknameAndAvatar("", "", new IMManager.Callback() {
                    @Override
                    public void onCallback(int errorCode, String message) {
                        if (errorCode == 0) {
                            UserModelManager manager = UserModelManager.getInstance();
                            UserModel userModel = manager.getUserModel();
                            userModel.userAvatar = null;
                            userModel.userName = null;
                            manager.setUserModel(userModel);
                            callback.onSuccess();
                        } else {
                            callback.onFailed(errorCode, message);
                            ToastUtils.showLong(mContext.getString(R.string.login_toast_failed_to_set_username),
                                    message);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseEntityEmpty> call, Throwable t) {
                isLogin = false;
                callback.onFailed(ERROR_CODE_UNKNOWN, "unknown error");
            }
        });
    }

    public void logout(final ActionCallback callback) {
        if (TextUtils.isEmpty(mToken)) {
            mToken = getToken();
        }
        if (TextUtils.isEmpty(mApaasUserId)) {
            mApaasUserId = getApaasUserId();
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("token", mToken);
        internalLogout(data, callback);
    }

    private void internalLogout(Map<String, String> data, final ActionCallback callback) {
        Call<ResponseEntityEmpty> logoutCall = mApi.logout(data);
        logoutCall.enqueue(new Callback<ResponseEntityEmpty>() {
            @Override
            public void onResponse(Call<ResponseEntityEmpty> call, retrofit2.Response<ResponseEntityEmpty> response) {
                if (response == null || response.body() == null) {
                    Log.d(TAG, "logout response is null");
                    return;
                }
                int errCode = response.body().errorCode;
                String errMsg = response.body().errorMessage;
                if (errCode != 0) {
                    Log.d(TAG, "logout failed errCode = " + errCode + " , errMsg = " + errMsg);
                    callback.onFailed(errCode, errMsg);
                    return;
                }
                //退出登录后,需停止向服务器保活
                if (mBackhandler != null) {
                    mBackhandler.removeCallbacksAndMessages(null);
                    mBackhandler = null;
                }
                cancelRequest();
                setToken("");
                setUserId("");
                setApaasUserId("");
                setSdkAppId("");
                isLogin = false;
                callback.onSuccess();
            }

            @Override
            public void onFailure(Call<ResponseEntityEmpty> call, Throwable t) {
                isLogin = false;
                callback.onFailed(ERROR_CODE_UNKNOWN, "logout unknown error");
            }
        });
    }

    public void register(String username, String password, final ActionCallback callback) {
        if (mRegisterCall != null) {
            mRegisterCall.cancel();
        }

        // 登录后台自定义规则：signature=md5(username-tag-ts-nonce-secret)，其中secret=md5(username-password)
        String salt = MD5Utils.getMD5String(String.format("%s-%s", username, password));
        Map<String, String> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("salt", salt);
        mRegisterCall = mApi.register(data);
        mRegisterCall.enqueue(new Callback<ResponseEntityEmpty>() {
            @Override
            public void onResponse(Call<ResponseEntityEmpty> call, retrofit2.Response<ResponseEntityEmpty> response) {
                ResponseEntityEmpty res = response.body();
                if (res == null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "register response is null");
                    return;
                }
                if (res.errorCode == 0) {
                    callback.onSuccess();
                } else {
                    callback.onFailed(res.errorCode, res.errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseEntityEmpty> call, Throwable t) {
                callback.onFailed(ERROR_CODE_UNKNOWN, "register unknown error");
            }

        });
    }

    public void login(String username, String password, final ActionCallback callback) {
        if (mLoginCall != null) {
            mLoginCall.cancel();
            mUserInfo = null;
        }

        // 登录后台自定义规则：signature=md5(username-tag-ts-nonce-secret)，其中secret=md5(username-password)
        String tag = "xiaozhibo";
        String ts = String.valueOf(System.currentTimeMillis());
        String nonce = "";
        String signature = MD5Utils.getMD5String(String.format("%s-%s-%s-%s-%s", username, tag, ts, nonce,
                MD5Utils.getMD5String(String.format("%s-%s", username, password))));

        Map<String, String> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("signature", signature);
        data.put("tag", tag);
        data.put("ts", ts);
        data.put("nonce", nonce);
        mLoginCall = mApi.login(data);
        internalLogin(data, callback);
    }

    public void autoLogin(String token, final ActionCallback callback) {
        if (mLoginCall != null) {
            mLoginCall.cancel();
            mUserInfo = null;
        }

        if (TextUtils.isEmpty(mApaasUserId)) {
            mApaasUserId = getApaasUserId();
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("token", token);
        mLoginCall = mApi.autologin(data);
        internalLogin(data, callback);
    }

    private void internalLogin(Map<String, String> data, final ActionCallback callback) {
        mLoginCall.enqueue(new Callback<ResponseEntity<UserInfo>>() {
            @Override
            public void onResponse(Call<ResponseEntity<UserInfo>> call,
                                   retrofit2.Response<ResponseEntity<UserInfo>> response) {
                ResponseEntity<UserInfo> res = response.body();
                if (res == null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "data is null");
                    return;
                }
                if (res.errorCode == 0 && res.data != null) {
                    final UserInfo userInfo = res.data;
                    mUserInfo = userInfo;
                    setToken(userInfo.token);
                    setUserId(userInfo.userId);
                    setApaasUserId(userInfo.apaasUserId);
                    setSdkAppId(userInfo.sdkAppId);
                    final UserModel userModel = new UserModel();
                    userModel.phone = userInfo.phone;
                    userModel.userId = userInfo.userId;
                    if (!TextUtils.isEmpty(userInfo.sdkUserSig)) {
                        userModel.userSig = userInfo.sdkUserSig;
                    }

                    final UserModelManager userModelManager = UserModelManager.getInstance();
                    userModelManager.setUserModel(userModel);
                    //登录成功后,需要每隔10s给后台发消息保活
                    if (mBackhandler != null) {
                        mBackhandler.sendEmptyMessageDelayed(MSG_KEEP_ALIVE, INTERVAL_TIME);
                    }
                    loginIM(userModel, new ActionCallback() {
                        @Override
                        public void onSuccess() {
                            isLogin = true;
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailed(int code, String msg) {
                            isLogin = false;
                            callback.onFailed(code, msg);
                        }
                    });
                } else {
                    isLogin = false;
                    setToken("");
                    setUserId("");
                    setApaasUserId("");
                    setSdkAppId("");
                    callback.onFailed(res.errorCode, res.errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<UserInfo>> call, Throwable t) {
                isLogin = false;
                callback.onFailed(ERROR_CODE_UNKNOWN, "unknown error");
            }
        });
    }

    private void keepAlive() {
        String apaasUserId = getApaasUserId();
        String token = getToken();
        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", apaasUserId);
        data.put("token", token);
        Call<ResponseEntityEmpty> mCall = mApi.keepAlive(data);
        mCall.enqueue(new Callback<ResponseEntityEmpty>() {
            @Override
            public void onResponse(Call<ResponseEntityEmpty> call, retrofit2.Response<ResponseEntityEmpty> response) {
                if (response == null || response.body() == null) {
                    Log.d(TAG, "keepAlive onResponse failed ");
                    return;
                }
                int errorCode = response.body().errorCode;
                String errorMessage = response.body().errorMessage;
//                Log.d(TAG, "keepAlive errorMessage = " + errorCode + " , errorMessage = " + errorMessage);
            }

            @Override
            public void onFailure(Call<ResponseEntityEmpty> call, Throwable t) {
                Log.d(TAG, "keepAlive onFailure: reason = " + t);
            }
        });

    }

    //更新用户信息
    private void userUpdate(String nickName, ActionCallback callback) {
        // 构造注销请求参数
        if (TextUtils.isEmpty(mToken)) {
            mToken = getToken();
        }

        if (TextUtils.isEmpty(mApaasUserId)) {
            mApaasUserId = getApaasUserId();
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put("apaasUserId", mApaasUserId);
        data.put("token", mToken);
        data.put("name", nickName);
        internalUserUpdate(data, callback);
    }

    private void internalUserUpdate(Map<String, String> data, final ActionCallback callback) {
        Call<ResponseEntityEmpty> userUpdateCall = mApi.userUpdate(data);
        userUpdateCall.enqueue(new Callback<ResponseEntityEmpty>() {
            @Override
            public void onResponse(Call<ResponseEntityEmpty> call, retrofit2.Response<ResponseEntityEmpty> response) {
                if (response == null || response.body() == null) {
                    Log.d(TAG, "userUpdate failed");
                    return;
                }
                int errCode = response.body().errorCode;
                String errMsg = response.body().errorMessage;
                if (errCode == 0) {
                    callback.onSuccess();
                } else {
                    callback.onFailed(errCode, errMsg);
                    ToastUtils.showLong(mContext.getString(R.string.login_toast_failed_to_set_username), errMsg);
                }
            }

            @Override
            public void onFailure(Call<ResponseEntityEmpty> call, Throwable t) {
                callback.onFailed(ERROR_CODE_UNKNOWN, "unknown error");
            }
        });

    }

    private void loginIM(final UserModel userModel, final ActionCallback callback) {
        if (mContext == null) {
            Log.d(TAG, "login im failed, context is null");
            return;
        }
        V2TIMSDKConfig config = new V2TIMSDKConfig();
        config.setLogLevel(V2TIMSDKConfig.V2TIM_LOG_DEBUG);
        boolean isInitIMSDK = TUILogin.init(mContext, ProfileManager.getInstance().getSdkAppId(), config,
                new V2TIMSDKListener() {
            @Override
            public void onConnecting() {
            }

            @Override
            public void onConnectSuccess() {

            }

            @Override
            public void onConnectFailed(int code, String error) {
                Log.e(TAG, "init im sdk error.");
            }
        });

        if (!isInitIMSDK) {
            if (callback != null) {
                callback.onFailed(-1, "init im sdk error.");
            }
            return;
        }

        String loginedUserId = V2TIMManager.getInstance().getLoginUser();
        if (loginedUserId != null && loginedUserId.equals(userModel.userId)) {
            // 已经登录过了
            Log.i(TAG, "login im success.");
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        TUILogin.login(userModel.userId, userModel.userSig, new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "login im fail, code:" + i + " msg:" + s);
                if (callback != null) {
                    callback.onFailed(i, s);
                }
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "login im success.");
                getUserInfoByUserId(userModel.userId, new GetUserInfoCallback() {
                    @Override
                    public void onSuccess(UserModel model) {
                        UserModelManager.getInstance().setUserModel(model);
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailed(int code, String msg) {
                        callback.onFailed(code, msg);
                    }
                });

            }
        });
    }

    public void setNickName(final String nickname, final ActionCallback callback) {
        IMManager.sharedInstance().setNickname(nickname, new IMManager.Callback() {
            @Override
            public void onCallback(int errorCode, String message) {
                if (errorCode == 0) {
                    UserModel userModel = UserModelManager.getInstance().getUserModel();
                    userModel.userName = nickname;
                    UserModelManager.getInstance().setUserModel(userModel);
                    callback.onSuccess();
                } else {
                    callback.onFailed(errorCode, message);
                    ToastUtils.showLong(mContext.getString(R.string.login_toast_failed_to_set_username), message);
                }
            }
        });
    }

    public void setAvatar(final String avatar, final ActionCallback callback) {
        IMManager.sharedInstance().setAvatar(avatar, new IMManager.Callback() {
            @Override
            public void onCallback(int errorCode, String message) {
                if (errorCode == 0) {
                    UserModel userModel = UserModelManager.getInstance().getUserModel();
                    userModel.userAvatar = avatar;
                    UserModelManager.getInstance().setUserModel(userModel);
                    callback.onSuccess();
                } else {
                    callback.onFailed(errorCode, message);
                    ToastUtils.showLong(mContext.getString(R.string.login_toast_failed_to_set_username), message);
                }
            }
        });
    }

    public void setNicknameAndAvatar(final String nickname, final String avatar, final ActionCallback callback) {
        userUpdate(nickname, new ActionCallback() {
            @Override
            public void onSuccess() {
                UserModel userModel = UserModelManager.getInstance().getUserModel();
                userModel.userAvatar = avatar;
                userModel.userName = nickname;
                UserModelManager.getInstance().setUserModel(userModel);
                callback.onSuccess();
            }

            @Override
            public void onFailed(int code, String msg) {
                callback.onFailed(code, msg);
                ToastUtils.showLong(mContext.getString(R.string.login_toast_failed_to_set_username), msg);
            }
        });
        IMManager.sharedInstance().setNicknameAndAvatar(nickname, avatar, new IMManager.Callback() {
            @Override
            public void onCallback(int errorCode, String message) {
                if (errorCode == 0) {
                    UserModel userModel = UserModelManager.getInstance().getUserModel();
                    userModel.userAvatar = avatar;
                    userModel.userName = nickname;
                    UserModelManager.getInstance().setUserModel(userModel);
                    callback.onSuccess();
                } else {
                    callback.onFailed(errorCode, message);
                    ToastUtils.showLong(mContext.getString(R.string.login_toast_failed_to_set_username), message);
                }
            }
        });
    }

    public NetworkAction getUserInfoByUserId(String userId, final GetUserInfoCallback callback) {
        if (TextUtils.isEmpty(mToken)) {
            mToken = SPUtils.getInstance(PER_DATA).getString(PER_TOKEN, "");
        }
        Map<String, String> data = new LinkedHashMap<>();
        data.put("userId", userId);
        data.put("token", mToken);
        return internalGetUserInfo(data, callback);
    }

    public NetworkAction getUserInfoByPhone(String phone, final GetUserInfoCallback callback) {
        if (TextUtils.isEmpty(mToken)) {
            mToken = SPUtils.getInstance(PER_DATA).getString(PER_TOKEN, "");
        }
        Map<String, String> data = new LinkedHashMap<>();
        data.put("phone", phone);
        data.put("token", mToken);
        return internalGetUserInfo(data, callback);
    }

    private NetworkAction internalGetUserInfo(Map<String, String> data, final GetUserInfoCallback callback) {
        Call<ResponseEntity<UserInfo>> call = mApi.getUserInfo(data);
        call.enqueue(new Callback<ResponseEntity<UserInfo>>() {
            @Override
            public void onResponse(Call<ResponseEntity<UserInfo>> call,
                                   retrofit2.Response<ResponseEntity<UserInfo>> response) {
                ResponseEntity<UserInfo> res = response.body();
                if (res == null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "data is null");
                    return;
                }
                if (res.errorCode == 0 && res.data != null) {
                    UserInfo userInfo = res.data;
                    final UserModel userModel = UserModelManager.getInstance().getUserModel();
                    userModel.userId = userInfo.userId;
                    IMManager.sharedInstance().getUserInfo(userModel.userId, new IMManager.UserCallback() {
                        @Override
                        public void onCallback(int code, String msg, IMUserInfo userInfo) {

                            if (code == 0) {
                                if (userInfo == null) {
                                    callback.onFailed(ERROR_CODE_UNKNOWN, "user info get is null");
                                    return;
                                }
                                // 如果第一次没有设置用户名，跳转注册用户名
                                if (TextUtils.isEmpty(userInfo.userName)) {
                                    callback.onFailed(ERROR_CODE_NEED_REGISTER,
                                            mContext.getString(R.string.login_not_register));
                                } else {
                                    userModel.userName = userInfo.userName;
                                    userModel.userAvatar = userInfo.userAvatar;
                                    callback.onSuccess(userModel);
                                }
                            } else {
                                callback.onFailed(code, msg);
                            }
                        }
                    });
                } else {
                    callback.onFailed(res.errorCode, res.errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<UserInfo>> call, Throwable t) {
                callback.onFailed(ERROR_CODE_UNKNOWN, "");
            }
        });
        return new NetworkAction(call);
    }

    /**
     * ==== 网络层相关 ====
     */
    private interface Api {
        @GET("base/v1/oauth/register")
        Call<ResponseEntityEmpty> register(@QueryMap Map<String, String> map);

        @GET("base/v1/oauth/signature")
        Call<ResponseEntity<UserInfo>> login(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_login_token")
        Call<ResponseEntity<UserInfo>> autologin(@QueryMap Map<String, String> map);

        //用户登录后,每隔10s向服务器保活
        @GET("base/v1/auth_users/user_keepalive")
        Call<ResponseEntityEmpty> keepAlive(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_logout")
        Call<ResponseEntityEmpty> logout(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_delete")
        Call<ResponseEntityEmpty> deleteUser(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_doctor_auth")
        Call<ResponseEntity<UserInfo>> getDoctorAuth(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_update")
        Call<ResponseEntityEmpty> userUpdate(@QueryMap Map<String, String> map);

        @GET("base/v1/auth_users/user_query")
        Call<ResponseEntity<UserInfo>> getUserInfo(@QueryMap Map<String, String> map);

        @POST("getUserInfoBatch")
        @FormUrlEncoded
        Call<ResponseEntity<List<UserInfo>>> getUserInfoBatch(@FieldMap Map<String, String> map);
    }


    public class ResponseEntityEmpty {
        public int    errorCode;
        public String errorMessage;
    }


    private class ResponseEntity<T> {
        public int    errorCode;
        public String errorMessage;
        public T      data;
    }


    private class UserInfo {
        public String userId;
        public String apaasAppId;
        public String apaasUserId;
        public String sdkAppId;
        public String sdkUserSig;
        public String token;
        public String expire;
        public String phone;
        public String email;
        public String name;
        public String avatar;

        @Override
        public String toString() {
            return "UserInfo{"
                    + "userId='" + userId + '\''
                    + ", apaasAppId='" + apaasAppId + '\''
                    + ", apaasUserId='" + apaasUserId + '\''
                    + ", sdkAppId='" + sdkAppId + '\''
                    + ", sdkUserSig='" + sdkUserSig + '\''
                    + ", token='" + token + '\''
                    + ", expire='" + expire + '\''
                    + ", phone='" + phone + '\''
                    + ", email='" + email + '\''
                    + ", name='" + name + '\''
                    + ", avatar='" + avatar + '\''
                    + '}';
        }
    }


    private class RegisterModel {
        public String sessionId;
    }


    /**
     * okhttp 拦截器
     */

    public static class HttpLogInterceptor implements Interceptor {
        private static final String  TAG  = "HttpLogInterceptor";
        private static final Charset UTF8 = Charset.forName("UTF-8");

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            RequestBody requestBody = request.body();
            String body = null;
            if (requestBody != null) {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                body = buffer.readString(charset);
            }

            Log.d(TAG,
                    "发送请求: method：" + request.method()
                            + "\nurl：" + request.url()
                            + "\n请求头：" + request.headers()
                            + "\n请求参数: " + body);

            long startNs = System.nanoTime();
            Response response = chain.proceed(request);
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            ResponseBody responseBody = response.body();
            String rBody;

            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    e.printStackTrace();
                }
            }
            rBody = buffer.clone().readString(charset);

            Log.d(TAG,
                    "收到响应: code:" + response.code()
                            + "\n请求url：" + response.request().url()
                            + "\n请求body：" + body
                            + "\nResponse: " + rBody);

            return response;
        }
    }


    public static class NetworkAction {
        private Call retrofitCall;

        public NetworkAction() {
        }

        public NetworkAction(Call call) {
            retrofitCall = call;
        }

        public void cancel() {
            if (retrofitCall != null) {
                retrofitCall.cancel();
            }
        }
    }


    // 操作回调
    public interface ActionCallback {
        void onSuccess();

        void onFailed(int code, String msg);
    }


    // IM登录回调
    public interface IMActionCallback {
        void onSuccess();

        void onFailed(int code, String msg);
    }


    // 通过userid/phone获取用户信息回调
    public interface GetUserInfoCallback {
        void onSuccess(UserModel model);

        void onFailed(int code, String msg);
    }


    // 通过userId批量获取用户信息回调
    public interface GetUserInfoBatchCallback {
        void onSuccess(List<UserModel> model);

        void onFailed(int code, String msg);
    }


    // 验证码数据回调
    public interface VerifyIdCallback {
        void onSuccess(String appId);

        void onFailed(int code, String msg);
    }

}
