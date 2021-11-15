package com.tencent.liteav.demo.services;

import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.demo.services.room.callback.RoomInfoCallback;

public interface IRoomService {

    /**
     * 主播 创建房间
     *
     * @param roomId
     * @param roomName
     * @param coverUrl
     * @param callback
     */
    void createRoom(final String roomId, final String type, final String roomName, final String coverUrl, final CommonCallback callback);

    /**
     * 主播 销毁房间
     *
     * @param roomId
     * @param callback
     */
    void destroyRoom(final String roomId, final String type, final CommonCallback callback);

    /**
     * 观众 进入房间
     *
     * @param roomId
     * @param callback
     */
    void enterRoom(int roomId, final CommonCallback callback);


    /**
     * 观众退出房间
     *
     * @param roomId
     * @param callback
     */
    void exitRoom(int roomId, CommonCallback callback);

    /**
     * 获取房间列表
     *
     * @param type
     * @param callback
     */
    void getRoomList(final String type, final RoomInfoCallback callback);
}
