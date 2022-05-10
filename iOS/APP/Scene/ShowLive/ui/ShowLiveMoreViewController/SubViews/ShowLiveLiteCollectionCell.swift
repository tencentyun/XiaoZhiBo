//
//  ShowLiveLiteCollectionCell.swift
//  XiaoZhiBoApp
//
//  Created by jack on 2022/3/28.
//  Copyright © 2022 Tencent. All rights reserved.

import Foundation
import UIKit

class ShowLiveLiteCollectionCell: UICollectionViewCell {
    
    public var joinRoomBlock: ((String)->Void)? = nil
    
    private var isViewReady = false
    private lazy var roomIdTextField: UITextField = {
        let textField = UITextField(frame: .zero)
        textField.backgroundColor = UIColor.black
        textField.font = UIFont.systemFont(ofSize: 16)
        textField.textColor = UIColor.white
        textField.keyboardType = .asciiCapableNumberPad
        textField.attributedPlaceholder = NSAttributedString(string: .inputRoomId,
                                                             attributes: [
                                                                NSAttributedString.Key.font: UIFont.systemFont(ofSize: 16),
                                                                NSAttributedString.Key.foregroundColor: UIColor("BBBBBB")])
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
        joinButton.layer.cornerRadius = joinButton.frame.height * 0.5
    }

    private func constructViewHierarchy() {
        addSubview(roomIdTextField)
        addSubview(textFieldSpacingLine)
        addSubview(joinButton)
    }

    private func activateConstraints() {
        roomIdTextField.snp.makeConstraints { (make) in
            make.leading.equalTo(15)
            make.trailing.equalTo(-15)
            make.top.equalTo(10)
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
    }

    private func bindInteraction() {
        joinButton.addTarget(self, action: #selector(joinAction), for: .touchUpInside)
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
extension ShowLiveLiteCollectionCell {
    
    @objc private func joinAction() {
        guard let roomId = roomIdTextField.text, !roomId.isEmpty else {
            return
        }
        roomIdTextField.resignFirstResponder()
        joinRoomBlock?(roomId)
    }
    
}

// MARK: - UITextFieldDelegate
extension ShowLiveLiteCollectionCell: UITextFieldDelegate {
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        checkJoinState(textField.text)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        endEditing(true)
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

// MARK: - internationalization string
fileprivate extension String {
    static let inputRoomId = ShowLiveLocalize("Scene.ShowLive.List.inputroomid")
    static let joinText = ShowLiveLocalize("Scene.ShowLive.List.joinroom")
    static let createText = ShowLiveLocalize("Scene.ShowLive.List.createroom")
}
