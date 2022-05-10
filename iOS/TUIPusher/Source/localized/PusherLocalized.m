//
//  PusherLocalized.m
//  Pods
//
//  Created by gg on 2021/9/7.
//

#import "PusherLocalized.h"

#pragma mark - Base

NSBundle *TUIPusherBundle(void) {
    NSURL *callingKitBundleURL = [[NSBundle mainBundle] URLForResource:@"TUIPusherKitBundle" withExtension:@"bundle"];
    return [NSBundle bundleWithURL:callingKitBundleURL];
}

NSString *TUIPusherLocalizeFromTable(NSString *key, NSString *table) {
    return [TUIPusherBundle() localizedStringForKey:key value:@"" table:table];
}

NSString *TUIPusherLocalizeFromTableAndCommon(NSString *key, NSString *common, NSString *table) {
    return TUIPusherLocalizeFromTable(key, table);
}

#pragma mark - Replace String

NSString *TUIPusherLocalizeReplaceXX(NSString *origin, NSString *xxx_replace) {
    if (xxx_replace == nil) { xxx_replace = @"";}
    return [origin stringByReplacingOccurrencesOfString:@"xxx" withString:xxx_replace];
}

NSString *TUIPusherLocalizeReplace(NSString *origin, NSString *xxx_replace, NSString *yyy_replace) {
    if (yyy_replace == nil) { yyy_replace = @"";}
    return [TUIPusherLocalizeReplaceXX(origin, xxx_replace) stringByReplacingOccurrencesOfString:@"yyy" withString:yyy_replace];
}

#pragma mark - Pusher

NSString *const TUIPusher_Localize_TableName = @"PusherLocalized";
NSString *TUIPusherLocalize(NSString *key) {
    return TUIPusherLocalizeFromTable(key, TUIPusher_Localize_TableName);
}
