#if __has_include(<CleverPush/CleverPush.h>)
#import <CleverPush/CleverPush.h>
#else
#import "CleverPush.h"
#endif

@interface RCTCleverPush : NSObject

+ (RCTCleverPush *)sharedInstance;

@property (nonatomic) BOOL didStartObserving;

- (void)init:(NSDictionary *)options;

@end
