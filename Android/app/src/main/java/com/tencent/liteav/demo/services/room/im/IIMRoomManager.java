package com.tencent.liteav.demo.services.room.im;

import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.demo.services.room.im.listener.RoomInfoListCallback;

import java.util.List;

/**
 * IM相关的接口封装
 */
public interface IIMRoomManager {

    /**
     * 主播创建房间
     * @param roomId
     * @param roomName
     * @param coverUrl
     * @param callback
     */
    void createRoom(String roomId, String roomName, String coverUrl, CommonCallback callback);

    /**
     * 主播销毁房间
     * @param roomId
     * @param callback
     */
    void destroyRoom(String roomId, CommonCallback callback);

    /**
     * 观众加入房间
     * @param roomId
     * @param callback
     */
    void joinGroup(String roomId, CommonCallback callback);

    /**
     * 观众退出房间
     * @param roomId
     * @param callback
     */
    void quitGroup(String roomId, CommonCallback callback);

    /**
     * 获取房间列表
     * @param roomId
     * @param callback
     */
    void getRoomInfos(List<String> roomId, RoomInfoListCallback callback);
}
