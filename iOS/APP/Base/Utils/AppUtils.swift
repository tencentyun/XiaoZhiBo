//
//  appUtil.swift
//  trtcScenesDemo
//
//  Created by xcoderliu on 12/24/19.
//  Copyright © 2019 xcoderliu. All rights reserved.
//
// 用于TRTC_SceneDemo

import UIKit
import ImSDK_Plus

//推送证书 ID
#if DEBUG
    let timSdkBusiId: UInt32 = 18069
#else
    let timSdkBusiId: UInt32 = 18070
#endif

@objcMembers
public class AppUtils: NSObject {
    
    public static let shared = AppUtils()
    private override init() {}
    
    var appDelegate: AppDelegate {
        return UIApplication.shared.delegate as! AppDelegate
    }

    var curUserId: String {
         get {
        #if NOT_LOGIN
            return ""
        #else
            return V2TIMManager.sharedInstance()?.getLoginUser() ?? ""
        #endif
        }
    }
    
    var deviceToken: Data? = nil

    //MARK: - UI
    func alertUserTips(_ vc: UIViewController) {
        // 提醒用户不要用Demo App来做违法的事情
        // 每天提醒一次
        let nowDay = Calendar.current.component(.day, from: Date())
        if let day = UserDefaults.standard.object(forKey: "UserTipsKey") as? Int {
            if day == nowDay {
                return
            }
        }
        UserDefaults.standard.set(nowDay, forKey: "UserTipsKey")
        UserDefaults.standard.synchronize()
        let alertVC = UIAlertController(title:LoginLocalize("LoginNetwork.AppUtils.warmprompt"), message: LoginLocalize("LoginNetwork.AppUtils.tomeettheregulatory"), preferredStyle: UIAlertController.Style.alert)
        let okView = UIAlertAction(title: LoginLocalize("LoginNetwork.AppUtils.determine"), style: UIAlertAction.Style.default, handler: nil)
        alertVC.addAction(okView)
        vc.present(alertVC, animated: true, completion: nil)
    }
    
    
    static func getCurrentWindow() -> UIWindow? {
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
            if let keyWindow = windowScene.windows.first {
                return keyWindow
            }
        }
        return nil
    }
}
