//
//  TRTCStartLoginViewController.swift
//  XiaoZhiBoApp
//
//  Created by jack on 2022/3/25.
//

import Foundation
import Toast_Swift

class TRTCStartLoginViewController: UIViewController {
    
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
    
    lazy var tipTextView: TRTCLoginAgreementTextView = {
        let textView = TRTCLoginAgreementTextView(frame: .zero)
        textView.backgroundColor = .clear
        textView.delegate = self
        
        let content: String = String.tipText + String.setupServiceText
        let setupRange = (content as NSString).range(of: String.setupServiceText)
        
        let attr = NSMutableAttributedString(string: content, attributes: [
            NSAttributedString.Key.font: UIFont.systemFont(ofSize: 14),
            NSAttributedString.Key.foregroundColor: UIColor.darkGray
        ])
        attr.addAttribute(.link, value: "setupService", range: setupRange)
        attr.addAttribute(.foregroundColor, value: UIColor.blue, range: setupRange)
        textView.attributedText = attr
        return textView
    }()
    
    private lazy var withServiceButton: UIButton = {
        let btn = UIButton(type: .custom)
        btn.setTitleColor(.white, for: .normal)
        btn.setTitle(.withServiceText, for: .normal)
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
    
    private lazy var withoutServiceButton: UIButton = {
        let btn = UIButton(type: .custom)
        btn.setTitleColor(.white, for: .normal)
        btn.setTitle(.withoutServiceText, for: .normal)
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
    }
    
    private func constructViewHierarchy() {
        view.addSubview(bgView)
        bgView.addSubview(titleLabel)
        view.addSubview(tipTextView)
        view.addSubview(withServiceButton)
        view.addSubview(withoutServiceButton)
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
        tipTextView.snp.makeConstraints { (make) in
            make.leading.equalTo(30)
            make.trailing.equalTo(-30)
            make.top.equalTo(bgView.snp.bottom).offset(30)
            make.height.equalTo(80)
        }
        withServiceButton.snp.makeConstraints { (make) in
            make.height.equalTo(48)
            make.leading.trailing.equalToSuperview().inset(40)
            make.top.equalTo(tipTextView.snp.bottom).offset(30)
        }
        withoutServiceButton.snp.makeConstraints { (make) in
            make.height.equalTo(48)
            make.leading.trailing.equalToSuperview().inset(40)
            make.top.equalTo(withServiceButton.snp.bottom).offset(30)
        }
    }
    
    private func bindInteraction() {
        withServiceButton.addTarget(self, action: #selector(withServiceAction), for: .touchUpInside)
        withoutServiceButton.addTarget(self, action: #selector(withoutServiceAction), for: .touchUpInside)
    }
}

// MARK: - Touch Action
extension TRTCStartLoginViewController {
    /// 配置后台服务h5
    private func showSetupService() {
        guard let url = URL(string: "https://cloud.tencent.com/document/product/454/38625") else {
            return
        }
        let vc = TRTCWebViewController(url: url, title: .setupServiceText)
        navigationController?.pushViewController(vc, animated: true)
    }
    
    /// 我已配置后台
    @objc private func withServiceAction() {
        if SERVERLESSURL.isEmpty || SERVERLESSURL == "PLACEHOLDER" {
            view.makeToast(.showServiceTipText)
            return
        }
        MLVBConfigManager.shared.isSetupService = true
        let loginViewController = TRTCLoginViewController()
        navigationController?.pushViewController(loginViewController, animated: true)
    }
    
    /// 暂不配置
    @objc private func withoutServiceAction() {
        MLVBConfigManager.shared.isSetupService = false
        let loginViewController = TRTCLoginLiteViewController()
        navigationController?.pushViewController(loginViewController, animated: true)
    }
}

// MARK: - UITextViewDelegate
extension TRTCStartLoginViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        if URL.absoluteString == "setupService" {
            showSetupService()
        }
        return true
    }
}

// MARK: - internationalization string
fileprivate extension String {
    static let titleText = LoginLocalize("Demo.TRTC.Login.welcome")
    static let tipText = LoginLocalize("Demo.TRTC.Login.setupServiceTip")
    static let setupServiceText = LoginLocalize("Demo.TRTC.Login.setupService")
    static let withServiceText = LoginLocalize("Demo.TRTC.Login.runWithService")
    static let withoutServiceText = LoginLocalize("Demo.TRTC.Login.runWithoutService")
    static let showServiceTipText = LoginLocalize("Demo.TRTC.Login.checkService")
}
