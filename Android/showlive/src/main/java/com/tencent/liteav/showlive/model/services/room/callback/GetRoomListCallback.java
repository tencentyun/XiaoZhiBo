package com.tencent.liteav.showlive.model.services.room.callback;

import java.util.List;

public interface GetRoomListCallback {
    void onSuccess(List<String> roomIdList);

    void onFailed(int code, String msg);
}
