package com.tencent.liteav.demo.services.room.im;

import com.tencent.liteav.demo.services.room.callback.RoomMemberInfoCallback;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.demo.services.room.im.listener.RoomInfoListCallback;

import java.util.List;

/**
 * IM相关的接口封装
 */
public interface IIMRoomManager {

    /**
     * 主播创建房间
     *
     * @param roomId
     * @param roomName
     * @param coverUrl
     * @param callback
     */
    void createRoom(String roomId, String roomName, String coverUrl, CommonCallback callback);

    /**
     * 主播销毁房间
     *
     * @param roomId
     * @param callback
     */
    void destroyRoom(String roomId, CommonCallback callback);

    /**
     * 观众加入房间
     *
     * @param roomId
     * @param callback
     */
    void joinGroup(String roomId, CommonCallback callback);

    /**
     * 观众退出房间
     *
     * @param roomId
     * @param callback
     */
    void quitGroup(String roomId, CommonCallback callback);

    /**
     * 获取房间列表
     *
     * @param roomId
     * @param callback
     */
    void getRoomInfos(List<String> roomId, RoomInfoListCallback callback);

    /**
     * 获取房间观众列表
     *
     * @param roomId   房间号
     * @param callback 获取结果回调
     */
    void getGroupMemberList(String roomId, RoomMemberInfoCallback callback);

    /**
     * 添加单向好友
     *
     * @param userId   添加者id
     * @param friendId 好友id
     * @param callback 添加结果回调
     */
    void addFriend(String userId, String friendId, CommonCallback callback);

    /**
     * 删除单向好友
     *
     * @param userId   待删除好友id
     * @param callback 删除结果回调
     */
    void deleteFromFriendList(String userId, CommonCallback callback);

    /**
     * 查验单向好友
     *
     * @param uerId    待查验好友id
     * @param callback 查验结果回调
     */
    void checkFriend(String uerId, CommonCallback callback);

    /**
     * 获取指定房间信息
     *
     * @param roomId   房间号
     * @param callback 结果回调
     */
    void getGroupInfo(String roomId, CommonCallback callback);

}
