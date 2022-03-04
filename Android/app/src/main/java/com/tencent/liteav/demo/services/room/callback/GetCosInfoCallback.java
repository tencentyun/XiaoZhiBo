package com.tencent.liteav.demo.services.room.callback;

import com.tencent.liteav.demo.services.room.bean.http.ShowLiveCosInfo;

public interface GetCosInfoCallback {
    void onSuccess(ShowLiveCosInfo cosInfo);

    void onFailed(int code, String msg);
}
