//
//  XmagicBeautyManager.h
//  TUIBeauty
//
//  Created by origin 李 on 2022/2/28.
//

#import <Foundation/Foundation.h>
#import "XMagic.h"
#import "XmagicVideoFrameDelegate.h"
#import "TCBeautyModel.h"
NS_ASSUME_NONNULL_BEGIN

@interface XmagicBeautyManager : NSObject
///初始化美颜SDK
- (instancetype)initXmagicSDKWith:(NSString *)licenseKey licenseUrl:(NSString *)licenseUrl;
///获取视频处理对象
- (id)videoFrameDelegate;
///设置美颜参数
- (void)configPropertyWith:(TCBeautyBaseItem *)beautyBaseItem;
///美颜强度改变
- (void)sliderValueChange:(TCBeautyBaseItem *)beautyBaseItem;
/// 清理美颜资源
- (void)cleanXmagic;
@end

NS_ASSUME_NONNULL_END
