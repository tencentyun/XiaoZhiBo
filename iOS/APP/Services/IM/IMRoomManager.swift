//
//  IMRoomManager.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/9/29.
//

import UIKit
import ImSDK_Plus

typealias IMRoomInfoListCallback = (_ code: Int32, _ message: String?, _ roomInfos: [ShowLiveRoomInfo]) -> Void
typealias IMCommonCallback = (_ code: Int32, _ message: String?) -> Void

class IMRoomManager: NSObject {
    private static let staticInstance: IMRoomManager = IMRoomManager.init()
    public static func sharedManager() -> IMRoomManager { staticInstance }
    private let imManager = V2TIMManager.sharedInstance()
    private override init(){}
    
    public func createRoom(roomId: String, roomName: String, callback: IMCommonCallback?) {
        TRTCLog.out("createRoom roomId: \(roomId)")
        imManager?.createGroup("AVChatRoom", groupID: roomId, groupName: roomName, succ: { [weak self] (succ) in
            guard let `self` = self else { return }
            TRTCLog.out("createGroup onSuccess succ: \(succ ?? "")")
            self.imManager?.addSimpleMsgListener(listener: self)
            self.imManager?.setGroupListener(self)
            if let callBack = callback {
                callBack(0, "create room success.")
            }
        }, fail: { (code, msg) in
            var msg = msg
            if code == 10036 {
                msg = "您当前使用的云通讯账号未开通音视频聊天室功能，创建聊天室数量超过限额，请前往腾讯云官网开通【IM音视频聊天室】，地址：https://cloud.tencent.com/document/product/269/11673"
            }
            
            if code == 10037 {
                msg = "单个用户可创建和加入的群组数量超过了限制，请购买相关套餐,价格地址：https://cloud.tencent.com/document/product/269/11673"
            }
            
            if code == 10038 {
                msg = "群成员数量超过限制，请参考，请购买相关套餐，价格地址：https://cloud.tencent.com/document/product/269/11673"
            }
            
            if code == 10025 {
                if let callBack = callback {
                    callBack(0, "success.")
                }
            } else {
                if let callBack = callback {
                    callBack(code, msg)
                }
            }
        })
    }
    
    public func destroyRoom(roomId: String, callback: IMCommonCallback?) {
        imManager?.dismissGroup(roomId, succ: { [weak self] in
            TRTCLog.out("destroyRoom remove onSuccess roomId: \(roomId)")
            guard let `self` = self else { return }
            self.imManager?.removeSimpleMsgListener(listener: self)
            self.imManager?.setGroupListener(nil)
            TRTCLog.out("destroy room success.")
            if let callBack = callback {
                callBack(0, "destroy room success.")
            }
        }, fail: { code, msg in
            TRTCLog.out("destroy room fail, code: \(code), msg: \(msg ?? "")")
            if let callBack = callback {
                callBack(code, msg ?? "")
            }
        })
    }
    
    public func joinGroup(roomId: String, callback: IMCommonCallback?) {
        imManager?.joinGroup(roomId, msg: "", succ: { [weak self] in
            guard let `self` = self else { return }
            TRTCLog.out("enter room success. roomId:  \(roomId)")
            self.imManager?.addSimpleMsgListener(listener: self)
            self.imManager?.setGroupListener(self)
            if let callBack = callback {
                callBack(0, "success")
            }
        }, fail: { code, msg in
            // 已经是群成员了，可以继续操作
            if (code == 10013) {
                if let callBack = callback {
                    callBack(0, "success")
                }
            } else {
                TRTCLog.out("enter room fail, code: \(code) msg: \(msg ?? "")");
                if let callBack = callback {
                    callBack(code, msg ?? "")
                }
            }
        })
    }
    
    public func quitGroup(roomId: String, callback: IMCommonCallback?) {
        imManager?.quitGroup(roomId, succ: { [weak self] in
            guard let `self` = self else { return }
            TRTCLog.out("exit room success.")
            self.imManager?.removeSimpleMsgListener(listener: self)
            self.imManager?.setGroupListener(nil)
            if let callBack = callback {
                callBack(0, "exit room success.")
            }
        }, fail: { code, msg in
            TRTCLog.out("exit room fail, code: \(code) msg: \(msg ?? "")")
            if let callBack = callback {
                callBack(code, msg ?? "")
            }
        })
    }
    
    public func getIMRoomInfoList(roomIds:[String], callback: IMRoomInfoListCallback?) {
        imManager?.getGroupsInfo(roomIds, succ: { [weak self] groupResultList in
            guard let `self` = self else { return }
            if let groupResultList = groupResultList {
                var groupResults: [ShowLiveRoomInfo] = []
                var tempDic: [String : V2TIMGroupInfoResult] = [:]
                groupResultList.forEach { result in
                    if result.info.groupID != nil {
                        tempDic[result.info.groupID] = result
                    }
                }
                var userList: [String] = []
                var roomDic: [String : ShowLiveRoomInfo] = [:]
                roomIds.forEach { roomId in
                    if let groupInfo = tempDic[roomId] {
                        if groupInfo.resultCode == 0 {
                            let roomInfo = ShowLiveRoomInfo.init(roomID: groupInfo.info.groupID, ownerId: groupInfo.info.owner, memberCount: groupInfo.info.memberCount)
                            roomInfo.roomName = groupInfo.info.groupName ?? ""
                            roomInfo.coverUrl = groupInfo.info.faceURL ?? self.defaultAvatar()
                            roomInfo.ownerName = groupInfo.info.introduction ?? ""
                            if groupInfo.info.owner != nil {
                                userList.append(groupInfo.info.owner)
                                roomDic.updateValue(roomInfo, forKey: groupInfo.info.owner)
                            }
                        }
                    }
                }
                if userList.count > 0 {
                    self.imManager?.getUsersInfo(userList, succ: { infos in
                        if let infos = infos {
                            for info in infos {
                                if let roomInfo = roomDic[info.userID] {
                                    roomInfo.ownerId = info.userID
                                    roomInfo.ownerName = info.nickName ?? ""
                                    roomInfo.coverUrl = info.faceURL ?? self.defaultAvatar()
                                    groupResults.append(roomInfo)
                                }
                            }
                            if let callBack = callback {
                                callBack(0,"success",groupResults)
                            }
                        } else {
                            if let callBack = callback {
                                callBack(-1,"get group info failed.reslut is nil.",[])
                            }
                        }
                    }, fail: { code, msg in
                        if let callBack = callback {
                            callBack(-1,"get group info failed.reslut is nil.",[])
                        }
                    })
                } else {
                    if let callBack = callback {
                        callBack(-1,"get group info failed.reslut is nil.",[])
                    }
                }
            } else {
                if let callBack = callback {
                    callBack(-1,"get group info failed.reslut is nil.",[])
                }
            }
        }, fail: { code, message in
            if let callBack = callback {
                callBack(code,message,[])
            }
        })
    }
    
    private func defaultAvatar() -> String {
        return "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover1.png"
    }
}

extension IMRoomManager: V2TIMSimpleMsgListener {
    
}

extension IMRoomManager: V2TIMGroupListener {
    
}
