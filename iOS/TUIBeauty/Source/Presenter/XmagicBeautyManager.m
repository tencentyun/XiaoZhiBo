//
//  XmagicBeautyManager.m
//  TUIBeauty
//
//  Created by origin 李 on 2022/2/28.
//

#import "XmagicBeautyManager.h"
#import "TESign.h"
#import "yt_auth_apple.h"
#import "BeautyLocalized.h"
#import "TCDownLoadManager.h"

@interface XmagicBeautyManager()
///证书监听
@property(nonatomic, strong) NSString *licenseKey;
@property(nonatomic, strong) NSString *licenseUrl;
@property(nonatomic, strong) XmagicVideoFrameDelegate *videoFrameDelegate;

@end

@implementation XmagicBeautyManager

+ (instancetype)sharedInstance {
    static id _sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedInstance = [[self alloc] init];
    });
    return _sharedInstance;
}

- (instancetype)initXmagicSDKWith:(NSString *)licenseKey licenseUrl:(NSString *)licenseUrl{
    self = [super init];
    if (self) {
        _licenseKey = licenseKey;
        _licenseUrl = licenseUrl;
    }
    return self;
}

- (void)configPropertyWith:(TCBeautyBaseItem *)beautyBaseItem{
    switch (beautyBaseItem.type) {
        case TCBeautyTypeBeauty:
            [self configPropertyWithType:@"beauty" withName:beautyBaseItem.key withData:[NSString stringWithFormat:@"%f",beautyBaseItem.currentValue] withExtraInfo:beautyBaseItem.extraConfig];
            break;
        case TCBeautyTypeFilter:
            [self configPropertyWithType:@"lut" withName:[@"lut.bundle/" stringByAppendingPathComponent:beautyBaseItem.path] withData:[NSString stringWithFormat:@"%f",beautyBaseItem.currentValue] withExtraInfo:nil];
            break;
        case TCBeautyTypeMotion:
            [self dowloadItem:[beautyBaseItem key]];
            [self configPropertyWithType:@"motion" withName:[beautyBaseItem key] withData:[[TCDownLoadManager xmagicRootPath]stringByAppendingString:@"/motions"] withExtraInfo:beautyBaseItem.extraConfig];
            [self configPropertyWithType:@"custom" withName:@"makeup.strength" withData:[NSString stringWithFormat:@"%.2f", [beautyBaseItem currentValue]] withExtraInfo:nil];
            break;
        case TCBeautyTypeKoubei:
        case TCBeautyTypeCosmetic:
        case TCBeautyTypeGesture:
        case TCBeautyTypeGreen:
            [self dowloadItem:[beautyBaseItem key]];
            [self configPropertyWithType:@"motion" withName:[beautyBaseItem key] withData:[[TCDownLoadManager xmagicRootPath]stringByAppendingString:@"/motions"] withExtraInfo:beautyBaseItem.extraConfig];
            break;
        default:
            break;
    }
}

- (void)sliderValueChange:(TCBeautyBaseItem *)beautyBaseItem {
    [self configPropertyWith:beautyBaseItem];
}

- (int)configPropertyWithType:(NSString *_Nonnull)propertyType withName:(NSString *_Nonnull)propertyName withData:(NSString*_Nonnull)propertyValue withExtraInfo:(id _Nullable)extraInfo {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.xMagicKit configPropertyWithType:propertyType withName:propertyName withData:propertyValue withExtraInfo:extraInfo];
    });
    return 0;
}

- (XMagic *)xMagicKit{
    return self.videoFrameDelegate.xMagicKit;
}

- (id)videoFrameDelegate{
    if (!_videoFrameDelegate) {
        _videoFrameDelegate = [[XmagicVideoFrameDelegate alloc]initWithlicenseUrl:_licenseUrl licenseKey:_licenseKey];
    }
    return _videoFrameDelegate;
}

- (void)dowloadItem:(NSString *)key{
    if ([key isEqualToString:@"naught"]) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"https://liteav.sdk.qcloud.com/app/res/xmagic/resource/%@.zip",key];
    [[TCDownLoadManager sharedInstance]downloadIfNeedUrl:url type:@"motions" itemName:key progress:^(float progress) {
        
    } complete:^(BOOL success, NSString *msg) {
        
    }];
}

- (void)cleanXmagic {
    [self.videoFrameDelegate.xMagicKit deinit];
    self.videoFrameDelegate.xMagicKit = nil;
}


@end
