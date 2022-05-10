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
