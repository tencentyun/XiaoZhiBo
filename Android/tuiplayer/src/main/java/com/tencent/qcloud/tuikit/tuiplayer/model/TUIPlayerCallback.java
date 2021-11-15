package com.tencent.qcloud.tuikit.tuiplayer.model;

public interface TUIPlayerCallback {
    int    SUCCESS_CODE    = 0;
    String SUCCESS_MESSAGE = "success";

    void onResult(int code, String message);
}
