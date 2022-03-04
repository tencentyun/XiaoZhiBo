package com.tencent.liteav.demo.services.room.http.impl;

import android.graphics.Bitmap;

import com.tencent.liteav.debug.GenerateGlobalConfig;
import com.tencent.liteav.demo.services.room.bean.RoomInfo;
import com.tencent.liteav.demo.services.room.bean.http.RoomDetail;
import com.tencent.liteav.demo.services.room.bean.http.ShowLiveCosInfo;
import com.tencent.liteav.demo.services.room.callback.ActionCallback;
import com.tencent.liteav.demo.services.room.callback.GetCosInfoCallback;
import com.tencent.liteav.demo.services.room.callback.RoomDetailCallback;
import com.tencent.liteav.demo.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.demo.services.room.http.IHttpRoomManager;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.qcloud.tuicore.TUILogin;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;

public class HttpRoomManager implements IHttpRoomManager {
    private static final HttpRoomManager mOurInstance = new HttpRoomManager();

    public static final int ERROR_CODE_UNKNOWN = -1;

    public static final String TYPE_MLVB_SHOW_LIVE     = "mlvb-show-live";
    public static final String TYPE_MLVB_SHOPPING_LIVE = "mlvb-shopping-live";

    private static final String BASE_URL = GenerateGlobalConfig.SERVERLESSURL;


    private final Retrofit mRetrofit;
    private final Api      mApi;
    private       int      mSdkAppId;

    private Call<ResponseEntity<Void>>             mEnterRoomCall;
    private Call<ResponseEntity<Void>>             mLeaveRoomCall;
    private Call<ResponseEntity<Void>>             mUpdateRoomCall;
    private Call<ResponseEntity<Void>>             mDestroyRoomCall;
    private Call<ResponseEntity<List<RoomDetail>>> mGetRoomListCall;
    private Call<ResponseEntity<RoomDetail>>       mGetRoomDetailCall;
    private Call<ResponseEntity<ShowLiveCosInfo>>  mGetRoomCosInfo;
    private Call<Void>                             mUploadAvatar;

    public static HttpRoomManager getInstance() {
        return mOurInstance;
    }

