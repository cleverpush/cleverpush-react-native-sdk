#import <CleverPush/CleverPush.h>

@interface RCTCleverPush : NSObject

+ (RCTCleverPush *)sharedInstance;

@property (nonatomic) BOOL didStartObserving;

- (void)init:(NSDictionary *)options;
- (void)setNotificationHandlerEnabled:(BOOL)enabled;
- (void)completeNotificationBehavior:(NSString *)requestId behavior:(NSDictionary *)behavior;

@end
