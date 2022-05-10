//
//  PlayerLocalized.h
//  Pods
//
//  Created by gg on 2021/9/14.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

#pragma mark - Base

extern NSBundle *TUIPlayerBundle(void);

#pragma mark - Replace String

extern NSString *TUIPlayerLocalizeReplaceXX(NSString *origin, NSString *xxx_replace);
extern NSString *TUIPlayerLocalizeReplace(NSString *origin, NSString *xxx_replace, NSString *yyy_replace);

#pragma mark - Player

extern NSString *const TUIPlayer_Localize_TableName;
extern NSString *TUIPlayerLocalize(NSString *key);

NS_ASSUME_NONNULL_END
