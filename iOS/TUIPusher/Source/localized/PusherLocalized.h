//
//  PusherLocalized.h
//  Pods
//
//  Created by gg on 2021/9/7.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

#pragma mark - Base

extern NSBundle *TUIPusherBundle(void);

#pragma mark - Replace String

extern NSString *TUIPusherLocalizeReplaceXX(NSString *origin, NSString *xxx_replace);
extern NSString *TUIPusherLocalizeReplace(NSString *origin, NSString *xxx_replace, NSString *yyy_replace);

#pragma mark - TRTC

extern NSString *const TUIPusher_Localize_TableName;
extern NSString *TUIPusherLocalize(NSString *key);

NS_ASSUME_NONNULL_END
