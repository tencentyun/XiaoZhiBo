package com.tencent.liteav.showlive.model.services.room.http;

import com.tencent.liteav.showlive.model.services.room.callback.ActionCallback;
import com.tencent.liteav.showlive.model.services.room.callback.RoomDetailCallback;
import com.tencent.liteav.showlive.model.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.showlive.model.services.room.http.impl.HttpRoomManager;

public interface IHttpRoomManager {

    /**
     * 进入房间
     *
     * @param roomId   房间号
     * @param type     房间类型
     * @param roleName 角色名称
     * @param callback 进房回调
     */
    void enterRoom(String roomId, String type, String roleName, final ActionCallback callback);

    /**
     * 退出房间
     *
     * @param roomId   房间号
     * @param callback 退房回调
     */
    void leaveRoom(String roomId, final ActionCallback callback);

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
     * @param type      房间类型
     * @param orderType 排序规则
     * @param callback  获取结果回调
     */
    void getRoomList(final String type, final HttpRoomManager.RoomOrderType orderType, final RoomInfoCallback callback);

    /**
     * 获取房间详情
     *
     * @param roomId
     * @param callback
     */
    void getRoomDetail(String roomId, final RoomDetailCallback callback);
}
