//
//  ShowLiveAudienceRootView.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/9/29.
//

import UIKit
import TUIPlayer

class ShowLiveAudienceRootView: UIView {

    private let viewModel: ShowLiveAudienceViewModel
    
    lazy var playerView: TUIPlayerView = {
        let view = TUIPlayerView.init(frame: .zero)
        view.setDelegate(self)
        return view
    }()
    
    lazy var exitButton: UIButton = {
        let button = UIButton.init(frame: .zero)
        button.setBackgroundImage(UIImage.init(named: "exit_room"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFill
        return button
    }()
    
    lazy var roomInfoView: ShowLiveRoomInfoView = {
        let view = ShowLiveRoomInfoView(type: .audience)
        return view
    }()
    
    init(viewModel: ShowLiveAudienceViewModel, frame: CGRect = .zero) {
        self.viewModel = viewModel
        super.init(frame: frame)
        backgroundColor = .white
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private var isViewReady = false
    override func didMoveToWindow() {
        super.didMoveToWindow()
        guard !isViewReady else {
            return
        }
        isViewReady = true
        constructViewHierarchy()
        activateConstraints()
        bindInteraction()
    }
    
    private func constructViewHierarchy() {
        addSubview(playerView)
        addSubview(exitButton)
        addSubview(roomInfoView)
    }
    
    private func activateConstraints() {
        playerView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        exitButton.snp.makeConstraints { make in
            make.trailing.equalToSuperview().offset(-20)
            make.bottom.equalTo(safeAreaLayoutGuide.snp.bottom).offset(-13)
        }
        roomInfoView.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(20)
            make.top.equalTo(safeAreaLayoutGuide.snp.top).offset(20)
            make.size.equalTo(CGSize(width: ScreenWidth*0.5, height: 50))
        }
    }
    
    private func bindInteraction() {
        exitButton.addTarget(self, action: #selector(exitButtonClick(sender:)), for: .touchUpInside)
        viewModel.joinRoom { [weak self] in
            guard let `self` = self else { return }
            self.playerView.startPlay(URLUtils.generatePlayUrl(self.viewModel.roomInfo.ownerId, type: .WEBRTC))
            self.playerView.setGroupId(self.viewModel.groupId)
            self.roomInfoView.setRoomInfo(self.viewModel.roomInfo)
        } failed: { [weak self] (code, msg) in
            guard let `self` = self else { return }
            self.showToast(message: msg)
        }
    }

    func showToast(message: String) {
        makeToast(message)
    }
}

//MARK: - UIButton Touch Event
extension ShowLiveAudienceRootView {
    
    @objc func exitButtonClick(sender: UIButton) {
        self.playerView.stopPlay()
        self.viewModel.exitRoom(success: nil, failed: nil)
    }
}

extension ShowLiveAudienceRootView: TUIPlayerViewDelegate {
    func onPlayStarted(_ playerView: TUIPlayerView!, url: String!) {
        
    }
    
    func onPlayStoped(_ playerView: TUIPlayerView!, url: String!) {
        debugPrint("recv player stop, now exit room")
        showToast(message: "主播已结束直播")
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.viewModel.exitRoom(success: nil, failed: nil)
        }
    }
    
    func onPlayEvent(_ playerView: TUIPlayerView!, event: TUIPlayerEvent, message: String!) {
        
    }
    
    func onRejectJoinAnchorResponse(_ playerView: TUIPlayerView!, reason: Int32) {
        switch reason {
        case 1:
            showToast(message: "对方拒绝")
        case 2:
            showToast(message: "对方正忙")
        default:
            break
        }
    }
}
