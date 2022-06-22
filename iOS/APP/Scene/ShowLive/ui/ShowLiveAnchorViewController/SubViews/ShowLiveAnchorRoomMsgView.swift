//
//  ShowLiveAnchorRoomMsgView.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/9/28.
//  Copyright © 2022 Tencent. All rights reserved.

import UIKit
import CoreServices
import Toast_Swift
import Kingfisher

class ShowLiveAnchorRoomMsgView: UIView {
    
    let viewModel: ShowLiveAnchorViewModel
    
    private lazy var roomImageView: UIImageView = {
        let imageView = UIImageView.init(image: UIImage.init(named: "showLive_cover1"))
        if let url = URL.init(string: viewModel.roomInfo.coverUrl) {
            imageView.kf.setImage(with: .network(url))
        }
        imageView.layer.masksToBounds = true
        return imageView
    }()
    
    private lazy var roomIdLabel: UILabel = {
        let label = UILabel.init(frame: .zero)
        label.textColor = .white
        label.textAlignment = .left
        label.text = viewModel.roomInfo.roomID
        return label
    }()
    
    private lazy var roomNameTextField: UITextField = {
        let textField = UITextField(frame: .zero)
        textField.font = UIFont(name: "PingFangSC-Regular", size: 16)
        textField.textColor = .white
        textField.text = ShowLiveLocalizeReplaceOneCharacter(origin: "Scene.ShowLive.Create.xxxsroom", xxx_replace: viewModel.roomInfo.ownerName)
        textField.backgroundColor = .clear
        textField.delegate = self
#if RTCube_APPSTORE
        textField.isUserInteractionEnabled = false
#endif
        return textField
    }()
    
    init(frame: CGRect = .zero, viewModel: ShowLiveAnchorViewModel) {
        self.viewModel = viewModel
        super.init(frame: frame)
        backgroundColor = UIColor.gray.withAlphaComponent(0.5)
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
    }
    
    override func draw(_ rect: CGRect) {
        super.draw(rect)
        roomImageView.layer.cornerRadius = roomImageView.frame.height * 0.5
    }
    
    private func constructViewHierarchy() {
        addSubview(roomImageView)
        addSubview(roomIdLabel)
        addSubview(roomNameTextField)
    }
    
    private func activateConstraints() {
        roomImageView.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.top.equalTo(15)
            make.size.equalTo(CGSize.init(width: 50, height: 50))
        }
        
        roomIdLabel.snp.makeConstraints { make in
            make.left.equalTo(roomImageView.snp.right).offset(10)
            make.top.equalTo(roomImageView.snp.top)
        }
        
        roomNameTextField.snp.makeConstraints { make in
            make.left.equalTo(roomImageView.snp.right).offset(10)
            make.bottom.equalTo(roomImageView.snp.bottom)
            make.height.equalTo(20)
            make.right.equalToSuperview().offset(-10)
            make.bottom.equalToSuperview().offset(-15)
        }
    }
}


extension ShowLiveAnchorRoomMsgView: UITextFieldDelegate {
    func textFieldDidEndEditing(_ textField: UITextField) {
        viewModel.roomInfo.roomName = textField.text ?? ""
    }
    
    @discardableResult
    override func resignFirstResponder() -> Bool {
        super.resignFirstResponder()
        return roomNameTextField.resignFirstResponder()
    }
    
    @discardableResult
    override func becomeFirstResponder() -> Bool {
        super.becomeFirstResponder()
        return roomNameTextField.becomeFirstResponder()
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        roomNameTextField.resignFirstResponder()
    }
}

// MARK: - internationalization string
fileprivate extension String {
    static let selectPhotoText = ShowLiveLocalize("Scene.ShowLive.Anchor.selectPhoto")
    static let cameraTitleText = ShowLiveLocalize("Scene.ShowLive.Anchor.camera")
    static let photoLibraryText = ShowLiveLocalize("Scene.ShowLive.Anchor.photolibrary")
    static let cameraAuthText = ShowLiveLocalize("Scene.ShowLive.Authorization.camera")
    static let photoLibraryAuthText = ShowLiveLocalize("Scene.ShowLive.Authorization.photolibrary")
    static let cancelText = ShowLiveLocalize("Scene.ShowLive.Anchor.cancel")
}
