//
//  ShowLiveAnchorViewModel.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/9/28.
//

import UIKit
import TUICore

public protocol ShowLiveAnchorViewNavigator: NSObject {
    func pop()
    func present(viewController: UIViewController)
}

public protocol ShowLiveAnchorViewResponder: NSObject {
    func switchCamera(isFront: Bool)
    func showToast(message: String)
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
        RoomService.shared.createRoom(sdkAppID: HttpLogicRequest.sdkAppId, roomID: roomInfo.roomID, roomName: roomInfo.roomName, coverUrl: roomInfo.coverUrl, roomType: .showLive) {
            if let callBack = callback {
                callBack(0, "create Room Success.")
            }
        } failed: { code, msg in
            if let callBack = callback {
                callBack(code, msg)
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
    
}
