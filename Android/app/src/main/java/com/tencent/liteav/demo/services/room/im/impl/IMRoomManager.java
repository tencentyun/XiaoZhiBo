package com.tencent.liteav.demo.services.room.im.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMFriendAddApplication;
import com.tencent.imsdk.v2.V2TIMFriendCheckResult;
import com.tencent.imsdk.v2.V2TIMFriendOperationResult;
import com.tencent.imsdk.v2.V2TIMGroupChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfoResult;
import com.tencent.imsdk.v2.V2TIMGroupListener;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMUserInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.services.room.bean.AudienceInfo;
import com.tencent.liteav.demo.services.room.bean.RoomInfo;
import com.tencent.liteav.demo.services.room.callback.RoomMemberInfoCallback;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.demo.services.room.im.IIMRoomManager;
import com.tencent.liteav.demo.services.room.im.listener.RoomInfoListCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tencent.imsdk.v2.V2TIMFriendInfo.V2TIM_FRIEND_TYPE_SINGLE;

public class IMRoomManager implements IIMRoomManager {
    private static final String        TAG = "IMRoomManager";
    private static       IMRoomManager sInstance;

    private Context                   mContext;
    private LiveRoomSimpleMsgListener mSimpleListener;
    private LiveRoomGroupListener     mGroupListener;
    private OnMemberChangeListener    mMemberChangeListener;

    public static synchronized IMRoomManager getInstance() {
        if (sInstance == null) {
            sInstance = new IMRoomManager();
        }
        return sInstance;
    }

    private IMRoomManager() {

        mSimpleListener = new LiveRoomSimpleMsgListener();
        mGroupListener = new LiveRoomGroupListener();
    }

