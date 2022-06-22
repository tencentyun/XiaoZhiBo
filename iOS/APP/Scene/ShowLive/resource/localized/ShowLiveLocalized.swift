//
//  ShowLiveLocalized.swift
//  XiaoZhiBoApp
//
//  Created by adams on 2021/5/10.
//  Copyright © 2022 Tencent. All rights reserved.

import Foundation

//MARK: ShowLive
let ShowLiveLocalizeTableName = "ShowLiveLocalized"
func ShowLiveLocalize(_ key: String) -> String {
    return localizeFromTable(key: key, table: ShowLiveLocalizeTableName)
}

func ShowLiveLocalizeReplaceOneCharacter(origin: String, xxx_replace: String) -> String {
    return ShowLiveLocalize(origin).replacingOccurrences(of: "xxx", with: xxx_replace)
}

//let ShowLiveBundle: Bundle? = {
//    guard let url = Bundle.main.url(forResource: "ShowLiveAssets", withExtension: "bundle") else {
//        return nil
//    }
//    return Bundle(url: url)
//}()
