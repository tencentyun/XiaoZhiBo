package com.tencent.liteav.showlive.model.services.room.callback;

public interface ActionCallback {
    void onSuccess();

    void onFailed(int code, String msg);
}
