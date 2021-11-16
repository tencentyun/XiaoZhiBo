//
//  TUIPlayerView.h
//  TUIPlayer
//
//  Created by gg on 2021/9/14.
//

#import <UIKit/UIKit.h>
#import "TUIPlayerViewDelegate.h"

NS_ASSUME_NONNULL_BEGIN

@interface TUIPlayerView : UIView

/// 设置代理对象
- (void)setDelegate:(id <TUIPlayerViewDelegate>)delegate;

/// 开始拉流
/// @param url 流地址
- (BOOL)startPlay:(NSString *)url;

/// 停止拉流
- (void)stopPlay;

/// 加载挂件时可能会需要使用（TUIBarrage / TUIGift）
/// @param groupId 创建群组的 group id
- (void)setGroupId:(NSString *)groupId;

/// 关闭连麦功能
- (void)disableLinkMic;

@end

NS_ASSUME_NONNULL_END
