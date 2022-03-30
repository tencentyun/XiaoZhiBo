//
//  XmagicVideoFrameDelegate.m
//  TUIBeauty
//
//  Created by origin 李 on 2022/2/28.
//

#import "XmagicVideoFrameDelegate.h"
#import <OpenGLES/EAGL.h>
#import <OpenGLES/ES2/gl.h>
#import <OpenGLES/ES2/glext.h>
#import "TESign.h"
#import "yt_auth_apple.h"
#import "BeautyLocalized.h"
#import "TCDownLoadManager.h"
#import "TUIBeautyHeader.h"
#import "TELicenseCheck.h"

@interface XmagicVideoFrameDelegate()<YTSDKEventListener, YTSDKLogListener>
{
    EAGLContext* xmagicContext;
    EAGLContext* cloudContext;
    
    GLuint cloudGlFrameBuffer;
    CVPixelBufferRef cloudPixelBuffer;
    
    CVOpenGLESTextureRef cloudTextureRef;
    CVOpenGLESTextureCacheRef cloudTextureCache;
    
    
    CVOpenGLESTextureRef bgraTextureRef;
    CVOpenGLESTextureCacheRef textureCache;
    EAGLContext* eaglContext;
    GLuint glFrameBuffer;
    CVPixelBufferRef pixelBufferOuput;
}
@property (nonatomic, assign) float heightF;
@end

@implementation XmagicVideoFrameDelegate

- (instancetype)initWithlicenseUrl:(NSString *)licenseUrl licenseKey:(NSString *)licenseKey {
    self = [super init];
    if (self) {
        [TELicenseCheck  setTELicense:licenseUrl key:licenseKey completion:^(NSInteger authresult, NSString * errorMsg) {
            if (authresult == 0) {
                NSLog(@"success");
            } else {
                NSLog(@"failure");
            }
        }];
    }
    return self;
}

- (void)dealloc {
    if (self.xMagicKit) {
        [self.xMagicKit deinit];
        self.xMagicKit = nil;
    }
}

#pragma mark - V2TXLivePusherObserver
- (void)onProcessVideoFrame:(V2TXLiveVideoFrame *)srcFrame dstFrame:(V2TXLiveVideoFrame *)dstFrame {
    if(!_xMagicKit){
        [self buildBeautySDK:srcFrame.width and:srcFrame.height texture:srcFrame.textureId];
        self.heightF = srcFrame.height;
    }
    if(_xMagicKit != nil && self.heightF != srcFrame.height){
        [_xMagicKit setRenderSize:CGSizeMake(srcFrame.width, srcFrame.height)];
    }
    YTProcessInput *input = [[YTProcessInput alloc] init];
    input.textureData = [[YTTextureData alloc] init];
    input.textureData.texture = srcFrame.textureId;
    input.textureData.textureWidth = srcFrame.width;
    input.textureData.textureHeight = srcFrame.height;
    input.dataType = kYTTextureData;
    YTProcessOutput *output =[self.xMagicKit process:input withOrigin:YtLightImageOriginTopLeft withOrientation:YtLightCameraRotation0];
    dstFrame.textureId = output.textureData.texture;
}

//HB构建SDK，初始化接口
- (void)buildBeautySDK:(NSInteger)width and:(NSInteger)height texture:(unsigned)textureID {
    NSString *beautyConfigPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    beautyConfigPath = [beautyConfigPath stringByAppendingPathComponent:@"beauty_config.json"];
    NSFileManager *localFileManager=[[NSFileManager alloc] init];
    BOOL isDir = YES;
    NSDictionary * beautyConfigJson = @{};
    if ([localFileManager fileExistsAtPath:beautyConfigPath isDirectory:&isDir] && !isDir) {
        NSString *beautyConfigJsonStr = [NSString stringWithContentsOfFile:beautyConfigPath encoding:NSUTF8StringEncoding error:nil];
        NSError *jsonError;
        NSData *objectData = [beautyConfigJsonStr dataUsingEncoding:NSUTF8StringEncoding];
        beautyConfigJson = [NSJSONSerialization JSONObjectWithData:objectData options:NSJSONReadingMutableContainers error:&jsonError];
    }
    
    NSDictionary *assetsDict = @{@"core_name":@"LightCore.bundle",
                                 @"root_path":[BeautyBundle() resourcePath],
                                 @"plugin_3d":@"Light3DPlugin.bundle",
                                 @"plugin_hand":@"LightHandPlugin.bundle",
                                 @"plugin_segment":@"LightSegmentPlugin.bundle",
                                 @"beauty_config":beautyConfigJson
    };
    self.xMagicKit = [[XMagic alloc] initWithRenderSize:CGSizeMake(width, height) assetsDict:assetsDict];
    [self.xMagicKit registerSDKEventListener:self];
    [self.xMagicKit registerLoggerListener:self withDefaultLevel:YT_SDK_VERBOSE_LEVEL];
}

- (void)onYTDataEvent:(id _Nonnull)event{
    
}

- (void)onAIEvent:(id _Nonnull)event{
    
}

- (void)onTipsEvent:(id _Nonnull)event{
    
}

- (void)onAssetEvent:(id _Nonnull)event{
    
}
#pragma mark - YTSDKLogListener
- (void)onLog:(YtSDKLoggerLevel) loggerLevel withInfo:(NSString * _Nonnull) logInfo{
    
}



@end
