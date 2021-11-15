package com.tencent.liteav.demo.services.room.im.listener;


import com.tencent.liteav.demo.services.room.bean.RoomInfo;

import java.util.List;

public interface RoomInfoListCallback {
    void onCallback(int code, String msg, List<RoomInfo> list);
}
