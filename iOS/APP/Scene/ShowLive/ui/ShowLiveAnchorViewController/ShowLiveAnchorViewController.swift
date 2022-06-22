//
//  ShowLiveAnchorViewController.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/9/28.
//  Copyright © 2022 Tencent. All rights reserved.

import UIKit
import TUICore

class ShowLiveAnchorViewController: UIViewController {
   
    let viewModel: ShowLiveAnchorViewModel
    init(viewModel: ShowLiveAnchorViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        TRTCLog.out("deinit \(type(of: self))")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
#if RTCube_APPSTORE
        let selector = NSSelectorFromString("showAlertUserLiveTips")
        if responds(to: selector) {
            perform(selector)
        }
#endif
        TUILogin.add(self)
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: true)
    }
    
    public override func loadView() {
        let rootView = ShowLiveAnchorRootView.init(viewModel: viewModel)
        viewModel.viewNavigator = self
        view = rootView
    }
    
    /// 取消
    @objc func cancel() {
        navigationController?.popViewController(animated: true)
    }
}

extension ShowLiveAnchorViewController: TUILoginListener {
    func onConnecting() {
        
    }
    
    func onConnectSuccess() {
        
    }
    
    func onConnectFailed(_ code: Int32, err: String!) {
        
    }
    
    func onKickedOffline() {
        (view as? ShowLiveAnchorRootView)?.pusherView.stop()
        viewModel.stopPush()
    }
    
    func onUserSigExpired() {
        
    }
}

extension ShowLiveAnchorViewController: ShowLiveAnchorViewNavigator {
    func pop() {
        navigationController?.popViewController(animated: true)
    }
    
    func present(viewController: UIViewController) {
        present(viewController, animated: true, completion: nil)
    }
}
