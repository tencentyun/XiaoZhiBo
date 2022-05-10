//
//  ShowLiveListLessRootView.swift
//  XiaoZhiBoApp
//
//  Created by jack on 2022/3/28.
//  Copyright © 2022 Tencent. All rights reserved.

import Foundation
import UIKit
import TUICore

class ShowLiveListLiteRootView: UIView {
    
    let viewModel: ShowLiveListViewModel
    private var isViewReady = false
    
    init(frame: CGRect = .zero, viewModel: ShowLiveListViewModel) {
        self.viewModel = viewModel
        super.init(frame: frame)
        self.viewModel.viewResponder = self
        backgroundColor = UIColor("F4F5F9")
    }
    
    required init?(coder: NSCoder) {
        fatalError("can't init this viiew from coder")
    }
    
    private lazy var roomIdTextField: UITextField = {
        let textField = UITextField(frame: .zero)
        textField.backgroundColor = .white
        textField.font = UIFont.systemFont(ofSize: 16)
        textField.textColor = UIColor("333333")
        textField.keyboardType = .asciiCapableNumberPad
        textField.attributedPlaceholder = NSAttributedString(string: .inputRoomId, attributes: [NSAttributedString.Key.font:  UIFont.systemFont(ofSize: 16), NSAttributedString.Key.foregroundColor: UIColor("BBBBBB")])
        textField.delegate = self
        return textField
    }()
    
    private lazy var textFieldSpacingLine: UIView = {
        let view = UIView(frame: .zero)
        view.backgroundColor = UIColor("EEEEEE")
        return view
    }()
    
    private lazy var joinButton: UIButton = {
        let btn = UIButton(type: .custom)
        btn.setTitleColor(.white, for: .normal)
        btn.setTitle(.joinText, for: .normal)
        btn.adjustsImageWhenHighlighted = false
        btn.setBackgroundImage(UIColor("006EFF").trans2Image(), for: .normal)
        btn.titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 18)
        btn.layer.masksToBounds = true
        return btn
    }()
    
    private lazy var createButton: UIButton = {
        let btn = UIButton.init(type: .custom)
        btn.titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 18)
        btn.titleLabel?.textColor = .white
        btn.setImage(UIImage.init(named: "add"), for: .normal)
        btn.setTitle(.createText, for: .normal)
        btn.adjustsImageWhenHighlighted = false
        btn.clipsToBounds = true
        btn.backgroundColor = UIColor("006EFF")
        btn.titleEdgeInsets = UIEdgeInsets(top: 0, left: 4, bottom: 0, right: -4)
        btn.imageEdgeInsets = UIEdgeInsets(top: 0, left: -4, bottom: 0, right: 4)
        return btn
    }()
    
    override func didMoveToWindow() {
        super.didMoveToWindow()
        guard !isViewReady else {
            return
        }
        isViewReady = true
        constructViewHierarchy()
        activateConstraints()
        bindInteraction()
        checkJoinState()
    }
    
    override func draw(_ rect: CGRect) {
        super.draw(rect)
        createButton.layer.cornerRadius = createButton.frame.height * 0.5
        joinButton.layer.cornerRadius = joinButton.frame.height * 0.5
    }

    private func constructViewHierarchy() {
        addSubview(roomIdTextField)
        addSubview(textFieldSpacingLine)
        addSubview(joinButton)
        addSubview(createButton)
    }

    private func activateConstraints() {
        roomIdTextField.snp.makeConstraints { (make) in
            make.leading.equalTo(30)
            make.trailing.equalTo(-30)
            make.top.equalTo(kDeviceSafeTopHeight + 10)
            make.height.equalTo(50)
        }
        textFieldSpacingLine.snp.makeConstraints { (make) in
            make.height.equalTo(1)
            make.leading.trailing.equalTo(roomIdTextField)
            make.top.equalTo(roomIdTextField.snp.bottom)
        }
        joinButton.snp.makeConstraints { (make) in
            make.leading.trailing.equalToSuperview().inset(40)
            make.top.equalTo(textFieldSpacingLine.snp.bottom).offset(30)
            make.height.equalTo(52)
        }
        createButton.snp.makeConstraints { (make) in
            make.bottom.equalToSuperview().offset(-90 - kDeviceSafeBottomHeight)
            make.leading.trailing.equalToSuperview().inset(40)
            make.height.equalTo(52)
        }
    }

    private func bindInteraction() {
        createButton.addTarget(self, action: #selector(createAction), for: .touchUpInside)
        joinButton.addTarget(self, action: #selector(joinAction), for: .touchUpInside)
        addTapGesture(target: self, action: #selector(hiddenKeyboard))
    }
    
    private func checkJoinState(_ string: String? = nil) {
        var isEnabled = false
        if let text = string {
            let scan: Scanner = Scanner(string: text)
            var val:Int = 0
            if scan.scanInt(&val) && scan.isAtEnd {
                isEnabled = true
            }
        }
        joinButton.isEnabled = isEnabled
        let color = isEnabled ? UIColor("006EFF") : UIColor("DBDBDB")
        joinButton.setBackgroundImage(color.trans2Image(), for: .normal)
    }
}

// MARK: - Touch Action
extension ShowLiveListLiteRootView {
    
    @objc private func createAction() {
        viewModel.pushShowLiveAnchorRoom()
    }
    
    @objc private func joinAction() {
        guard let roomId = roomIdTextField.text, !roomId.isEmpty else {
            return
        }
        IMRoomManager.sharedManager().getIMRoomInfoList(roomIds: [roomId]) { [weak self] (code, message, roomInfos) in
            guard let self = self else { return }
            if let roomInfo = roomInfos.first {
                if TUILogin.getUserID() == roomInfo.roomID {
                    // 开始进入已经存在的房间
                    self.viewModel.startEnterExistRoom(info: roomInfo)
                } else {
                    // 正常进房逻辑
                    self.viewModel.enterRoom(info: roomInfo)
                }
            } else {
                // 房间不存在
                self.makeToast(message)
            }
        }
    }
    
    @objc private func hiddenKeyboard() {
        endEditing(true)
    }
}

// MARK: - UITextFieldDelegate
extension ShowLiveListLiteRootView: UITextFieldDelegate {
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        checkJoinState(textField.text)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        hiddenKeyboard()
        return true
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        if let text = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) {
            checkJoinState(text)
        } else {
            checkJoinState()
        }
        return true
    }
    
}

// MARK: - ShowLiveListViewResponder
extension ShowLiveListLiteRootView: ShowLiveListViewResponder {
    
    func showToast(message: String) {
        makeToast(message)
    }
    
    func refreshList() {
        
    }
    
    func stopListRefreshing() {
        
    }
    
    func showLoading(message: String) {
        
    }
    
    func hideLoading() {
        
    }
    
}

// MARK: - internationalization string
fileprivate extension String {
    static let inputRoomId = ShowLiveLocalize("Scene.ShowLive.List.inputroomid")
    static let joinText = ShowLiveLocalize("Scene.ShowLive.List.joinroom")
    static let createText = ShowLiveLocalize("Scene.ShowLive.List.createroom")
}
