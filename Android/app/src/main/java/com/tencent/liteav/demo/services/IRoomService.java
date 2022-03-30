package com.tencent.liteav.demo.services;

import android.graphics.Bitmap;

import com.tencent.liteav.demo.services.room.callback.ActionCallback;
import com.tencent.liteav.demo.services.room.callback.GetCosInfoCallback;
import com.tencent.liteav.demo.services.room.callback.RoomMemberInfoCallback;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.demo.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager;
import com.tencent.liteav.demo.services.room.im.listener.RoomInfoListCallback;

import java.util.Map;

public interface IRoomService {

    /**
     * 主播 创建房间
     *
     * @param roomId
     * @param roomName
     * @param coverUrl
     * @param callback
     */
    void createRoom(final String roomId, final String type, final String roomName,
                    final String coverUrl, final CommonCallback callback);

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
     * @param roomId   房间号
     * @param type     房间类型
     * @param callback 回调
     */
    void enterRoom(int roomId, String type, final CommonCallback callback);


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
     * @param type      房间类型
     * @param orderType 排序规则
     * @param callback  结果回调
     */
    void getRoomList(final String type, HttpRoomManager.RoomOrderType orderType, final RoomInfoCallback callback);

    /**
     * 获取房间观众列表
     *
     * @param roomId   房间号
     * @param callback 获取结果回调
     */
    void getRoomAudienceList(String roomId, RoomMemberInfoCallback callback);

    /**
     * 添加单向好友
     *
     * @param userId   添加者id
     * @param friendId 好友id
     * @param callback 添加回调
     */
    void addFriend(String userId, String friendId, CommonCallback callback);

    /**
     * 删除单向好友
     *
     * @param userId   好友id
     * @param callback 删除结果回调
     */
    void deleteFromFriendList(String userId, CommonCallback callback);

    /**
     * 查验单向好友
     *
     * @param uerId    好友id
     * @param callback 查验结果回调
     */
    void checkFriend(String uerId, CommonCallback callback);

    /**
     * 获取头像上传cos信息
     *
     * @param roomCosType cos类型
     * @param callback    结果回调
     */
    void getRoomCosInfo(String roomCosType, final GetCosInfoCallback callback);

    /**
     * 上传房间头像
     *
     * @param url      上传的cos地址
     * @param fileName 文件名
     */
    void uploadRoomAvatar(Bitmap bitmap, String url, String fileName, Map<String, Object> map,
                          final ActionCallback callback);

    /**
     * 获取指定房间信息
     *
     * @param roomId   房间号
     * @param callback 获取结果回调
     */
    void getGroupInfo(String roomId, RoomInfoListCallback callback);
}
