package com.tencent.liteav.demo.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.services.room.bean.AudienceInfo;
import com.tencent.liteav.demo.services.room.bean.RoomInfo;
import com.tencent.liteav.demo.services.room.bean.http.ShowLiveCosInfo;
import com.tencent.liteav.demo.services.room.callback.ActionCallback;
import com.tencent.liteav.demo.services.room.callback.GetCosInfoCallback;
import com.tencent.liteav.demo.services.room.callback.RoomMemberInfoCallback;
import com.tencent.liteav.demo.services.room.callback.CommonCallback;
import com.tencent.liteav.demo.services.room.callback.RoomInfoCallback;
import com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager;
import com.tencent.liteav.demo.services.room.im.impl.IMRoomManager;
import com.tencent.liteav.demo.services.room.im.listener.RoomInfoListCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tencent.liteav.demo.services.room.http.impl.HttpRoomManager.TYPE_MLVB_SHOW_LIVE;

public class RoomService implements IRoomService {
    private static final String      TAG = RoomService.class.getSimpleName();
    private static       RoomService mInstance;
    private              Context     mContext;

    private RoomService(Context context) {
        mContext = context;
    }

    public static RoomService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RoomService(context);
        }
        return mInstance;
    }

    @Override
    public void createRoom(final String roomId, final String type,
                           final String roomName, final String coverUrl, final CommonCallback callback) {
        Log.d(TAG, "createRoom roomId:" + roomId);
        //TODO 1、 IM创建群组
        IMRoomManager.getInstance().createRoom(roomId, roomName, coverUrl, new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                Log.d(TAG, " TXRoomService.getInstance().createRoom() code:" + code + ", message:" + msg);
                if (!UserModelManager.getInstance().haveBackstage()) {
                    callback.onCallback(code, msg);
                    return;
                }
                //TODO 2、服务器创建房间
                HttpRoomManager.getInstance().enterRoom(roomId, type,
                        HttpRoomManager.RoomRole.ANCHOR.getName(), new ActionCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, " HttpRoom2Manager.createRoom() success");
                                HttpRoomManager.getInstance().updateRoom(roomId, roomName, coverUrl,
                                        new ActionCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d(TAG, " HttpRoom2Manager.updateRoom() success");
                                                callback.onCallback(0, "createRoom success");
                                            }

                                            @Override
                                            public void onFailed(int code, String msg) {
                                                callback.onCallback(code, msg);
                                            }
                                        });

                            }

                            @Override
                            public void onFailed(int code, String msg) {
                                callback.onCallback(code, msg);
                            }
                        });
            }
        });
    }

    @Override
    public void destroyRoom(final String roomId, final String type, final CommonCallback callback) {
        //TIM退出群组
        IMRoomManager.getInstance().destroyRoom(roomId, new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (!UserModelManager.getInstance().haveBackstage()) {
                    callback.onCallback(code, msg);
                    return;
                }
                //服务器删除房间
                HttpRoomManager.getInstance().destroyRoom(roomId, type, new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onCallback(0, "");
                    }

                    @Override
                    public void onFailed(int code, String msg) {
                        callback.onCallback(code, msg);
                    }
                });
            }
        });
    }

    @Override
    public void enterRoom(final int roomId, final String type, final CommonCallback callback) {
        //TODO 加入群组
        IMRoomManager.getInstance().joinGroup(roomId + "", new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                Log.d(TAG, "TXRoomService.getInstance().enterRoom code:" + code + ", msg:" + msg);
                if (!UserModelManager.getInstance().haveBackstage()) {
                    callback.onCallback(code, msg);
                    return;
                }
                HttpRoomManager.getInstance().enterRoom(roomId + "",
                        type, HttpRoomManager.RoomRole.GUEST.getName(), new ActionCallback() {
                            @Override
                            public void onSuccess() {
                                if (callback != null) {
                                    callback.onCallback(0, "enterRoom success");
                                }
                            }

                            @Override
                            public void onFailed(int code, String msg) {
                                if (callback != null) {
                                    callback.onCallback(code, msg);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void exitRoom(final int roomId, final CommonCallback callback) {
        //TODO 退出群组
        IMRoomManager.getInstance().quitGroup(roomId + "", new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                Log.d(TAG, "TXRoomService.getInstance().exitRoom() code:" + code + ", message:" + msg);
                if (!UserModelManager.getInstance().haveBackstage()) {
                    if (callback != null) {
                        callback.onCallback(code, msg);
                    }
                    return;
                }
                HttpRoomManager.getInstance().leaveRoom(roomId + "", new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, " HttpRoom2Manager.exitRoom() success");
                        if (callback != null) {
                            callback.onCallback(0, "exitRoom success");
                        }
                    }

                    @Override
                    public void onFailed(int code, String msg) {
                        if (callback != null) {
                            callback.onCallback(code, msg);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void getRoomList(String type, HttpRoomManager.RoomOrderType orderType, final RoomInfoCallback callback) {
        if (!UserModelManager.getInstance().haveBackstage()) {
            return;
        }
        HttpRoomManager.getInstance().getRoomList(TYPE_MLVB_SHOW_LIVE, orderType, new RoomInfoCallback() {
            @Override
            public void onCallback(int code, String msg, List<RoomInfo> list) {
                if (code == 0) {
                    if (list == null || list.size() <= 0) {
                        callback.onCallback(0, "success", new ArrayList<RoomInfo>());
                        return;
                    }
                    callback.onCallback(0, "success", list);
                } else {
                    callback.onCallback(code, msg, list);
                }
            }
        });
    }


    @Override
    public void addFriend(String userId, String friendId, final CommonCallback callback) {
        IMRoomManager.getInstance().addFriend(userId, friendId, new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (callback != null) {
                    callback.onCallback(code, msg);
                }
            }
        });
    }

    @Override
    public void deleteFromFriendList(String userId, final CommonCallback callback) {
        IMRoomManager.getInstance().deleteFromFriendList(userId, new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (callback != null) {
                    callback.onCallback(code, msg);
                }
            }
        });
    }

    @Override
    public void checkFriend(String uerId, final CommonCallback callback) {
        IMRoomManager.getInstance().checkFriend(uerId, new CommonCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (callback != null) {
                    callback.onCallback(code, msg);
                }
            }
        });
    }

    @Override
    public void getRoomAudienceList(String roomId, final RoomMemberInfoCallback callback) {
        IMRoomManager.getInstance().getGroupMemberList(roomId, new RoomMemberInfoCallback() {
            @Override
            public void onCallback(int code, String msg, List<AudienceInfo> list) {
                if (code == 0) {
                    if (list == null || list.size() <= 0) {
                        callback.onCallback(0, "success", new ArrayList<AudienceInfo>());
                        return;
                    }
                    callback.onCallback(0, "success", list);
                } else {
                    callback.onCallback(code, msg, list);
                }
            }
        });
    }

    @Override
    public void getRoomCosInfo(String roomCosType, final GetCosInfoCallback callback) {
        if (!UserModelManager.getInstance().haveBackstage()) {
            return;
        }
        HttpRoomManager.getInstance().getRoomCosInfo(roomCosType, new GetCosInfoCallback() {
            @Override
            public void onSuccess(ShowLiveCosInfo cosInfo) {
                if (callback != null) {
                    callback.onSuccess(cosInfo);
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null) {
                    callback.onFailed(code, msg);
                }
            }
        });
    }

    @Override
    public void uploadRoomAvatar(Bitmap bitmap, String url, String fileName, Map<String, Object> map,
                                 final ActionCallback callback) {
        if (!UserModelManager.getInstance().haveBackstage()) {
            return;
        }
        HttpRoomManager.getInstance().uploadRoomAvatar(bitmap, url, fileName, map, new ActionCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                if (callback != null) {
                    callback.onFailed(code, msg);
                }
            }
        });
    }

    @Override
    public void getGroupInfo(String roomId, final RoomInfoListCallback callback) {
        List<String> roomIdList = new ArrayList<>();
        roomIdList.add(roomId);
        IMRoomManager.getInstance().getRoomInfos(roomIdList, new RoomInfoListCallback() {
            @Override
            public void onCallback(int code, String msg, List<RoomInfo> list) {
                callback.onCallback(code, msg, list);
            }
        });
    }
}
