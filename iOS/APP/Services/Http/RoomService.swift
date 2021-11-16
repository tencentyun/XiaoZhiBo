//
//  RoomManager.swift
//  trtcScenesDemo
//
//  Created by 刘智民 on 2020/3/2.
//  Copyright © 2020 xcoderliu. All rights reserved.
//
import Alamofire
import UIKit

private let roomBaseUrl = SERVERLESSURL
private let createRoomUrl = "base/v1/rooms/enter_room"
private let updateRoomUrl = "base/v1/rooms/update_room"
private let queryRoomUrl = "base/v1/rooms/query_room"
private let destroyRoomUrl = "base/v1/rooms/destroy_room"

public enum RoomType: String {
    case showLive = "mlvb-show-live"
    case shoppingLive = "mlvb-shopping-live"
    case other = "other"
}

@objcMembers
class RoomCommonModel: NSObject, Codable {
    var errorCode: Int32 = -1
    var errorMessage: String = ""
}

@objcMembers
class RoomInfoModel: NSObject, Codable {
    var appId: String = ""
    var type: String = ""
    var roomId: String = ""
    var id: UInt32 = 0
    var createTime: String = ""
}

@objcMembers
class RoomInfoResultModel: NSObject, Codable {
    var errorCode: Int32 = -1
    var errorMessage: String = ""
    var data: [RoomInfoModel]? = []
}

@objcMembers
public class RoomService: NSObject {
    public static let shared = RoomService()
    override private init() {}
    public func createRoom(sdkAppID: Int32,
                           roomID: String,
                           roomName: String,
                           coverUrl: String,
                           roomType: RoomType,
                           success: @escaping () -> Void,
                           failed: @escaping (_ code: Int32, _ error: String) -> Void) {
        IMRoomManager.sharedManager().createRoom(roomId: roomID, roomName: roomName) { (code, msg) in
            if code == 0 {
                let params = ["category": roomType.rawValue,
                              "role": "anchor",
                              "roomId": roomID] as [String: Any]
                HttpBaseRequest.trtcRequest(roomBaseUrl + createRoomUrl, method: .post, parameters: params, completionHandler: { (model: HttpJsonModel) in
                    if model.errorCode == 0 {
                        let param = ["roomId": roomID,
                                     "title": roomName,
                                     "cover": coverUrl
                        ] as [String : Any]
                        HttpBaseRequest.trtcRequest(roomBaseUrl + updateRoomUrl, method: .post, parameters: param) { model in
                            if model.errorCode == 0 {
                                success()
                            }
                            else {
                                failed(model.errorCode, model.errorMessage)
                            }
                        }
                    }
                    else {
                        failed(model.errorCode, model.errorMessage)
                    }
                })
            }
            else {
                failed(code, msg ?? "")
            }
        }
    }

    public func destroyRoom(sdkAppID: Int32,
                            roomID: String,
                            roomType: RoomType,
                            success: @escaping () -> Void,
                            failed: @escaping (_ code: Int32, _ error: String) -> Void) {
        HttpBaseRequest.trtcRequest(roomBaseUrl + destroyRoomUrl, method: .post, parameters: ["roomId": roomID], completionHandler: { (model: HttpJsonModel) in
            if model.errorCode == 0 {
                success()
            } else {
                failed(model.errorCode, model.errorMessage)
            }
        })
        
        IMRoomManager.sharedManager().destroyRoom(roomId: roomID) { code, msg in
            
        }
    }

    public func enterRoom(roomId: String,
                          success: @escaping () -> Void,
                          failed: @escaping (_ code: Int32, _ error: String) -> Void) {
        IMRoomManager.sharedManager().joinGroup(roomId: roomId) { code, msg in
            TRTCLog.out("enterRoom code: \(code), msg: \(msg ?? "")")
            if (code == 0) {
                success()
            } else {
                failed(code, msg ?? "")
            }
        }
    }
    
    public func exitRoom(roomId: String,
                         success: @escaping () -> Void,
                         failed: @escaping (_ code: Int32, _ error: String) -> Void) {
        IMRoomManager.sharedManager().quitGroup(roomId: roomId) { code, msg in
            TRTCLog.out("exitRoom code: \(code), msg: \(msg ?? "")")
            if (code == 0) {
                success()
            } else {
                failed(code, msg ?? "")
            }
        }
    }
    
    public func getRoomList(sdkAppID: Int32,
                            roomType: RoomType,
                            success: @escaping (_ roomIDs: [ShowLiveRoomInfo]) -> Void,
                            failed: @escaping (_ code: Int32, _ error: String) -> Void) {
        let params = ["category": roomType.rawValue] as [String: Any]
        HttpBaseRequest.trtcRequest(roomBaseUrl + queryRoomUrl, method: .post, parameters: params, completionHandler: { (model: HttpJsonModel) in
            if model.errorCode == 0 {
                success(model.roomInfos)
            } else {
                failed(model.errorCode, model.errorMessage)
            }
        })
    }
}
