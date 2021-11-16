//
//  ShowLiveListViewModel.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/9/27.
//

import UIKit
import TUICore

public protocol ShowLiveListViewNavigator: NSObject {
    func pushRoomView(viewController: UIViewController)
    func pushCreateRoom(viewController: UIViewController)
    func showFloatFloatingWindow()
}

public protocol ShowLiveListViewResponder: NSObject {
    func showToast(message: String)
    func refreshList()
    func stopListRefreshing()
    func showLoading(message: String)
    func hideLoading()
}

class ShowLiveListViewModel: NSObject {
    weak var viewNavigator: ShowLiveListViewNavigator?
    weak var viewResponder: ShowLiveListViewResponder?
    
    private let roomType: RoomType = .showLive
    
    // 视图相关属性
    private(set) var roomList: [ShowLiveRoomInfo] = []
    
    deinit {
        TRTCLog.out("deinit \(type(of: self))")
    }
}

extension ShowLiveListViewModel {
    
    func getRoomList() {
        guard HttpLogicRequest.sdkAppId != 0 else {
            viewResponder?.showToast(message: .appidErrorText)
            return
        }
        RoomService.shared.getRoomList(sdkAppID: HttpLogicRequest.sdkAppId, roomType: .showLive) { [weak self] (roomInfos) in
            guard let `self` = self else { return }
            DispatchQueue.main.async {
                self.roomList = roomInfos
                self.viewResponder?.refreshList()
            }
            self.viewResponder?.stopListRefreshing()
        } failed: { [weak self] (code, message) in
            guard let `self` = self else { return }
            TRTCLog.out("error: get room list fail. code: \(code), message:\(message)")
            self.viewResponder?.showToast(message: .listFailedText)
            self.viewResponder?.stopListRefreshing()
        }
        
    }
    
    func clickRoomItem(index: Int) {
        let roomInfo = self.roomList[index]
        if TUILogin.getUserID() == roomInfo.ownerId {
            // 开始进入已经存在的房间
            startEnterExistRoom(info: roomInfo)
        } else {
            // 正常进房逻辑
            enterRoom(info: roomInfo)
        }
    }
    
    func startEnterExistRoom(info: ShowLiveRoomInfo) {
        // 以主播方式进房
        if let viewNavigator = viewNavigator {
            let viewModel = ShowLiveAnchorViewModel.init(roomInfo: info)
            let showLiveAnchorViewController = ShowLiveAnchorViewController.init(viewModel: viewModel)
            viewNavigator.pushCreateRoom(viewController: showLiveAnchorViewController)
        }
    }
    
    func enterRoom(info: ShowLiveRoomInfo) {
        // 以观众方式进房
        if let viewNavigator = viewNavigator {
            let viewModel = ShowLiveAudienceViewModel.init(roomInfo: info)
            let showLiveAudienceViewController = ShowLiveAudienceViewController.init(viewModel: viewModel)
            viewNavigator.pushCreateRoom(viewController: showLiveAudienceViewController)
        }
    }
    
    func pushShowLiveAnchorRoom() {
        if let viewNavigator = viewNavigator {
            let viewModel = ShowLiveAnchorViewModel.init(roomInfo: ShowLiveRoomInfo.init(roomID: "", ownerId: "", memberCount: 0))
            let anchorViewController = ShowLiveAnchorViewController.init(viewModel: viewModel)
            viewNavigator.pushCreateRoom(viewController: anchorViewController)
        }
    }

}

private extension String {
    static let appidErrorText = ShowLiveLocalize("Scene.ShowLive.List.invalidappid")
    static let nocontentText = ShowLiveLocalize("Scene.ShowLive.List.nocontentnow~")
    static let listFailedText = ShowLiveLocalize("Scene.ShowLive.List.getlistfailed")
}
