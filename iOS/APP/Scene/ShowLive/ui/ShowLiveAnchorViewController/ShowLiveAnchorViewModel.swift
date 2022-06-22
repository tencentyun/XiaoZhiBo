//
//  ShowLiveAnchorViewModel.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/9/28.
//  Copyright © 2022 Tencent. All rights reserved.

import UIKit
import TUICore
import SwiftUI
import CommonCrypto

public protocol ShowLiveAnchorViewNavigator: NSObject {
    func pop()
    func present(viewController: UIViewController)
}

public protocol ShowLiveAnchorViewResponder: NSObject {
    func switchCamera(isFront: Bool)
    func showToast(message: String)
    // 刷新在线用户视图
    func refreshOnlineUsersView()
    func exitRoom()
}

enum ShowLiveAudioQuality {
    case normal
    case music
}

class ShowLiveAnchorViewModel: NSObject {
    weak var viewNavigator: ShowLiveAnchorViewNavigator?
    weak var viewResponder: ShowLiveAnchorViewResponder?
    var roomInfo: ShowLiveRoomInfo
    var audioQuality: ShowLiveAudioQuality = .music
    // 在线用户
    var onlineUsers: [ShowLiveUserInfo] = [ShowLiveUserInfo]()
    // 在线用户数量
    var onlineUsersCount: Int {
        return onlineUsers.count
    }
    
    var groupId: String {
        return roomInfo.roomID
    }
    
    public var cameraIsFrontMonitor: Bool = false {
        willSet {
            UserDefaults.standard.set(cameraIsFrontMonitor, forKey: "kShowLiveSwitchCamera")
            if let viewResponder = viewResponder {
                viewResponder.switchCamera(isFront: cameraIsFrontMonitor)
            }
        }
    }
    
    init(roomInfo: ShowLiveRoomInfo) {
        self.roomInfo = roomInfo
        super.init()
        self.roomInfo.roomID = getRoomId()
        self.roomInfo.ownerId = TUILogin.getUserID() ?? ""
        self.roomInfo.coverUrl = ProfileManager.sharedManager().currentUserModel?.avatar ?? randomBgImageLink()
        self.roomInfo.ownerName = TUILogin.getNickName() ?? ""
        //
        registerNotification()
    }
    
    func stopPush() {
        RoomService.shared.destroyRoom(sdkAppID: HttpLogicRequest.sdkAppId, roomID: roomInfo.roomID, roomType: .showLive) {
            
        } failed: { code, msg in
            
        }
        if let viewNavigator = viewNavigator {
            viewNavigator.pop()
        }
    }
    
    func showAlert(viewController: UIViewController) {
        if let viewNavigator = viewNavigator {
            viewNavigator.present(viewController: viewController)
        }
    }

    private func randomBgImageLink() -> String {
        let random = arc4random() % 12 + 1
        return "https://liteav-test-1252463788.cos.ap-guangzhou.myqcloud.com/voice_room/voice_room_cover\(random).png"
    }
    
    private func getRoomId() -> String {
        if let userId = TUILogin.getUserID() {
            TRTCLog.out("room id:\(userId), userId: \(userId)")
            return userId
        }
        return ""
    }
    
    public func createRoom(callback: ((_ code: Int32, _ msg: String) -> Void)?) {
        
        if roomInfo.roomName == "" {
            roomInfo.roomName = "\(roomInfo.ownerName)的直播间"
        }
        IMRoomManager.sharedManager().createRoom(roomId: roomInfo.roomID, roomName: roomInfo.roomName) { [weak self] (code, message) in
            guard let self = self else { return }
            if code == 0 {
                RoomService.shared.createRoom(sdkAppID: HttpLogicRequest.sdkAppId,
                                              roomID: self.roomInfo.roomID,
                                              roomName: self.roomInfo.roomName,
                                              coverUrl: self.roomInfo.coverUrl,
                                              roomType: .showLive) { [weak self] in
                    guard let self = self else { return }
                    callback?(0, "create Room Success.")
                    // 获取群成员列表
                    IMRoomManager.sharedManager().getGroupMemberList(roomID: self.roomInfo.roomID) { [weak self] (code, message, memberList) in
                        if code == 0, let userList = memberList {
                            self?.onlineUsers = userList
                            self?.viewResponder?.refreshOnlineUsersView()
                        }
                    }
                } failed: { code, msg in
                    callback?(code, msg)
                }
            } else {
                callback?(code, message ?? "")
            }
        }
    }
    
    public func getRoomList(callBack: @escaping ([ShowLiveRoomInfo]) -> Void) {
        RoomService.shared.getRoomList(sdkAppID: HttpLogicRequest.sdkAppId, roomType: .showLive) { showLiveRoomInfos in
            callBack(showLiveRoomInfos)
        } failed: { code, msg in
            callBack([])
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}

// MARK: - IMListenerNotification 用户进入、离开直播间监听
extension ShowLiveAnchorViewModel {
    
    private func registerNotification() {
        NotificationCenter.default.addObserver(self, selector: #selector(onUserEnterLiveRoom(_:)), name: .IMGroupUserEnterLiveRoom, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(onUserLeaveLiveRoom(_:)), name: .IMGroupUserLeaveLiveRoom, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(onGroupDestroy(_:)), name: .IMGroupDismissed, object: nil)
    }
    
    @objc
    private func onUserEnterLiveRoom(_ notification: Notification) {
        guard let enterInfo = notification.userInfo as? [String: Any], let groupID = enterInfo["groupID"] as? String, groupID == self.groupId else {
            return
        }
        guard let enterUsers = enterInfo["memberList"] as? [ShowLiveUserInfo] else {
            return
        }
        for user in enterUsers {
            if onlineUsers.contains(where: {$0.userId == user.userId}) == false {
                onlineUsers.append(user)
            }
        }
        viewResponder?.refreshOnlineUsersView()
    }
    
    @objc
    private func onUserLeaveLiveRoom(_ notification: Notification) {
        guard let leaveInfo = notification.userInfo as? [String: Any], let groupID = leaveInfo["groupID"] as? String, groupID == self.groupId else {
            return
        }
        guard let leaveUser = leaveInfo["member"] as? ShowLiveUserInfo else {
            return
        }
        onlineUsers.removeAll(where: {$0.userId == leaveUser.userId})
        viewResponder?.refreshOnlineUsersView()
    }
    
    @objc
    private func onGroupDestroy(_ notification: Notification) {
        guard let leaveInfo = notification.userInfo as? [String: Any], let groupID = leaveInfo["groupID"] as? String, groupID == self.groupId else {
            return
        }
        viewResponder?.exitRoom()
#if RTCube_APPSTORE
        let selector = NSSelectorFromString("showAlertUserLiveTimeOut")
        if UIViewController.responds(to: selector) {
            UIViewController.perform(selector)
        }
#endif
    }
}
