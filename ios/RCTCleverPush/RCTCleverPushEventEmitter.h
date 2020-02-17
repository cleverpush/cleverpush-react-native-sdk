#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>
#elif __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#import "RCTConvert.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"
#endif

typedef NS_ENUM(NSInteger, CPNotificationEventTypes) {
    NotificationReceived,
    NotificationOpened,
    Subscribed
};

#define CPNotificationEventTypesArray @[@"CleverPush-notificationReceived",@"CleverPush-notificationOpened",@"CleverPush-subscribed"]
#define CPEventString(enum) [CPNotificationEventTypesArray objectAtIndex:enum]

@interface RCTCleverPushEventEmitter : RCTEventEmitter <RCTBridgeModule>

+ (void)sendEventWithName:(NSString *)name withBody:(NSDictionary *)body;
+ (BOOL)hasSetBridge;

@end
