//
//  TUIPusherPresenter.h
//  Alamofire
//
//  Created by gg on 2021/9/13.
//

#import <Foundation/Foundation.h>
#import "TUIPusherStreamService.h"
#import "TUIPusherSignalingService.h"

NS_ASSUME_NONNULL_BEGIN

@protocol TUIPusherViewDelegate;
@class TUIPusherView;

@protocol TUIPusherPresenterDelegate <NSObject>

- (void)onStreamServiceError:(V2TXLiveCode)code msg:(NSString *)msg;

- (void)onSignalingError:(NSString *)cmd code:(int)code message:(NSString *)msg;

- (void)onReceivePKInvite:(NSString *)inviter cmd:(NSString *)cmd streamId:(NSString *)streamId;
- (void)onAcceptPKInvite:(NSString *)cmd streamId:(NSString *)streamId;
- (void)onRejectPKInvite:(NSString *)cmd reason:(int)reason;
- (void)onCancelPK:(NSString *)cmd;
- (void)onStopPK:(NSString *)cmd;
- (void)onTimeoutPK;

- (void)onReceiveLinkMicInvite:(NSString *)inviter cmd:(NSString *)cmd streamId:(NSString *)streamId;
- (void)onStartLinkMic:(NSString *)cmd streamId:(NSString *)streamId;
- (void)onCancelLinkMic:(NSString *)cmd;
- (void)onStopLinkMic:(NSString *)cmd;
- (void)onTimeoutLinkMic;
@end

@interface TUIPusherPresenter : NSObject

@property (nonatomic, copy, null_resettable) NSString *pushUrl;
@property (nonatomic, weak) id <TUIPusherPresenterDelegate> delegate;
@property (nonatomic, weak) id <TUIPusherViewDelegate> pusherViewDelegate;
@property (nonatomic, weak) TUIPusherView *pusherView;
@property (nonatomic, copy) NSString *remoteStreamId;

@property (nonatomic, assign) BOOL isFrontCamera;
@property (nonatomic, assign) BOOL isMirror;

@property (nonatomic, readonly) NSString *currentUserId;

@property (nonatomic, readonly, nullable) V2TXLivePusher *pusher;

@property (nonatomic, readonly) BOOL isInPK;
@property (nonatomic, readonly) BOOL isInLinkMic;

- (instancetype)initWithPusherView:(TUIPusherView *)pusherView;

- (BOOL)checkPushUrl:(NSString *)url;

- (BOOL)checkLoginStatus;

- (BOOL)start:(NSString *)url view:(UIView *)view;

- (void)stop;

- (BOOL)startPush:(NSString *)url;

- (void)stopPush;

- (void)switchCamera:(BOOL)isFrontCamera;

- (void)setMirror:(BOOL)isMirror;

- (void)setVideoResolution:(VideoResolution)resolution;

- (BOOL)sendPKRequest:(NSString *)userID;
- (void)cancelPKRequest;
- (void)acceptPK;
- (void)rejectPK;
- (BOOL)startPKWithUser:(NSString *)remoteUserId atView:(UIView *)view;
- (void)sendStopPK;
- (void)stopPK;

- (void)acceptLinkMic;
- (void)rejectLinkMic;
- (BOOL)startLinkMicWithUser:(NSString *)remoteUserId atView:(UIView *)view;
- (void)sendStopLinkMic;
- (void)stopLinkMic;
@end

NS_ASSUME_NONNULL_END
