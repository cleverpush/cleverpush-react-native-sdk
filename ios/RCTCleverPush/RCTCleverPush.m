#if __has_include(<React/RCTConvert.h>)
#import <React/RCTConvert.h>
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>
#else
#import "RCTConvert.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"
#endif

#import "RCTCleverPush.h"
#import "RCTCleverPushEventEmitter.h"

#import <objc/runtime.h>

#if __IPHONE_CP_VERSION_MIN_REQUIRED < __IPHONE_8_0

#define UIUserNotificationTypeAlert UIRemoteNotificationTypeAlert
#define UIUserNotificationTypeBadge UIRemoteNotificationTypeBadge
#define UIUserNotificationTypeSound UIRemoteNotificationTypeSound
#define UIUserNotificationTypeNone  UIRemoteNotificationTypeNone
#define UIUserNotificationType      UIRemoteNotificationType

#endif

@interface RCTCleverPush ()
@end

@implementation RCTCleverPush {
    BOOL didInitialize;
}

CPNotificationOpenedResult* coldStartCPNotificationOpenedResult;

+ (RCTCleverPush *) sharedInstance {
    static dispatch_once_t token = 0;
    static id _sharedInstance = nil;
    dispatch_once(&token, ^{
        _sharedInstance = [[RCTCleverPush alloc] init];
    });
    return _sharedInstance;
}

- (NSDictionary *) dictionaryWithPropertiesOfObject:(id)obj {
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    
    unsigned count;
    objc_property_t *properties = class_copyPropertyList([obj class], &count);
    
    for (int i = 0; i < count; i++) {
        NSString *key = [NSString stringWithUTF8String:property_getName(properties[i])];
        if ([obj valueForKey:key] != nil) {
            if ([[obj valueForKey:key] isKindOfClass:[NSDate class]]) {
                NSString *convertedDateString = [NSString stringWithFormat:@"%@", [obj valueForKey:key]];
                [dict setObject:convertedDateString forKey:key];
            } else {
                [dict setObject:[obj valueForKey:key] forKey:key];
            }
        }
    }
    free(properties);
    return [NSDictionary dictionaryWithDictionary:dict];
}

- (NSString*)stringifyNotificationOpenedResult:(CPNotificationOpenedResult*)result {
    NSDictionary *notificationDictionary = [self dictionaryWithPropertiesOfObject:result.notification];
    NSDictionary *subscriptionDictionary = [self dictionaryWithPropertiesOfObject:result.subscription];

    NSMutableDictionary* obj = [NSMutableDictionary new];
    [obj setObject:notificationDictionary forKeyedSubscript:@"notification"];
    [obj setObject:subscriptionDictionary forKeyedSubscript:@"subscription"];

    NSError * err;
    NSData * jsonData = [NSJSONSerialization  dataWithJSONObject:obj options:0 error:&err];
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

- (NSString*)stringifyNotificationReceivedResult:(CPNotificationReceivedResult*)result {
    NSDictionary *notificationDictionary = [self dictionaryWithPropertiesOfObject:result.notification];
    NSDictionary *subscriptionDictionary = [self dictionaryWithPropertiesOfObject:result.subscription];

    NSMutableDictionary* obj = [NSMutableDictionary new];
    [obj setObject:notificationDictionary forKeyedSubscript:@"notification"];
    [obj setObject:subscriptionDictionary forKeyedSubscript:@"subscription"];

    NSError * err;
    NSData * jsonData = [NSJSONSerialization  dataWithJSONObject:obj options:0 error:&err];
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

- (void)initCleverPush {
    NSLog(@"CleverPush: initCleverPush called");

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didBeginObserving) name:@"didSetBridge" object:nil];
    
    [CleverPush setAutoClearBadge:NO];

    didInitialize = false;
}

- (void)didBeginObserving {
    RCTCleverPush.sharedInstance.didStartObserving = true;

    dispatch_async(dispatch_get_main_queue(), ^{
        if (coldStartCPNotificationOpenedResult) {
            [self handleNotificationOpened:[self stringifyNotificationOpenedResult:coldStartCPNotificationOpenedResult]];
            coldStartCPNotificationOpenedResult = nil;
        }
    });
}

- (void)init:(NSDictionary *)options {
    if (didInitialize) {
        return;
    }

    NSLog(@"CleverPush: init with channelId called");

    BOOL autoRegister = YES;
    NSString *channelId = [options objectForKey:@"channelId"];
    if ([[options objectForKey:@"autoRegister"] isKindOfClass:[NSNumber class]]) {
        autoRegister = [[options objectForKey:@"autoRegister"] boolValue];
    }

    didInitialize = true;
    
    [CleverPush initWithLaunchOptions:nil channelId:channelId handleNotificationReceived:^(CPNotificationReceivedResult *result) {
        NSLog(@"CleverPush: init: handleNotificationReceived");

        if (RCTCleverPush.sharedInstance.didStartObserving) {
            [self handleNotificationReceived:[self stringifyNotificationReceivedResult:result]];
        }
    } handleNotificationOpened:^(CPNotificationOpenedResult *result) {
      NSLog(@"CleverPush: init: handleNotificationOpened");

      if (!RCTCleverPush.sharedInstance.didStartObserving) {
          coldStartCPNotificationOpenedResult = result;
      } else {
          [self handleNotificationOpened:[self stringifyNotificationOpenedResult:result]];
      }
    } handleSubscribed:^(NSString *result) {
        NSLog(@"CleverPush: init: handleSubscribed");
        [self handleSubscribed:result];
    } autoRegister:autoRegister];

    [CleverPush setAppBannerOpenedCallback:^(CPAppBannerAction *action) {
        NSMutableDictionary *result = [NSMutableDictionary new];
        if (action.type != nil) {
            [result setObject:action.type forKey:@"type"];
        }
        if (action.urlType != nil) {
            [result setObject:action.urlType forKey:@"urlType"];
        }
        if (action.url != nil) {
            [result setObject:action.url forKey:@"url"];
        }
        if (action.name != nil) {
            [result setObject:action.name forKey:@"name"];
        }
        [self sendEvent:CPEventString(AppBannerOpened) withBody:result];
    }];
}

- (void)handleNotificationOpened:(NSString *)result {
    NSDictionary *json = [self jsonObjectWithString:result];

    if (json) {
        [self sendEvent:CPEventString(NotificationOpened) withBody:json];
    }
}

- (void)handleNotificationReceived:(NSString *)result {
    NSDictionary *json = [self jsonObjectWithString:result];

    if (json) {
        [self sendEvent:CPEventString(NotificationReceived) withBody:json];
    }
}

- (void)handleSubscribed:(NSString *)subscriptionId {
    NSDictionary* result = [[NSDictionary alloc] initWithObjectsAndKeys:
     subscriptionId, @"id",
     nil];

    [self sendEvent:CPEventString(Subscribed) withBody:result];
}

- (NSDictionary *)jsonObjectWithString:(NSString *)jsonString {
    NSError *jsonError;
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&jsonError];

    if (jsonError) {
        NSLog(@"CleverPush: Unable to serialize JSON string into an object: %@", jsonError);
        return nil;
    }

    return json;
}

- (void)sendEvent:(NSString *)eventName withBody:(NSDictionary *)body {
    [RCTCleverPushEventEmitter sendEventWithName:eventName withBody:body];
}

@end