    private HttpRoomManager() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new ProfileManager.HttpLogInterceptor());
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mApi = mRetrofit.create(Api.class);
    }

    /**
     * 需要先设置一个sdkappid
     *
     * @param sdkAppId
     */
    public void initSdkAppId(int sdkAppId) {
        mSdkAppId = sdkAppId;
    }

    public enum RoomOrderType {
        CREATE_UTC("createUtc"),       // 按时间倒序
        TOTAL_JOINED("totalJoined");   // 按房间人数倒序

        String type;

        RoomOrderType(String type) {
            this.type = type;
        }
    }

    @Override
    public void enterRoom(String roomId, String type, String roleName, final ActionCallback callback) {
        if (mEnterRoomCall != null && mEnterRoomCall.isExecuted()) {
            mEnterRoomCall.cancel();
        }
        Map<String, String> param = new HashMap<>();
        param.put("appId", String.valueOf(mSdkAppId));
        param.put("userId", TUILogin.getUserId());
        param.put("token", ProfileManager.getInstance().getToken());
        param.put("roomId", roomId);
        param.put("role", roleName);
        param.put("category", type);
        param.put("apaasUserId", ProfileManager.getInstance().getApaasUserId());
        mEnterRoomCall = mApi.enterRoom(param);
        mEnterRoomCall.enqueue(new Callback<ResponseEntity<Void>>() {
            @Override
            public void onResponse(Call<ResponseEntity<Void>> call, Response<ResponseEntity<Void>> response) {
                ResponseEntity res = response.body();
                if (res.errorCode == 0) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed(res.errorCode, res.errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<Void>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "未知错误");
                }
            }
        });
    }

    @Override
    public void leaveRoom(String roomId, final ActionCallback callback) {
        if (mLeaveRoomCall != null && mLeaveRoomCall.isExecuted()) {
            mLeaveRoomCall.cancel();
        }
        Map<String, String> param = new HashMap<>();
        param.put("userId", TUILogin.getUserId());
        param.put("token", ProfileManager.getInstance().getToken());
        param.put("roomId", roomId);
        param.put("apaasUserId", ProfileManager.getInstance().getApaasUserId());
        mLeaveRoomCall = mApi.leaveRoom(param);
        mLeaveRoomCall.enqueue(new Callback<ResponseEntity<Void>>() {
            @Override
            public void onResponse(Call<ResponseEntity<Void>> call, Response<ResponseEntity<Void>> response) {
                ResponseEntity res = response.body();
                if (res.errorCode == 0) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed(res.errorCode, res.errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<Void>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "未知错误");
                }
            }
        });
    }

    @Override
    public void destroyRoom(String roomId, String type, final ActionCallback callback) {
        if (mDestroyRoomCall != null && mDestroyRoomCall.isExecuted()) {
            mDestroyRoomCall.cancel();
        }
        Map<String, String> param = new HashMap<>();
        param.put("userId", TUILogin.getUserId());
        param.put("token", ProfileManager.getInstance().getToken());
        param.put("roomId", roomId);
        param.put("apaasUserId", ProfileManager.getInstance().getApaasUserId());
        mDestroyRoomCall = mApi.destroyRoom(param);
        mDestroyRoomCall.enqueue(new Callback<ResponseEntity<Void>>() {
            @Override
            public void onResponse(Call<ResponseEntity<Void>> call, Response<ResponseEntity<Void>> response) {
                ResponseEntity res = response.body();
                if (res.errorCode == 0) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed(res.errorCode, res.errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<Void>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "未知错误");
                }
            }
        });
    }

    @Override
    public void updateRoom(String roomId, String title, String cover, final ActionCallback callback) {
        if (mUpdateRoomCall != null && mUpdateRoomCall.isExecuted()) {
            mUpdateRoomCall.cancel();
        }
        Map<String, String> param = new HashMap<>();
        param.put("appId", String.valueOf(mSdkAppId));
        param.put("userId", TUILogin.getUserId());
        param.put("token", ProfileManager.getInstance().getToken());
        param.put("roomId", roomId);
        param.put("title", title);
        param.put("cover", cover);
        param.put("apaasUserId", ProfileManager.getInstance().getApaasUserId());

        mUpdateRoomCall = mApi.updateRoom(param);
        mUpdateRoomCall.enqueue(new Callback<ResponseEntity<Void>>() {
            @Override
            public void onResponse(Call<ResponseEntity<Void>> call, Response<ResponseEntity<Void>> response) {
                ResponseEntity res = response.body();
                if (res.errorCode == 0) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed(res.errorCode, res.errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<Void>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "未知错误");
                }
            }
        });
    }

    @Override
    public void getRoomList(final String type, final RoomOrderType orderType, final RoomInfoCallback callback) {
        if (mGetRoomListCall != null && mGetRoomListCall.isExecuted()) {
            mGetRoomListCall.cancel();
        }
        Map<String, String> param = new HashMap<>();
        param.put("appId", String.valueOf(mSdkAppId));
        param.put("userId", TUILogin.getUserId());
        param.put("token", ProfileManager.getInstance().getToken());
        param.put("category", type);
        param.put("orderBy", orderType.type);
        param.put("apaasUserId", ProfileManager.getInstance().getApaasUserId());
        mGetRoomListCall = mApi.getRoomList(param);
        mGetRoomListCall.enqueue(new Callback<ResponseEntity<List<RoomDetail>>>() {
            @Override
            public void onResponse(Call<ResponseEntity<List<RoomDetail>>> call,
                                   Response<ResponseEntity<List<RoomDetail>>> response) {
                ResponseEntity res = response.body();
                if (res.errorCode == 0 && res.data != null) {
                    List<RoomDetail> roomDetailList = (List<RoomDetail>) res.data;
                    callback.onCallback(res.errorCode, res.errorMessage, convertRoomInfoList(roomDetailList));
                } else {
                    if (callback != null) {
                        callback.onCallback(res.errorCode, res.errorMessage, new ArrayList<RoomInfo>());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<List<RoomDetail>>> call, Throwable t) {
                if (callback != null) {
                    callback.onCallback(ERROR_CODE_UNKNOWN, "unknown error", new ArrayList<RoomInfo>());
                }
            }
        });
    }

    @Override
    public void getRoomDetail(String roomId, final RoomDetailCallback callback) {
        if (mGetRoomDetailCall != null && mGetRoomDetailCall.isExecuted()) {
            mGetRoomDetailCall.cancel();
        }
        Map<String, String> param = new HashMap<>();
        param.put("userId", TUILogin.getUserId());
        param.put("token", ProfileManager.getInstance().getToken());
        param.put("roomId", roomId);
        param.put("apaasUserId", ProfileManager.getInstance().getApaasUserId());
        mGetRoomDetailCall = mApi.getRoomInfo(param);
        mGetRoomDetailCall.enqueue(new Callback<ResponseEntity<RoomDetail>>() {
            @Override
            public void onResponse(Call<ResponseEntity<RoomDetail>> call,
                                   Response<ResponseEntity<RoomDetail>> response) {
                ResponseEntity res = response.body();
                if (res.errorCode == 0 && res.data != null) {
                    RoomDetail roomDetail = (RoomDetail) res.data;
                    callback.onCallback(res.errorCode, res.errorMessage, convertRoomInfo(roomDetail));
                } else {
                    if (callback != null) {
                        callback.onCallback(res.errorCode, res.errorMessage, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<RoomDetail>> call, Throwable t) {
                if (callback != null) {
                    callback.onCallback(ERROR_CODE_UNKNOWN, "unknown error", null);
                }
            }
        });
    }

    @Override
    public void getRoomCosInfo(String roomCosType, final GetCosInfoCallback callback) {
        if (mGetRoomCosInfo != null && mGetRoomCosInfo.isExecuted()) {
            mGetRoomCosInfo.cancel();
        }
        Map<String, String> param = new HashMap<>();
        param.put("appId", String.valueOf(mSdkAppId));
        param.put("category", roomCosType);
        param.put("userId", TUILogin.getUserId());
        param.put("token", ProfileManager.getInstance().getToken());
        param.put("apaasUserId", ProfileManager.getInstance().getApaasUserId());
        mGetRoomCosInfo = mApi.getCosInfo(param);
        mGetRoomCosInfo.enqueue(new Callback<ResponseEntity<ShowLiveCosInfo>>() {
            @Override
            public void onResponse(Call<ResponseEntity<ShowLiveCosInfo>> call,
                                   Response<ResponseEntity<ShowLiveCosInfo>> response) {
                ResponseEntity res = response.body();
                if (res.errorCode == 0 && res.data != null) {
                    ShowLiveCosInfo cosInfo = (ShowLiveCosInfo) res.data;
                    callback.onSuccess(cosInfo);
                } else {
                    if (callback != null) {
                        callback.onFailed(res.errorCode, res.errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<ShowLiveCosInfo>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "unknown error");
                }
            }
        });
    }

    private Map<String, RequestBody> convertParam(Map<String, Object> map) {
        Map<String, RequestBody> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"),
                    String.valueOf(entry.getValue()));
            resultMap.put(entry.getKey(), body);
        }
        return resultMap;
    }

    @Override
    public void uploadRoomAvatar(Bitmap bitmap, String url, String fileName, Map<String, Object> map,
                                 final ActionCallback callback) {
        if (mUploadAvatar != null && mUploadAvatar.isExecuted()) {
            mUploadAvatar.cancel();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), stream.toByteArray());
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestBody);
        mUploadAvatar = mApi.uploadAvatar(url, convertParam(map), part);
        mUploadAvatar.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (callback != null) {
                    callback.onFailed(ERROR_CODE_UNKNOWN, "未知错误");
                }
            }
        });
    }

    private RoomInfo convertRoomInfo(RoomDetail roomDetail) {
        if (roomDetail == null) {
            return null;
        }
        RoomInfo info = new RoomInfo();
        info.roomId = roomDetail.roomId;
        info.roomName = roomDetail.title;
        info.ownerId = roomDetail.roomId;
        info.ownerName = roomDetail.ownBy;
        info.coverUrl = roomDetail.cover;
        info.memberCount = roomDetail.nnUsers;
        info.ownerAvatar = roomDetail.cover;
        info.roomStatus = roomDetail.status;
        info.totalJoined = roomDetail.totalJoined;
        return info;
    }


    private List<RoomInfo> convertRoomInfoList(List<RoomDetail> roomDetailList) {
        if (roomDetailList == null) {
            return null;
        }
        List<RoomInfo> infoList = new ArrayList<>();
        for (int i = 0; i < roomDetailList.size(); i++) {
            infoList.add(convertRoomInfo(roomDetailList.get(i)));
        }
        return infoList;
    }

    /**
     * ==== 网络层相关 ====
     */
    private interface Api {
        @POST("base/v1/rooms/enter_room")
        @FormUrlEncoded
        Call<ResponseEntity<Void>> enterRoom(@FieldMap Map<String, String> map);

        @POST("base/v1/rooms/leave_room")
        @FormUrlEncoded
        Call<ResponseEntity<Void>> leaveRoom(@FieldMap Map<String, String> map);

        @POST("base/v1/rooms/update_room")
        @FormUrlEncoded
        Call<ResponseEntity<Void>> updateRoom(@FieldMap Map<String, String> map);

        @POST("base/v1/rooms/destroy_room")
        @FormUrlEncoded
        Call<ResponseEntity<Void>> destroyRoom(@FieldMap Map<String, String> map);


        @POST("base/v1/rooms/query_room")
        @FormUrlEncoded
        Call<ResponseEntity<List<RoomDetail>>> getRoomList(@FieldMap Map<String, String> map);

        @POST("base/v1/rooms/room_detail")
        @FormUrlEncoded
        Call<ResponseEntity<RoomDetail>> getRoomInfo(@FieldMap Map<String, String> map);

        @POST("base/v1/cos/config")
        @FormUrlEncoded
        Call<ResponseEntity<ShowLiveCosInfo>> getCosInfo(@FieldMap Map<String, String> map);

        @POST
        @Multipart
        Call<Void> uploadAvatar(@Url String url, @PartMap Map<String, RequestBody> params,
                                @Part MultipartBody.Part parts);
    }


    private class ResponseEntity<T> {
        public int    errorCode;
        public String errorMessage;
        public T      data;
    }


    public enum RoomCosType {
        AVATAR("avatar");

        RoomCosType(String type) {
            this.type = type;
        }

        private String type;

        public String getType() {
            return type;
        }
    }


    public enum RoomRole {
        ANCHOR("anchor"),
        GUEST("audience");

        RoomRole(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return this.name;
        }
    }
}
