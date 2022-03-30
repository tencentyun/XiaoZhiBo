//
//  TUIBeautyExtension.m
//  TUIBeauty
//
//  Created by gg on 2021/9/28.
//

#import "TUIBeautyExtension.h"
#import "TUICore.h"
#import "TUIDefine.h"
#import "TUIBeautyView.h"
#import "TUIBeautyExtensionView.h"
#import "NSDictionary+TUISafe.h"

@interface TUIBeautyExtension () <TUIExtensionProtocol>

@end

@implementation TUIBeautyExtension

/// 注册美颜组件
+ (void)load {
    [TUICore registerExtension:TUICore_TUIBeautyExtension_Extension object:[TUIBeautyExtension sharedInstance]];
    [TUICore registerExtension:TUICore_TUIBeautyExtension_BeautyView object:[TUIBeautyExtension sharedInstance]];
}

+ (instancetype)sharedInstance {
    static TUIBeautyExtension *service = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        service = [[self alloc] init];
    });
    return service;
}

-(void )onGetExtensionInfo:(NSDictionary *)param {
      
   
}

#pragma mark - TUIExtensionProtocol
- (NSDictionary *)getExtensionInfo:(NSString *)key param:(NSDictionary *)param {
    if (!key || ![param isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    if ([key isEqualToString:TUICore_TUIBeautyExtension_BeautyView]) {
        id beautyManager = [param tui_objectForKey:TUICore_TUIBeautyExtension_BeautyView_BeautyManager asClass:[NSObject class]];
        if (!beautyManager || ![beautyManager isKindOfClass:NSClassFromString(@"TXBeautyManager")]) {
            return nil;
        }
        NSString *licenseUrl = [param tui_objectForKey:TUICore_TUIBeautyExtension_BeautyView_LicenseUrl asClass:[NSString class]];
        NSString *licenseKey = [param tui_objectForKey:TUICore_TUIBeautyExtension_BeautyView_LicenseKey asClass:[NSString class]];
        TUIBeautyView *beautyView = [[TUIBeautyView alloc] initWithFrame:CGRectZero beautyManager:beautyManager licenseUrl:licenseUrl licenseKey:licenseKey];
        id videoFrameDelegate = [beautyView getBeautyService];
        return @{TUICore_TUIBeautyExtension_BeautyView_View : beautyView,TUICore_TUIBeautyExtension_BeautyView_DataProcessDelegate :videoFrameDelegate};
    } else if ([key isEqualToString:TUICore_TUIBeautyExtension_Extension]) {
        return @{TUICore_TUIBeautyExtension_Extension_View : [TUIBeautyExtensionView getExtensionView]};
    }
    return nil;
}
@end
