package com.tencent.liteav.demo.services.room.http;

import com.tencent.liteav.demo.services.room.callback.ActionCallback;
import com.tencent.liteav.demo.services.room.callback.RoomDetailCallback;
import com.tencent.liteav.demo.services.room.callback.RoomInfoCallback;

public interface IHttpRoomManager {

    /**
     * 主播创建房间
     *
     * @param roomId
     * @param type
     * @param callback
     */
    void createRoom(String roomId, String type, final ActionCallback callback);

    /**
     * 主播销毁房间
     *
     * @param roomId
     * @param type
     * @param callback
     */
    void destroyRoom(String roomId, String type, final ActionCallback callback);

    /**
     * 更新房间信息
     *
     * @param roomId
     * @param callback
     */
    void updateRoom(String roomId, String title, String cover, final ActionCallback callback);

    /**
     * 获取房间列表
     *
     * @param type
     * @param callback
     */
    void getRoomList(final String type, final RoomInfoCallback callback);

    /**
     * 获取房间详情
     *
     * @param roomId
     * @param callback
     */
    void getRoomDetail(String roomId, final RoomDetailCallback callback);
}
