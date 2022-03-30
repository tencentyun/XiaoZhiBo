//
//  TCDownLoadManager.h
//  TUIBeauty
//
//  Created by origin 李 on 2022/3/14.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface TCDownLoadManager : NSObject
+ (id)sharedInstance;
+ (NSString *)xmagicRootPath;
- (void)downloadIfNeedUrl:(NSString *)aUrl type:(NSString * _Nullable)type itemName:(NSString * _Nullable)itemName progress:(void (^)(float))progress complete:(void (^)(BOOL, NSString * _Nonnull))complete;
@end

NS_ASSUME_NONNULL_END
