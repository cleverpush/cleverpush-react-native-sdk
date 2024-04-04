#import <CleverPush/CleverPush.h>

@interface RCTCleverPush : NSObject

+ (RCTCleverPush *)sharedInstance;

@property (nonatomic) BOOL didStartObserving;

- (void)init:(NSDictionary *)options;

@end
