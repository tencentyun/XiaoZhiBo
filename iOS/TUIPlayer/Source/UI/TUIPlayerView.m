//
//  TUIPlayerView.m
//  TUIPlayer
//
//  Created by gg on 2021/9/14.
//

#import "TUIPlayerView.h"
#import "Masonry.h"
#import "TUIPlayerPresenter.h"
#import "TUIPlayerHeader.h"
#import "PlayerLocalized.h"
#import "TUIPlayerContainerView.h"
#import "TUIPlayerLinkURLUtils.h"
#import "TUIConfig.h"

typedef NS_ENUM(NSUInteger, LinkMicBtnType) {
    LinkMicBtnTypeNormal,
    LinkMicBtnTypeCancel,
    LinkMicBtnTypeStop,
};

@interface TUIPlayerView () <TUIPlayerPresenterDelegate>

@property (nonatomic,  weak ) id <TUIPlayerViewDelegate> delegate;

@property (nonatomic, strong) TUIPlayerPresenter *presenter;

@property (nonatomic,  weak ) UIView *remoteView;
@property (nonatomic,  weak ) UIView *localView;

@property (nonatomic, strong) UIButton *requestLinkMicBtn;

@property (nonatomic,  weak ) TUIPlayerContainerView *containerView;

@property (nonatomic,  copy ) NSString *currentGroupId;
@property (nonatomic,  copy ) NSString *streamId;
@property (nonatomic,  copy ) NSString *playUrl;
@end

@implementation TUIPlayerView {
    UIImage *_requestJoinAnchorImage;
    UIImage *_cancelJoinAnchorImage;
    UIImage *_stopJoinAnchorImage;
    BOOL isViewReady;
}

#pragma mark - Interface
- (void)setDelegate:(id<TUIPlayerViewDelegate>)delegate {
    _delegate = delegate;
}

- (BOOL)startPlay:(NSString *)url {
    self.streamId = [TUIPlayerLinkURLUtils getStreamIdByPushUrl:url];
    LOGD("【Player】split stream id: %@", self.streamId);
    self.playUrl = url;
    BOOL res = [self.presenter startPlay:url atView:self.remoteView];
    if (res) {
        [UIApplication sharedApplication].idleTimerDisabled = YES;
    }
    if ([self.delegate respondsToSelector:@selector(onPlayStarted:url:)]) {
        [self.delegate onPlayStarted:self url:url];
    }
    return res;
}

- (void)stopPlay {
    [self.presenter sendStopLinkMic];
    [self.presenter stopPlay];
    [UIApplication sharedApplication].idleTimerDisabled = NO;
}

- (void)disableLinkMic {
    LOGD("【Player】disable linkmic");
    _requestLinkMicBtn = nil;
}

- (void)setGroupId:(NSString *)groupId {
    self.currentGroupId = groupId;
    
    LOGD("【Player】set group id: %@", groupId);
    
    if (isViewReady) {
        if (self.containerView.superview) {
            [self.containerView removeFromSuperview];
        }
        TUIPlayerContainerView *containerView = [[TUIPlayerContainerView alloc] initWithFrame:CGRectZero groupId:self.currentGroupId];
        [containerView setLinkMicBtn:self.requestLinkMicBtn];
        [self addSubview:containerView];
        self.containerView = containerView;
        [containerView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self);
        }];
    }
}

#pragma mark - TUIPlayerSignalingServiceDelegate
- (void)onReceiveLinkMicInvite:(NSString *)inviter cmd:(NSString *)cmd streamId:(NSString *)streamId {
    
}

- (void)onAcceptLinkMicInvite:(NSString *)cmd streamId:(NSString *)streamId {
    @weakify(self)
    [self.presenter startLinkMicWithUser:streamId atView:self.localView complete:^(BOOL success) {
        dispatch_async(dispatch_get_main_queue(), ^{
            @strongify(self)
            [self setRequestLinkMicBtnStatus:LinkMicBtnTypeStop];
        });
    }];
}
- (void)onRejectLinkMicInvite:(NSString *)cmd reason:(int)reason {
    [self setRequestLinkMicBtnStatus:LinkMicBtnTypeNormal];
    if ([self.delegate respondsToSelector:@selector(onRejectJoinAnchorResponse:reason:)]) {
        [self.delegate onRejectJoinAnchorResponse:self reason:reason];
    }
}

- (void)onStartLinkMic:(NSString *)cmd streamId:(NSString *)streamId {
    [self setRequestLinkMicBtnStatus:LinkMicBtnTypeStop];
}
- (void)onStopLinkMic:(NSString *)cmd {
    [self.presenter stopLinkMic];
    [self setRequestLinkMicBtnStatus:LinkMicBtnTypeNormal];
}
- (void)onLinkMicInviteTimeout {
    [self setRequestLinkMicBtnStatus:LinkMicBtnTypeNormal];
}

