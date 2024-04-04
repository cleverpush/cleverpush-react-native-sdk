#import <CleverPush/CleverPush.h>

#import "RCTCleverPushExtensionService.h"

@implementation RCTCleverPushExtensionService

+ (void)didReceiveNotificationRequest:(UNNotificationRequest *)request withContent:(UNMutableNotificationContent * _Nullable)content {
    [CleverPush didReceiveNotificationExtensionRequest:request withMutableNotificationContent:content];
}

+ (void)serviceExtensionTimeWillExpireRequest:(UNNotificationRequest *)request withMutableNotificationContent:(UNMutableNotificationContent * _Nullable)content {
    [CleverPush serviceExtensionTimeWillExpireRequest:request withMutableNotificationContent:content];
}

@end
