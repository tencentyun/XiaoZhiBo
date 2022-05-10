//
//  MLVBConfigManager.swift
//  XiaoZhiBoApp
//
//  Created by jack on 2022/1/17.
//

import Foundation

/// 隐私合规配置路径
let Privacy_PlistPath: String? = {
    guard let privacyPath = Bundle.main.path(forResource: "Privacy", ofType: "plist") else {
        return nil
    }
    return privacyPath
}()
/// 隐私合规配置信息
let Privacy_Info: NSDictionary = {
    guard let privacyPath = Privacy_PlistPath else  {
        return NSDictionary()
    }
    guard let privacyInfo = NSDictionary(contentsOfFile: privacyPath) else {
        return NSDictionary()
    }
    return privacyInfo
}()

/// 用户协议
let WEBURL_Agreement: String = {
    return (Privacy_Info["userProtocolURL"] as? String) ?? ""
}()

/// 隐私协议摘要
let WEBURL_PrivacySummary: String = {
    return (Privacy_Info["privacySummaryURL"] as? String) ?? ""
}()

/// 隐私协议
let WEBURL_Privacy: String = {
    return (Privacy_Info["privacyURL"] as? String) ?? ""
}()

typealias MLVBConfigManager = ConfigManager
class ConfigManager {
    
    private static let setupServiceCacheKey = "isSetupService+=37d89=+"
    
    static let shared: MLVBConfigManager = MLVBConfigManager()
    /// 本地配置文件信息
    var configInfo: [String: Any] = [:]
    /// 是否支持后台服务
    var isSetupService: Bool {
        set {
            UserDefaults.standard.set(newValue, forKey: MLVBConfigManager.setupServiceCacheKey)
        }
        get {
            if MLVBConfigManager.isGithub() {
                return UserDefaults.standard.bool(forKey: MLVBConfigManager.setupServiceCacheKey)
            }
            return true
        }
    }
    
    init() {
        loadConfig()
    }
    
}

// MARK: - Public 便利获取Config配置信息
extension ConfigManager {
    /// 是否支持直播间广告外链
    public class func enableLiveRoomAdLink() -> Bool {
        return (shared.configInfo["enableLiveRoomAdLink"] as? NSNumber)?.boolValue ?? false
    }
    /// 是否为Github发布
    public class func isGithub() -> Bool {
        return (shared.configInfo["isGithub"] as? NSNumber)?.boolValue ?? false
    }
}

// MARK: - Private
extension ConfigManager {
    
    /// 加载本地Config文件
    private func loadConfig() {
        let plistName: String = "Config"
        guard let plistPath: String = Bundle.main.path(forResource: plistName, ofType: "plist") else {
            return
        }
        guard let plistData = FileManager.default.contents(atPath: plistPath) else {
            return
        }
        var propertyListFormat = PropertyListSerialization.PropertyListFormat.xml
        guard let config = try? PropertyListSerialization.propertyList(from: plistData, options: .mutableContainersAndLeaves, format: &propertyListFormat) as? [String: Any] else {
            return
        }
        configInfo = config
    }
    
}
