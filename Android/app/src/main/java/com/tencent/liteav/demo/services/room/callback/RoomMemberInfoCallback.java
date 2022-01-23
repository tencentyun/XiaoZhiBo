package com.tencent.liteav.demo.services.room.callback;

import com.tencent.liteav.demo.services.room.bean.AudienceInfo;

import java.util.List;

/**
 * 房间内成员列表回调
 */
public interface RoomMemberInfoCallback {
    void onCallback(int code, String msg, List<AudienceInfo> list);
}