- (void)onRemoteStopPush {
    [self stopPlay];
    if ([self.delegate respondsToSelector:@selector(onPlayStoped:url:)]) {
        [self.delegate onPlayStoped:self url:self.playUrl];
    }
}

- (void)onSignalingError:(NSString *)cmd code:(int)code message:(NSString *)msg {
    LOGE("【Player】Signaling error: cmd:%@, code:%d, msg:%@", cmd, code, msg);
    if ([self.delegate respondsToSelector:@selector(onPlayEvent:event:message:)]) {
        [self.delegate onPlayEvent:self event:TUIPLAYER_EVENT_FAILED message:msg];
    }
}


#pragma mark - Private

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        isViewReady = NO;
        _requestJoinAnchorImage = [UIImage imageNamed:@"player_linkmic" inBundle:PlayerBundle() compatibleWithTraitCollection:nil];
        _cancelJoinAnchorImage = [UIImage imageNamed:@"player_cancelLinkmic" inBundle:PlayerBundle() compatibleWithTraitCollection:nil];
        _stopJoinAnchorImage = [UIImage imageNamed:@"player_cancelLinkmic" inBundle:PlayerBundle() compatibleWithTraitCollection:nil];
        [self setupUI];
        [[TUIConfig defaultConfig] setSceneOptimizParams:@"TUIPlayer"];
    }
    return self;
}

- (void)setupUI {
    UIView *remoteView = [[UIView alloc] initWithFrame:self.bounds];
    [self addSubview:remoteView];
    self.remoteView = remoteView;
    [remoteView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self);
    }];
    
    UIView *localView = [[UIView alloc] initWithFrame:CGRectZero];
    [self addSubview:localView];
    self.localView = localView;
    [localView mas_makeConstraints:^(MASConstraintMaker *make) {
        if (@available(iOS 11.0, *)) {
            make.top.equalTo(self.mas_safeAreaLayoutGuideTop).offset(10);
        } else {
            make.top.equalTo(self).offset(10);
        }
        make.trailing.equalTo(self).offset(-10);
        make.size.mas_equalTo(CGSizeMake(100, 150));
    }];
    
    self.requestLinkMicBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.requestLinkMicBtn addTarget:self action:@selector(linkMicBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    [self setRequestLinkMicBtnStatus:LinkMicBtnTypeNormal];
}

- (void)didMoveToWindow {
    [super didMoveToWindow];
    
    if (isViewReady) {
        return;
    }
    isViewReady = YES;
    
    TUIPlayerContainerView *containerView = [[TUIPlayerContainerView alloc] initWithFrame:CGRectZero groupId:self.currentGroupId];
    [containerView setLinkMicBtn:self.requestLinkMicBtn];
    [self addSubview:containerView];
    self.containerView = containerView;
    [containerView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self);
    }];
}

- (void)setRequestLinkMicBtnStatus:(LinkMicBtnType)type {
    switch (type) {
        case LinkMicBtnTypeNormal:
            [self.requestLinkMicBtn setImage:_requestJoinAnchorImage forState:UIControlStateNormal];
            self.requestLinkMicBtn.tag = 1;
            break;
        case LinkMicBtnTypeCancel:
            [self.requestLinkMicBtn setImage:_cancelJoinAnchorImage forState:UIControlStateNormal];
            self.requestLinkMicBtn.tag = 2;
            break;
        case LinkMicBtnTypeStop:
            [self.requestLinkMicBtn setImage:_stopJoinAnchorImage forState:UIControlStateNormal];
            self.requestLinkMicBtn.tag = 3;
            break;
        default:
            break;
    }
}

- (void)linkMicBtnClick:(UIButton *)btn {
    switch (btn.tag) {
        case 1: // request
            [self setRequestLinkMicBtnStatus:LinkMicBtnTypeCancel];
            [self.presenter sendLinkMicRequest:self.streamId];
            LOGD("【Player】send linkmic req");
            break;
        case 2: // cancel
            [self setRequestLinkMicBtnStatus:LinkMicBtnTypeNormal];
            [self.presenter cancelLinkMicRequest];
            LOGD("【Player】cancel linkmic req");
            break;
        case 3: // stop
            [self setRequestLinkMicBtnStatus:LinkMicBtnTypeNormal];
            [self.presenter sendStopLinkMic];
            LOGD("【Player】send stop linkmic req");
            break;
        default:
            break;
    }
}

- (TUIPlayerPresenter *)presenter {
    if (!_presenter) {
        _presenter = [[TUIPlayerPresenter alloc] init];
        _presenter.delegate = self;
    }
    return _presenter;
}

@end
