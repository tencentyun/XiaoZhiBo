//
//  TRTCPusher.h
//  TXIMSDK_TUIKit_iOS
//
//  Created by gg on 2021/9/7.
//

#import <Foundation/Foundation.h>
#import "TUIPusherKit.h"
#import "TUIPusherStreamServiceDelegate.h"

@import ImSDK_Plus;

NS_ASSUME_NONNULL_BEGIN

typedef void(^PusherActionCallback)(void);
typedef void(^ErrorCallback)(int code, NSString *des);

typedef enum : NSUInteger {
    VIDEO_RES_360 = 1,
    VIDEO_RES_540,
    VIDEO_RES_720,
    VIDEO_RES_1080,
} VideoResolution;

@class V2TXLivePusher;

@interface TUIPusherStreamService : NSObject

@property (nonatomic, readonly) V2TXLivePusher *pusher;

- (instancetype)initWithMode:(V2TXLiveMode)mode NS_DESIGNATED_INITIALIZER;

- (instancetype)init NS_UNAVAILABLE;

/// 设置TRTCCallingDelegate回调
/// @param delegate 回调实例
- (void)setDelegate:(id<TUIPusherStreamServiceDelegate>)delegate
NS_SWIFT_NAME(setDelegate(delegate:));

///开启远程用户视频渲染
- (void)startRemoteView:(NSString *)userId view:(UIView *)view
NS_SWIFT_NAME(startRemoteView(userId:view:));

///关闭远程用户视频渲染
- (void)stopRemoteView:(NSString *)userId
NS_SWIFT_NAME(stopRemoteView(userId:));

///打开摄像头
- (BOOL)openCamera:(BOOL)frontCamera view:(UIView *)view
NS_SWIFT_NAME(openCamera(frontCamera:view:));

///关闭摄像头
- (void)closeCamara NS_SWIFT_NAME(closeCamara());

/// 开始推流
- (BOOL)startPush:(NSString *)url
NS_SWIFT_NAME(startPush(url:));

/// 停止推流
- (void)stopPush NS_SWIFT_NAME(stopPush());

- (BOOL)startPK:(NSString *)streamId view:(UIView *)pkView;

- (void)stopPK;

///切换摄像头
- (void)switchCamera:(BOOL)frontCamera NS_SWIFT_NAME(switchCamera(isFront:));

///静音操作
- (void)setMicMute:(BOOL)isMute NS_SWIFT_NAME(setMicMute(isMute:));

///免提操作
- (void)setHandsFree:(BOOL)isHandsFree NS_SWIFT_NAME(setHandsFree(isHandsFree:));

// 设置选项
- (void)setMirror:(BOOL)isMirror;

- (void)setVideoResolution:(VideoResolution)resolution;

- (BOOL)startLinkMic:(NSString *)streamId view:(UIView *)view;

- (void)stopLinkMic;
@end



NS_ASSUME_NONNULL_END
