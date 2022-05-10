//
//  PlayerLocalized.m
//  Pods
//
//  Created by gg on 2021/9/14.
//

#import "PlayerLocalized.h"

#pragma mark - Base

NSBundle *TUIPlayerBundle(void) {
    NSURL *playerKitBundleURL = [[NSBundle mainBundle] URLForResource:@"TUIPlayerKitBundle" withExtension:@"bundle"];
    return [NSBundle bundleWithURL:playerKitBundleURL];
}

NSString *TUIPlayerLocalizeFromTable(NSString *key, NSString *table) {
    return [TUIPlayerBundle() localizedStringForKey:key value:@"" table:table];
}

NSString *TUIPlayerLocalizeFromTableAndCommon(NSString *key, NSString *common, NSString *table) {
    return TUIPlayerLocalizeFromTable(key, table);
}

#pragma mark - Replace String

NSString *TUIPlayerLocalizeReplaceXX(NSString *origin, NSString *xxx_replace) {
    if (xxx_replace == nil) { xxx_replace = @"";}
    return [origin stringByReplacingOccurrencesOfString:@"xxx" withString:xxx_replace];
}

NSString *TUIPlayerLocalizeReplace(NSString *origin, NSString *xxx_replace, NSString *yyy_replace) {
    if (yyy_replace == nil) { yyy_replace = @"";}
    return [TUIPlayerLocalizeReplaceXX(origin, xxx_replace) stringByReplacingOccurrencesOfString:@"yyy" withString:yyy_replace];
}

#pragma mark - Player

NSString *const TUIPlayer_Localize_TableName = @"PlayerLocalized";
NSString *TUIPlayerLocalize(NSString *key) {
    return TUIPlayerLocalizeFromTable(key, TUIPlayer_Localize_TableName);
}
