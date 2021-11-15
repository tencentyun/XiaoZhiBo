package com.tencent.liteav.demo.services.room.callback;

import com.tencent.liteav.demo.services.room.bean.RoomInfo;

import java.util.List;

public interface RoomInfoCallback {
    void onCallback(int code, String msg, List<RoomInfo> list);
}
