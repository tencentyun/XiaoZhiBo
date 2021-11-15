package com.tencent.liteav.demo.services.room.callback;

import com.tencent.liteav.demo.services.room.bean.RoomInfo;

public interface RoomDetailCallback {
    void onCallback(int code, String msg, RoomInfo info);
}
