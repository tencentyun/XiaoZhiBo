//
//  TUIPlayerPresenter.h
//  TUIPlayer
//
//  Created by gg on 2021/9/14.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol TUIPlayerPresenterDelegate <NSObject>

- (void)onReceiveLinkMicInvite:(NSString *)inviter cmd:(NSString *)cmd streamId:(NSString *)streamId;

- (void)onAcceptLinkMicInvite:(NSString *)cmd streamId:(NSString *)streamId;
- (void)onRejectLinkMicInvite:(NSString *)cmd reason:(int)reason;

- (void)onStartLinkMic:(NSString *)cmd streamId:(NSString *)streamId;
- (void)onStopLinkMic:(NSString *)cmd;
- (void)onLinkMicInviteTimeout;

- (void)onRemoteStopPush;

@end

@interface TUIPlayerPresenter : NSObject

@property (nonatomic, weak) id <TUIPlayerPresenterDelegate> delegate;

- (BOOL)startPlay:(NSString *)url atView:(UIView *)view;

- (void)stopPlay;

- (BOOL)sendLinkMicRequest:(NSString *)userId;

- (void)cancelLinkMicRequest;

- (void)sendStopLinkMic;

- (void)startLinkMicWithUser:(NSString *)remoteUserId atView:(UIView *)view complete:(void (^) (BOOL success))complete;

- (void)stopLinkMic;

@end

NS_ASSUME_NONNULL_END
