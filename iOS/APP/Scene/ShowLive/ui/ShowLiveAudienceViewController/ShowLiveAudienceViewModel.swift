//
//  ShowLiveAudienceViewModel.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/9/29.
//

import UIKit

public protocol ShowLiveAudienceViewNavigator: NSObject {
    func pop()
    func present(viewController: UIViewController)
}

public protocol ShowLiveAudienceViewResponder: NSObject {
    
}

class ShowLiveAudienceViewModel: NSObject {
    weak var viewNavigator: ShowLiveAudienceViewNavigator?
    weak var viewResponder: ShowLiveAudienceViewResponder?
    var roomInfo: ShowLiveRoomInfo
    
    var groupId: String {
        return roomInfo.roomID
    }
    
    init(roomInfo: ShowLiveRoomInfo) {
        self.roomInfo = roomInfo
        super.init()
    }
    
    public func joinRoom(success: @escaping () -> Void,
                         failed: @escaping (_ code: Int32, _ error: String) -> Void) {
        let roomID = roomInfo.roomID
        RoomService.shared.enterRoom(roomId: roomID) {
            success()
        } failed: { code, msg in
            failed(code, msg)
        }
    }
    
    public func exitRoom(success: (() -> Void)?,
                         failed: ((_ code: Int32, _ error: String) -> Void)?) {
        let roomID = roomInfo.roomID
        RoomService.shared.exitRoom(roomId: roomID) {
            success?()
        } failed: { code, msg in
            failed?(code, msg)
        }
        if let viewNavigator = viewNavigator {
            viewNavigator.pop()
        }
    }
    
    public func showAlert(viewController: UIViewController) {
        if let viewNavigator = viewNavigator {
            viewNavigator.present(viewController: viewController)
        }
    }

}
