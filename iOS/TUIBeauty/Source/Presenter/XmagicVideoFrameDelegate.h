//
//  XmagicVideoFrameDelegate.h
//  TUIBeauty
//
//  Created by origin 李 on 2022/2/28.
//

#import <Foundation/Foundation.h>
#import "XMagic.h"
#import "XmagicVideoFrameDelegate.h"
#import "XmagicBeautyManager.h"
NS_ASSUME_NONNULL_BEGIN

/**
 * @brief 视频帧的像素格式。
 */
typedef NS_ENUM(NSInteger, V2TXLivePixelFormat) {

    /// 未知
    V2TXLivePixelFormatUnknown,

    /// YUV420P I420
    V2TXLivePixelFormatI420,

    /// YUV420SP NV12
    V2TXLivePixelFormatNV12,

    /// BGRA8888
    V2TXLivePixelFormatBGRA32,

    /// OpenGL 2D 纹理
    V2TXLivePixelFormatTexture2D

};


/**
 * @brief 视频画面顺时针旋转角度。
 */
typedef NS_ENUM(NSInteger, V2TXLiveRotation) {

    /// 不旋转
    V2TXLiveRotation0,

    /// 顺时针旋转90度
    V2TXLiveRotation90,

    /// 顺时针旋转180度
    V2TXLiveRotation180,

    /// 顺时针旋转270度
    V2TXLiveRotation270

};


typedef NS_ENUM(NSInteger, V2TXLiveBufferType) {

    /// 未知
    V2TXLiveBufferTypeUnknown,

    /// 直接使用效率最高，iOS 系统提供了众多 API 获取或处理 PixelBuffer
    V2TXLiveBufferTypePixelBuffer,

    /// 会有一定的性能消耗，SDK 内部是直接处理 PixelBuffer 的，所以会存在 NSData 和 PixelBuffer 之间类型转换所产生的内存拷贝开销
    V2TXLiveBufferTypeNSData,

    /// 直接操作纹理 ID，性能最好
    V2TXLiveBufferTypeTexture

};

@interface XmagicVideoFrameDelegate : NSObject

@property(nonatomic, strong) XMagic *xMagicKit;

- (instancetype)initWithlicenseUrl:(NSString *)licenseUrl licenseKey:(NSString *)licenseKey;

@end

@interface V2TXLiveVideoFrame : NSObject

/// 【字段含义】视频帧像素格式
/// 【推荐取值】V2TXLivePixelFormatNV12
@property(nonatomic, assign) V2TXLivePixelFormat pixelFormat;

/// 【字段含义】视频数据包装格式
/// 【推荐取值】V2TXLiveBufferTypePixelBuffer
@property(nonatomic, assign) V2TXLiveBufferType bufferType;

/// 【字段含义】bufferType 为 V2TXLiveBufferTypeNSData 时的视频数据
@property(nonatomic, strong, nullable) NSData *data;

/// 【字段含义】bufferType 为 V2TXLiveBufferTypePixelBuffer 时的视频数据
@property(nonatomic, assign, nullable) CVPixelBufferRef pixelBuffer;

/// 【字段含义】视频宽度
@property(nonatomic, assign) NSUInteger width;

/// 【字段含义】视频高度
@property(nonatomic, assign) NSUInteger height;

/// 【字段含义】视频帧的顺时针旋转角度
@property(nonatomic, assign) V2TXLiveRotation rotation;

/// 【字段含义】视频纹理ID
@property(nonatomic, assign) GLuint textureId;

@end


NS_ASSUME_NONNULL_END
