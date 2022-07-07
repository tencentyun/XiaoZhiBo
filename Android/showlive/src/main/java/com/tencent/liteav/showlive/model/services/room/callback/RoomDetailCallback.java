package com.tencent.liteav.showlive.model.services.room.callback;

import com.tencent.liteav.showlive.model.services.room.bean.RoomInfo;

public interface RoomDetailCallback {
    void onCallback(int code, String msg, RoomInfo info);
}