    @Override
    public void createRoom(final String roomId, final String roomName, final String coverUrl,
                           final CommonCallback callback) {
        Log.e(TAG, "createRoom roomId:" + roomId);
        V2TIMManager.getInstance().createGroup(V2TIMManager.GROUP_TYPE_AVCHATROOM, roomId,
                roomName, new V2TIMValueCallback<String>() {
                    @Override
                    public void onError(int code, String s) {
                        String msg = s;
                        if (code == 10036) {
                            msg = mContext.getString(R.string.app_create_room_limit);
                        }
                        if (code == 10037) {
                            msg = mContext.getString(R.string.app_create_or_join_group_limit);
                        }
                        if (code == 10038) {
                            msg = mContext.getString(R.string.app_group_member_limit);
                        }
                        if (code == 10025) {
                            // 10025 表明群主是自己，那么认为创建房间成功
                            onSuccess("success");
                        } else {
                            Log.e(TAG, "create room fail, code:" + code + " msg:" + msg);
                            if (callback != null) {
                                callback.onCallback(code, msg);
                            }
                        }
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "createGroup onSuccess s: " + s);
                        V2TIMManager.getInstance().addSimpleMsgListener(mSimpleListener);
                        V2TIMManager.getInstance().setGroupListener(mGroupListener);
                        if (callback != null) {
                            callback.onCallback(0, "create room success.");
                        }
                    }
                });
    }

    private void updateGroupInfo(String roomId, String roomName, String coverUrl) {
        V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
        v2TIMGroupInfo.setGroupID(roomId);
        v2TIMGroupInfo.setGroupName(roomName);
        v2TIMGroupInfo.setFaceUrl(coverUrl);
        v2TIMGroupInfo.setGroupType(V2TIMManager.GROUP_TYPE_AVCHATROOM);
        V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                Log.e(TAG, "updateGroupInfo room owner update anchor list into group introduction fail, "
                        + "code: " + code + " msg:" + desc);
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "updateGroupInfo room owner update anchor list into group introduction success");
            }
        });
    }

    @Override
    public void destroyRoom(final String roomId, final CommonCallback callback) {
        V2TIMManager.getInstance().dismissGroup(roomId, new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "destroy room fail, code:" + i + " msg:" + s);
                if (callback != null) {
                    callback.onCallback(i, s);
                }
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "destroyRoom remove onSuccess roomId: " + roomId);
                V2TIMManager.getInstance().removeSimpleMsgListener(mSimpleListener);
                V2TIMManager.getInstance().setGroupListener(null);
                Log.i(TAG, "destroy room success.");
                if (callback != null) {
                    callback.onCallback(0, "destroy room success.");
                }
            }
        });
    }

    @Override
    public void joinGroup(final String roomId, final CommonCallback callback) {
        V2TIMManager.getInstance().joinGroup(roomId, "", new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                // 已经是群成员了，可以继续操作
                if (i == 10013) {
                    onSuccess();
                } else {
                    Log.e(TAG, "enter room fail, code:" + i + " msg:" + s);
                    if (callback != null) {
                        callback.onCallback(i, s);
                    }
                }
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "enter room success. roomId: " + roomId);
                V2TIMManager.getInstance().addSimpleMsgListener(mSimpleListener);
                V2TIMManager.getInstance().setGroupListener(mGroupListener);

                if (callback != null) {
                    callback.onCallback(0, "success");
                }

            }
        });
    }

    @Override
    public void quitGroup(String roomId, final CommonCallback callback) {
        V2TIMManager.getInstance().quitGroup(roomId, new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "exit room fail, code:" + i + " msg:" + s);
                if (callback != null) {
                    callback.onCallback(i, s);
                }
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "exit room success.");
                V2TIMManager.getInstance().removeSimpleMsgListener(mSimpleListener);
                V2TIMManager.getInstance().setGroupListener(null);
                if (callback != null) {
                    callback.onCallback(0, "exit room success.");
                }
            }
        });
    }

    @Override
    public void getRoomInfos(final List<String> roomList, final RoomInfoListCallback callback) {
        V2TIMManager.getGroupManager().getGroupsInfo(roomList, new V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
            @Override
            public void onError(int i, String s) {
                if (callback != null) {
                    callback.onCallback(i, s, null);
                }
            }

            @Override
            public void onSuccess(List<V2TIMGroupInfoResult> v2TIMGroupInfoResults) {
                final List<RoomInfo> txRoomInfos = new ArrayList<>();
                Map<String, V2TIMGroupInfo> groupInfoResultMap = new HashMap<>();

                for (V2TIMGroupInfoResult result : v2TIMGroupInfoResults) {
                    V2TIMGroupInfo groupInfo = result.getGroupInfo();
                    // 防止为空
                    if (groupInfo == null) {
                        continue;
                    }
                    groupInfoResultMap.put(groupInfo.getGroupID(), groupInfo);
                }
                //获取主播信息
                List<String> userList = new ArrayList<>();
                final Map<String, RoomInfo> map = new HashMap<>();
                for (String roomId : roomList) {
                    V2TIMGroupInfo timGroupDetailInfo = groupInfoResultMap.get(roomId);
                    if (timGroupDetailInfo == null) {
                        continue;
                    }
                    final RoomInfo txRoomInfo = new RoomInfo();
                    txRoomInfo.roomId = timGroupDetailInfo.getGroupID();
                    txRoomInfo.ownerId = timGroupDetailInfo.getOwner();
                    txRoomInfo.memberCount = timGroupDetailInfo.getMemberCount();
                    txRoomInfo.coverUrl = timGroupDetailInfo.getFaceUrl();
                    txRoomInfo.roomName = timGroupDetailInfo.getGroupName();

                    if (!TextUtils.isEmpty(timGroupDetailInfo.getOwner())) {
                        userList.add(timGroupDetailInfo.getOwner());
                    }
                    txRoomInfos.add(txRoomInfo);
                    map.put(timGroupDetailInfo.getOwner(), txRoomInfo);
                }
                if (userList.size() <= 0) {
                    callback.onCallback(0, "get getUsersInfo success", new ArrayList<RoomInfo>());
                    return;
                }
                V2TIMManager.getInstance().getUsersInfo(userList, new V2TIMValueCallback<List<V2TIMUserFullInfo>>() {
                    @Override
                    public void onSuccess(List<V2TIMUserFullInfo> v2TIMUserFullInfos) {
                        for (V2TIMUserFullInfo info : v2TIMUserFullInfos) {
                            if (info != null && map.containsKey(info.getUserID())) {
                                RoomInfo roomInfo = map.get(info.getUserID());
                                roomInfo.streamUrl = info.getUserID();
                                roomInfo.ownerName = info.getNickName();
                                roomInfo.coverUrl = info.getFaceUrl();
                                roomInfo.ownerAvatar = info.getFaceUrl();
                            }
                        }
                        if (callback != null) {
                            callback.onCallback(0, "get room info success", txRoomInfos);
                        }
                    }

                    @Override
                    public void onError(int code, String desc) {

                    }
                });
            }
        });
    }

    @Override
    public void getGroupMemberList(final String roomId, final RoomMemberInfoCallback callback) {
        V2TIMManager.getGroupManager().getGroupMemberList(roomId,
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_COMMON,
                0,
                new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
                    @Override
                    public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                        List<V2TIMGroupMemberFullInfo> fullInfoList = v2TIMGroupMemberInfoResult.getMemberInfoList();
                        List<AudienceInfo> list = new ArrayList<>();
                        for (V2TIMGroupMemberFullInfo info : fullInfoList) {
                            AudienceInfo audienceInfo = new AudienceInfo();
                            audienceInfo.setMemberId(info.getUserID());
                            audienceInfo.setAvatar(info.getFaceUrl());
                            audienceInfo.setName(info.getNickName());
                            list.add(audienceInfo);
                        }
                        if (callback != null) {
                            callback.onCallback(0, "get room info success", list);
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        if (callback != null) {
                            callback.onCallback(i, "get group member list failed!" + s + "  " + roomId,
                                    new ArrayList<AudienceInfo>());
                        }
                    }
                });
    }

    @Override
    public void addFriend(String userId, String friendId, final CommonCallback callback) {
        V2TIMFriendAddApplication v2TIMFriendAddApplication = new V2TIMFriendAddApplication(userId);
        v2TIMFriendAddApplication.setAddType(V2TIM_FRIEND_TYPE_SINGLE);
        v2TIMFriendAddApplication.setUserID(friendId);
        V2TIMManager.getFriendshipManager().addFriend(v2TIMFriendAddApplication,
                new V2TIMValueCallback<V2TIMFriendOperationResult>() {
                    @Override
                    public void onSuccess(V2TIMFriendOperationResult v2TIMFriendOperationResult) {
                        if (callback != null) {
                            callback.onCallback(0, v2TIMFriendOperationResult.getResultInfo());
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        if (callback != null) {
                            callback.onCallback(i, s);
                        }
                    }
                });
    }

    @Override
    public void deleteFromFriendList(String userId, final CommonCallback callback) {
        List<String> list = new ArrayList<>();
        list.add(userId);
        V2TIMManager.getFriendshipManager().deleteFromFriendList(list,
                V2TIM_FRIEND_TYPE_SINGLE,
                new V2TIMValueCallback<List<V2TIMFriendOperationResult>>() {
                    @Override
                    public void onSuccess(List<V2TIMFriendOperationResult> v2TIMFriendOperationResults) {
                        if (callback != null) {
                            callback.onCallback(0, v2TIMFriendOperationResults.get(0).getResultInfo());
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        if (callback != null) {
                            callback.onCallback(i, s);
                        }
                    }
                });
    }

    @Override
    public void checkFriend(String uerId, final CommonCallback callback) {
        List<String> list = new ArrayList<>();
        list.add(uerId);
        V2TIMManager.getFriendshipManager().checkFriend(list,
                V2TIM_FRIEND_TYPE_SINGLE,
                new V2TIMValueCallback<List<V2TIMFriendCheckResult>>() {
                    @Override
                    public void onSuccess(List<V2TIMFriendCheckResult> v2TIMFriendCheckResults) {
                        if (callback != null) {
                            callback.onCallback(0, v2TIMFriendCheckResults.get(0).getResultType() + "");
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        if (callback != null) {
                            callback.onCallback(i, s);
                        }
                    }
                });
    }

    public interface OnMemberChangeListener {
        void onMemberEnter(String groupId, List<AudienceInfo> memberList);

        void onMemberLeave(String groupId, AudienceInfo info);
    }

    public void setMemberChangeListener(OnMemberChangeListener mMemberChangeListener) {
        this.mMemberChangeListener = mMemberChangeListener;
    }

    private class LiveRoomSimpleMsgListener extends V2TIMSimpleMsgListener {

        public void onRecvC2CTextMessage(String msgID, V2TIMUserInfo sender, String text) {
        }

        public void onRecvC2CCustomMessage(String msgID, V2TIMUserInfo sender, byte[] customData) {

        }

        @Override
        public void onRecvGroupTextMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender, String text) {

        }

        @Override
        public void onRecvGroupCustomMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender,
                                             byte[] customData) {

        }
    }


    private class LiveRoomGroupListener extends V2TIMGroupListener {
        @Override
        public void onMemberEnter(String groupID, List<V2TIMGroupMemberInfo> memberList) {
            List<AudienceInfo> infoList = new ArrayList<>();
            for (V2TIMGroupMemberInfo info :
                    memberList) {
                AudienceInfo audienceInfo = new AudienceInfo();
                audienceInfo.setMemberId(info.getUserID());
                audienceInfo.setName(info.getNickName());
                audienceInfo.setAvatar(info.getFaceUrl());
                infoList.add(audienceInfo);
            }
            mMemberChangeListener.onMemberEnter(groupID, infoList);
        }

        @Override
        public void onMemberLeave(String groupID, V2TIMGroupMemberInfo member) {
            AudienceInfo audienceInfo = new AudienceInfo();
            audienceInfo.setMemberId(member.getUserID());
            audienceInfo.setAvatar(member.getFaceUrl());
            audienceInfo.setName(member.getNickName());
            mMemberChangeListener.onMemberLeave(groupID, audienceInfo);
        }

        @Override
        public void onGroupDismissed(String groupID, V2TIMGroupMemberInfo opUser) {
            Log.i(TAG, "recv room destroy msg");
            // 如果发现房间已经解散，那么内部退一次房间
            quitGroup(groupID, new CommonCallback() {
                @Override
                public void onCallback(int code, String msg) {
                    Log.i(TAG, "recv room destroy msg, exit room inner, code:" + code + " msg:" + msg);
                }
            });
        }

        @Override
        public void onGroupInfoChanged(String groupID, List<V2TIMGroupChangeInfo> changeInfos) {
            super.onGroupInfoChanged(groupID, changeInfos);
        }
    }

    @Override
    public void getGroupInfo(final String roomId, final CommonCallback callback) {
        List<String> roomIdList = new ArrayList<>();
        roomIdList.add(roomId);
        V2TIMManager.getGroupManager().getGroupsInfo(roomIdList, new V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
            @Override
            public void onSuccess(List<V2TIMGroupInfoResult> resultList) {
                if (resultList == null || resultList.isEmpty()) {
                    Log.e(TAG, "room not exist, result is null");
                    return;
                }
                V2TIMGroupInfoResult groupInfoResult = resultList.get(0);
                if (callback != null) {
                    callback.onCallback(groupInfoResult.getResultCode(), groupInfoResult.getResultMessage());
                }
            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "get group info failed : " + msg);
                if (callback != null) {
                    callback.onCallback(code, msg);
                }
            }
        });
    }
}
