//
//  TRTCLoginLiteViewController.swift
//  XiaoZhiBoApp
//
//  Created by jack on 2022/3/25.
//

import Foundation
import UIKit
import TUICore

class TRTCLoginLiteViewController: UIViewController {
    
    private lazy var bgView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "login_bg"))
        imageView.clipsToBounds = true
        return imageView
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel(frame: .zero)
        label.font = UIFont.systemFont(ofSize: 32)
        label.textColor = UIColor.white
        label.text = .titleText
        label.numberOfLines = 2
        label.adjustsFontSizeToFitWidth = true
        return label
    }()
    
    private lazy var textField: UITextField = {
        let textField = UITextField(frame: .zero)
        textField.backgroundColor = .white
        textField.font = UIFont.systemFont(ofSize: 16)
        textField.textColor = UIColor("333333")
        textField.keyboardType = .asciiCapableNumberPad
        textField.attributedPlaceholder = NSAttributedString(string: .inputUserIdText, attributes: [NSAttributedString.Key.font:  UIFont.systemFont(ofSize: 16), NSAttributedString.Key.foregroundColor: UIColor("BBBBBB")])
        textField.delegate = self
        return textField
    }()
    
    private lazy var textFieldSpacingLine: UIView = {
        let view = UIView(frame: .zero)
        view.backgroundColor = UIColor("EEEEEE")
        return view
    }()
    
    private lazy var loginButton: UIButton = {
        let btn = UIButton(type: .custom)
        btn.setTitleColor(.white, for: .normal)
        btn.setTitle(.loginText, for: .normal)
        btn.adjustsImageWhenHighlighted = false
        btn.setBackgroundImage(UIColor("006EFF").trans2Image(), for: .normal)
        btn.titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 18)
        btn.layer.shadowColor = UIColor("006EFF").cgColor
        btn.layer.shadowOffset = CGSize(width: 0, height: 6)
        btn.layer.shadowRadius = 16
        btn.layer.shadowOpacity = 0.4
        btn.layer.cornerRadius = 24
        btn.layer.masksToBounds = true
        return btn
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.white
        constructViewHierarchy()
        activateConstraints()
        bindInteraction()
        checkLoginState()
    }
    
    private func constructViewHierarchy() {
        view.addSubview(bgView)
        bgView.addSubview(titleLabel)
        view.addSubview(textField)
        view.addSubview(textFieldSpacingLine)
        view.addSubview(loginButton)
    }
    
    private func activateConstraints() {
        bgView.snp.makeConstraints { (make) in
            make.top.leading.trailing.equalToSuperview()
        }
        titleLabel.snp.makeConstraints { (make) in
            make.top.equalToSuperview().offset(kDeviceSafeTopHeight+64)
            make.leading.equalToSuperview().offset(40)
            make.trailing.lessThanOrEqualToSuperview().offset(-40)
            make.height.equalTo(100)
            make.bottom.equalTo(-40)
        }
        textField.snp.makeConstraints { (make) in
            make.leading.equalTo(30)
            make.trailing.equalTo(-30)
            make.top.equalTo(bgView.snp.bottom).offset(30)
            make.height.equalTo(50)
        }
        textFieldSpacingLine.snp.makeConstraints { (make) in
            make.height.equalTo(1)
            make.leading.trailing.equalTo(textField)
            make.top.equalTo(textField.snp.bottom)
        }
        loginButton.snp.makeConstraints { (make) in
            make.height.equalTo(48)
            make.leading.trailing.equalToSuperview().inset(40)
            make.top.equalTo(textFieldSpacingLine.snp.bottom).offset(100)
        }
    }
    
    private func bindInteraction() {
        loginButton.addTarget(self, action: #selector(loginAction), for: .touchUpInside)
        view.addTapGesture(target: self, action: #selector(hiddenKeyboard))
    }
    
    
    private func checkLoginState(_ string: String? = nil) {
        var isEnabled = false
        if let text = string {
            let scan: Scanner = Scanner(string: text)
            var val:Int = 0
            if scan.scanInt(&val) && scan.isAtEnd {
                isEnabled = true
            }
        }
        loginButton.isEnabled = isEnabled
        let color = isEnabled ? UIColor("006EFF") : UIColor("DBDBDB")
        loginButton.setBackgroundImage(color.trans2Image(), for: .normal)
    }
}

// MARK: - UITextFieldDelegate
extension TRTCLoginLiteViewController: UITextFieldDelegate {
    
    @objc private func hiddenKeyboard() {
        textField.resignFirstResponder()
        checkLoginState(textField.text)
    }

    func textFieldDidEndEditing(_ textField: UITextField) {
        checkLoginState(textField.text)
    }
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        hiddenKeyboard()
        return true
    }
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        if let text = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) {
            checkLoginState(text)
        } else {
            checkLoginState()
        }
        return true
    }
}

// MARK: -  Touch Action
extension TRTCLoginLiteViewController {

    @objc private func loginAction() {
        textField.resignFirstResponder()
        guard let userId = textField.text else { return }
        let userSig = GenerateTestUserSig.genTestUserSig(identifier: userId)
        let userModel = UserModel(token: "", userId: userId, userSig: userSig, apaasAppId: HttpLogicRequest.sdkAppId.description, apaasUserId: userId, sdkUserSig: userSig)
        IMLogicRequest.imUserLogin(currentUserModel: userModel) { [weak self] data in
            guard let self = self, let dataModel = data else { return }
            ProfileManager.sharedManager().updateUserModel(dataModel)
            if dataModel.name.isEmpty {
                let vc = TRTCRegisterViewController()
                self.navigationController?.pushViewController(vc, animated: true)
            } else {
                AppUtils.shared.appDelegate.showMainViewController()
            }
        } failed: { [weak self](errorCode, errorMessage) in
            guard let self = self else { return }
            self.view.makeToast(errorMessage)
        }

    }
}

// MARK: - internationalization string
fileprivate extension String {
    static let titleText = LoginLocalize("Demo.TRTC.Login.welcome")
    static let loginText = LoginLocalize("V2.Live.LoginMock.login")
    static let inputUserIdText = LoginLocalize("Demo.TRTC.Login.inputUserId")
    
}
