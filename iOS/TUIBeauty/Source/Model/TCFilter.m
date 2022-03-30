// Copyright (c) 2019 Tencent. All rights reserved.

#import "TCFilter.h"
#import "BeautyLocalized.h"

TCFilterIdentifier const TCFilterIdentifierNone      = @"";
TCFilterIdentifier const TCFilterIdentifierBaiXi     = @"baixi";
TCFilterIdentifier const TCFilterIdentifierNormal    = @"normal";
TCFilterIdentifier const TCFilterIdentifierZiRan     = @"ziran";
TCFilterIdentifier const TCFilterIdentifierYinghong  = @"yinghong";
TCFilterIdentifier const TCFilterIdentifierYunshang  = @"yunshang";
TCFilterIdentifier const TCFilterIdentifierChunzhen  = @"chunzhen";
TCFilterIdentifier const TCFilterIdentifierBailan    = @"bailan";
TCFilterIdentifier const TCFilterIdentifierYuanqi    = @"yuanqi";
TCFilterIdentifier const TCFilterIdentifierChaotuo   = @"chaotuo";
TCFilterIdentifier const TCFilterIdentifierXiangfen  = @"xiangfen";
TCFilterIdentifier const TCFilterIdentifierWhite     = @"white";
TCFilterIdentifier const TCFilterIdentifierLangman   = @"langman";
TCFilterIdentifier const TCFilterIdentifierQingxin   = @"qingxin";
TCFilterIdentifier const TCFilterIdentifierWeimei    = @"weimei";
TCFilterIdentifier const TCFilterIdentifierFennen    = @"fennen";
TCFilterIdentifier const TCFilterIdentifierHuaijiu   = @"huaijiu";
TCFilterIdentifier const TCFilterIdentifierLandiao   = @"landiao";
TCFilterIdentifier const TCFilterIdentifierQingliang = @"qingliang";
TCFilterIdentifier const TCFilterIdentifierRixi      = @"rixi";

@implementation TCFilter

- (instancetype)initWithIdentifier:(TCFilterIdentifier)identifier
                   lookupImagePath:(NSString *)lookupImagePath
{
    if (self = [super init]) {
        _identifier = identifier;
        _lookupImagePath = lookupImagePath;
    }
    return self;
}
@end

@implementation TCFilterManager
{
    NSDictionary<TCFilterIdentifier, TCFilter*> *_filterDictionary;
}

+ (instancetype)defaultManager
{
    static TCFilterManager *defaultManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        defaultManager = [[TCFilterManager alloc] init];
    });
    return defaultManager;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
//        _xMagicfilterItems = [self getFilterItems];
        NSBundle *bundle = BeautyBundle();
        NSString *path = [bundle pathForResource:@"FilterResource" ofType:@"bundle"];
        NSFileManager *manager = [[NSFileManager alloc] init];
        if ([manager fileExistsAtPath:path]) {
            NSArray<TCFilterIdentifier> *availableFilters = @[
                TCFilterIdentifierBaiXi,
                TCFilterIdentifierNormal,
                TCFilterIdentifierZiRan,
                TCFilterIdentifierYinghong,
                TCFilterIdentifierYunshang,
                TCFilterIdentifierChunzhen,
                TCFilterIdentifierBailan,
                TCFilterIdentifierYuanqi,
                TCFilterIdentifierChaotuo,
                TCFilterIdentifierXiangfen,
                TCFilterIdentifierWhite,
                TCFilterIdentifierLangman,
                TCFilterIdentifierQingxin,
                TCFilterIdentifierWeimei,
                TCFilterIdentifierFennen,
                TCFilterIdentifierHuaijiu,
                TCFilterIdentifierLandiao,
                TCFilterIdentifierQingliang,
                TCFilterIdentifierRixi];
            NSMutableArray<TCFilter *> *filters = [[NSMutableArray alloc] initWithCapacity:availableFilters.count];
            NSMutableDictionary<TCFilterIdentifier, TCFilter*> *filterMap = [[NSMutableDictionary alloc] initWithCapacity:availableFilters.count];
            for (TCFilterIdentifier identifier in availableFilters) {
                NSString * itemPath = [path stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.png", identifier]];
                if ([manager fileExistsAtPath:path]) {
                    TCFilter *filter = [[TCFilter alloc] initWithIdentifier:identifier lookupImagePath:itemPath];
                    [filters addObject:filter];
                    filterMap[identifier] = filter;
                }
            }
            _allFilters = filters;

        }
    }
    return self;
}

- (TCFilter *)filterWithIdentifier:(TCFilterIdentifier)identifier;
{
    return _filterDictionary[identifier];
}

- (NSMutableArray *)getFilterItems {
    NSMutableArray *filterItems = [[NSMutableArray alloc]init];
    NSString *filterpath = [BeautyBundle() pathForResource:@"TCFilter" ofType:@"json"];
    if (!filterpath) {
        return nil;
    }
    NSData *data = [NSData dataWithContentsOfFile:filterpath];
    if (!data) {
        return nil;
    }
    NSError *error;
    NSDictionary *root = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
    if (error || ![root isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    
    if (![root.allKeys containsObject:@"package"]) {
        return nil;
    }
    
    NSArray *arr = root[@"items"];
    if (![arr isKindOfClass:[NSArray class]]) {
        return nil;
    }
    
    for (NSDictionary *item in arr) {
        NSString *bundleStr = [item objectForKey:@"bundle"];
        NSBundle *bundle = BeautyBundle();
        NSString *iconPath = [bundle pathForResource:bundleStr ofType:@"bundle"];
        if (iconPath == nil) {
            continue;
        }
       NSArray *lutIDS = [item objectForKey:@"lutIDS"];
        for (NSDictionary *lutID in lutIDS) {
            NSString *path = [lutID objectForKey:@"path"];
            NSString *key = [lutID objectForKey:@"key"];
            NSNumber *strength = [lutID objectForKey:@"strength"];

            NSString *itemPath = [iconPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@",path]];
            NSFileManager *manager = [[NSFileManager alloc] init];
            if ([manager fileExistsAtPath:itemPath]) {
                TCFilter *filter = [[TCFilter alloc] initWithIdentifier:key lookupImagePath:itemPath];
                filter.title = [lutID objectForKey:@"title"];
                filter.isXmagic = YES;
                filter.path = path;
                filter.strength = strength;
                [filterItems addObject:filter];
            }
        }
    }
    return filterItems;
}
@end
