package com.tencent.liteav.demo.services.room.callback;

import java.util.List;

public interface GetRoomListCallback {
    void onSuccess(List<String> roomIdList);

    void onFailed(int code, String msg);
}
