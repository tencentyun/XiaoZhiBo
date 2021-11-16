//
//  TUIPlayerStreamService.h
//  TUIPlayer
//
//  Created by gg on 2021/9/14.
//

#import <Foundation/Foundation.h>
#import "TUIPlayerStreamServiceDelegate.h"

NS_ASSUME_NONNULL_BEGIN

@interface TUIPlayerStreamService : NSObject

@property (nonatomic, weak) id <TUIPlayerStreamServiceDelegate> delegate;

- (BOOL)startPlay:(NSString *)url atView:(UIView *)view;

- (void)stopPlay;

- (BOOL)startLinkMic:(NSString *)streamId view:(UIView *)view complete:(void (^) (BOOL success))complete;

- (void)stopLinkMic;

@end

NS_ASSUME_NONNULL_END
