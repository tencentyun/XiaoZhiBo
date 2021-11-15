package com.tencent.liteav.demo.services.room.callback;

public interface ActionCallback {
    void onSuccess();

    void onFailed(int code, String msg);
}
