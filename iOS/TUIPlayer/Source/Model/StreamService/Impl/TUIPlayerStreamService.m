//
//  TUIPlayerStreamService.m
//  TUIPlayer
//
//  Created by gg on 2021/9/14.
//

#import "TUIPlayerStreamService.h"
#import "TUIPlayerKit.h"
#import "TUIPlayerLinkURLUtils.h"
#import <TUICore/TUILogin.h>
#import "TUIPlayerHeader.h"

@interface TUIPlayerStreamService () <V2TXLivePlayerObserver, V2TXLivePusherObserver>

@property (nonatomic, strong) V2TXLivePlayer *player;

@property (nonatomic, strong) V2TXLivePusher *pusher;

@property (nonatomic, assign) BOOL isFrontCamera;

@property (nonatomic,  copy ) NSString *originalPlayUrl;

@property (nonatomic, assign) BOOL isInLinkMic;

@property (nonatomic,  copy ) void (^linkMicStatus) (BOOL success);
@end

@implementation TUIPlayerStreamService

- (BOOL)startPlay:(NSString *)url atView:(nonnull UIView *)view {
    self.originalPlayUrl = url;
    V2TXLiveCode res = [self.player setRenderView:view];
    res += [self.player startPlay:url];
    LOGD("【Player】start play: [%d] %@", res, url);
    return res == V2TXLIVE_OK;
}

- (void)stopPlay {
    LOGD("【Player】stop play");
    if (self.isInLinkMic) {
        [self.pusher stopPush];
    }
    [self.player stopPlay];
}

- (BOOL)startLinkMic:(NSString *)streamId view:(UIView *)view complete:(nonnull void (^)(BOOL))complete {
    
    self.isInLinkMic = YES;
    self.linkMicStatus = complete;
    
    // restart player
    V2TXLiveCode res = [self.player stopPlay];
    
    NSString *playUrl = [TUIPlayerLinkURLUtils generatePlayUrl:streamId];
    res += [self.player startPlay:playUrl];
    
    // set pusher
    res += [self.pusher startCamera:self.isFrontCamera];
    
    res += [self.pusher setRenderView:view];
    
    [[self.pusher getDeviceManager] enableCameraAutoFocus:YES];
    
    res += [self.pusher startMicrophone];
    
    NSString *pushUrl = [TUIPlayerLinkURLUtils generatePushUrl:[TUILogin getUserID]];
    res += [self.pusher startPush:pushUrl];
    
    LOGD("【Player】start link mic: %d", res);
    
    return res == V2TXLIVE_OK;
}

- (void)stopLinkMic {
    
    LOGD("【Player】stop linkmic");
    
    self.isInLinkMic = NO;
    [self.pusher stopCamera];
    [self.pusher stopPush];
    
    [self.player stopPlay];
    [self.player startPlay:self.originalPlayUrl];
}

#pragma mark - V2TXLivePlayerObserver
- (void)onError:(id<V2TXLivePlayer>)player
           code:(V2TXLiveCode)code
        message:(NSString *)msg
      extraInfo:(NSDictionary *)extraInfo {
    if (code == V2TXLIVE_ERROR_DISCONNECTED) {
        if ([self.delegate respondsToSelector:@selector(onRemoteStopPush)]) {
            [self.delegate onRemoteStopPush];
        }
    }
}

#pragma mark - V2TXLivePusherObserver
- (void)onError:(V2TXLiveCode)code message:(NSString *)msg extraInfo:(NSDictionary *)extraInfo {
    if (self.linkMicStatus != nil) {
        self.linkMicStatus(NO);
        self.linkMicStatus = nil;
    }

}

- (void)onCaptureFirstVideoFrame {
    if (self.linkMicStatus != nil) {
        self.linkMicStatus(YES);
        self.linkMicStatus = nil;
    }
}

#pragma mark - Initialize
- (instancetype)init {
    if (self = [super init]) {
        self.isFrontCamera = YES;
        self.isInLinkMic = NO;
    }
    return self;
}

- (V2TXLivePlayer *)player {
    if (!_player) {
        _player = [[V2TXLivePlayer alloc] init];
        [_player setObserver:self];
    }
    return _player;
}
    
- (V2TXLivePusher *)pusher {
    if (!_pusher) {
        _pusher = [[V2TXLivePusher alloc] initWithLiveMode:V2TXLiveMode_RTC];
        [_pusher setObserver:self];
    }
    return _pusher;
}
@end
