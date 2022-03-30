//
//  TCDownLoadManager.m
//  TUIBeauty
//
//  Created by origin 李 on 2022/3/14.
//

#import "TCDownLoadManager.h"
#import "SSZipArchive.h"
#import "UIView+TUIToast.h"

@interface TCDownLoadManager()<NSURLSessionDelegate>
@property (nonatomic, strong) NSString *type;
@property (nonatomic, strong) NSString *item;
@property (nonatomic, strong) NSURLSession *session;
@property (nonatomic, copy) void (^progressBlock) (float progress);
@property (nonatomic, copy) void (^completeBlock) (BOOL success, NSString *message);
@end

@implementation TCDownLoadManager
+ (id)sharedInstance {
    static dispatch_once_t once;
    static id sharedInstance;
    dispatch_once(&once, ^{
        sharedInstance = [[self alloc] init]; });
    return sharedInstance;
}

+ (NSString *)xmagicRootPath{
    NSString *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
    NSFileManager *manager = [NSFileManager defaultManager];
    path = [path stringByAppendingPathComponent:@"Xmagic"];
    [manager createDirectoryAtURL:[NSURL fileURLWithPath:path] withIntermediateDirectories:YES attributes:nil error:nil];
    return path;
}

- (NSURLSession *)session {
    if (!_session) {
        NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
        _session = [NSURLSession sessionWithConfiguration:config delegate:self delegateQueue:[NSOperationQueue mainQueue]];
    }
    return _session;
}

- (void)downloadIfNeedUrl:(NSString *)aUrl type:(NSString * _Nullable)type itemName:(NSString * _Nullable)itemName progress:(void (^)(float))progress complete:(void (^)(BOOL, NSString * _Nonnull))complete {
    self.type = type;
    self.item = itemName;
    NSURL *url = [NSURL URLWithString:aUrl];
    if (url == nil) {
        complete(NO, @"Url error");
        return;
    }
    self.progressBlock = progress;
    self.completeBlock = complete;
    if (progress != nil) {
        progress(0);
    }
    NSString *downloadPath;
    if (type.length>0 && itemName.length>0) {
        downloadPath = [NSString stringWithFormat:@"%@/%@/%@",[TCDownLoadManager xmagicRootPath],type,itemName];
        if ([[NSFileManager defaultManager] fileExistsAtPath:downloadPath]) {
            self.completeBlock(YES, @"");
            dispatch_async(dispatch_get_main_queue(), ^{
                [[self topViewController].view  hideToast];
            });
            return;
        }
    } else {
        downloadPath = [TCDownLoadManager xmagicRootPath];
        if ([[NSFileManager defaultManager] fileExistsAtPath:[downloadPath stringByAppendingString:@"/LightCore"] ]) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[self topViewController].view  hideToast];
            });
            self.completeBlock(YES, @"");
            return;
        }
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [[self topViewController].view makeToast:@"Downloading..." duration:100];
    });
    NSURLRequest *request = [NSURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringLocalCacheData timeoutInterval:30];
    NSURLSessionDownloadTask *task = [self.session downloadTaskWithRequest:request];
    [task resume];
}

- (NSString *)floderPathWithType:(NSString *)type itemName:(NSString *)item{
    self.type = type;
    self.item =item;
    NSString *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
    if (type == nil) {
        return path;
    }
    NSFileManager *manager = [NSFileManager defaultManager];
    path = [path stringByAppendingPathComponent:@"packages"];
    [manager createDirectoryAtURL:[NSURL fileURLWithPath:path] withIntermediateDirectories:YES attributes:nil error:nil];
    if (type.length > 0) {
        path = [path stringByAppendingPathComponent:type];
        [manager createDirectoryAtURL:[NSURL fileURLWithPath:path] withIntermediateDirectories:YES attributes:nil error:nil];
        if (item.length > 0) {
            path = [path stringByAppendingPathComponent:item];
            [manager createDirectoryAtURL:[NSURL fileURLWithPath:path] withIntermediateDirectories:YES attributes:nil error:nil];
        }
    }
    
    
    return path;
}

#pragma mark - NSURLSessionDelegate
- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didCompleteWithError:(nullable NSError *)error {
    if (error.code == 0) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self topViewController].view  hideToast];
        });
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self topViewController].view makeToast:[NSString stringWithFormat:@"Download Error: %ld",(long)error.code] duration:3];
        });
    }
    
    if (error != nil) {
        if (error.code == -999) {
            return;
        }
        if (self.completeBlock != nil) {
            self.completeBlock(NO, error.localizedDescription);
        }
        [self flushRefrence];
    }
}

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask didWriteData:(int64_t)bytesWritten totalBytesWritten:(int64_t)totalBytesWritten totalBytesExpectedToWrite:(int64_t)totalBytesExpectedToWrite {
    if (self.progressBlock == nil) {
        return;
    }
    float prog = totalBytesWritten * 1.0 / totalBytesExpectedToWrite;
    self.progressBlock(prog);
}

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask didFinishDownloadingToURL:(NSURL *)location {
    NSString *path = [TCDownLoadManager xmagicRootPath];
    if (self.type.length>0) {
        path =  [[TCDownLoadManager xmagicRootPath] stringByAppendingString:[NSString stringWithFormat:@"/%@",self.type]];
    }
    BOOL res = [SSZipArchive unzipFileAtPath:location.path toDestination:path];
    if (res) {
        if (self.completeBlock != nil) {
            self.completeBlock(YES, @"");
        }
    } else {
        if (self.completeBlock != nil) {
            self.completeBlock(NO, @"Unzip failed");
        }
    }
    [self flushRefrence];
    dispatch_async(dispatch_get_main_queue(), ^{
        [[self topViewController].view  hideToast];
    });
}

- (void)flushRefrence {
    self.completeBlock = nil;
    self.progressBlock = nil;
}

- (UIViewController *)topViewController {
    UIViewController *resultVC;
    resultVC = [self _topViewController:[[UIApplication sharedApplication].keyWindow rootViewController]];
    while (resultVC.presentedViewController) {
        resultVC = [self _topViewController:resultVC.presentedViewController];
    }
    return resultVC;
}

- (UIViewController *)_topViewController:(UIViewController *)vc {
    if ([vc isKindOfClass:[UINavigationController class]]) {
        return [self _topViewController:[(UINavigationController *)vc topViewController]];
    } else if ([vc isKindOfClass:[UITabBarController class]]) {
        return [self _topViewController:[(UITabBarController *)vc selectedViewController]];
    } else {
        return vc;
    }
}
@end
