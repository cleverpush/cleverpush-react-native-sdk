#import <UIKit/UIKit.h>
#import <objc/runtime.h>

@interface RCTCleverPush
+ (RCTCleverPush *)sharedInstance;
- (void)initCleverPush:(NSDictionary*)launchOptions;
@end

@implementation UIApplication(CleverPushReactNative)

static void injectSelector(Class newClass, SEL newSel, Class addToClass, SEL makeLikeSel) {
    Method newMeth = class_getInstanceMethod(newClass, newSel);
    IMP imp = method_getImplementation(newMeth);
    const char* methodTypeEncoding = method_getTypeEncoding(newMeth);
    
    BOOL successful = class_addMethod(addToClass, makeLikeSel, imp, methodTypeEncoding);
    if (!successful) {
        class_addMethod(addToClass, newSel, imp, methodTypeEncoding);
        newMeth = class_getInstanceMethod(addToClass, newSel);
        
        Method orgMeth = class_getInstanceMethod(addToClass, makeLikeSel);
        
        method_exchangeImplementations(orgMeth, newMeth);
    }
}

+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        method_exchangeImplementations(class_getInstanceMethod(self, @selector(setDelegate:)), class_getInstanceMethod(self, @selector(setCleverPushReactNativeDelegate:)));
    });
}

- (void)setCleverPushReactNativeDelegate:(id<UIApplicationDelegate>)delegate {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class delegateClass = [delegate class];
        
        injectSelector(self.class, @selector(cleverPushApplication:didFinishLaunchingWithOptions:),
                       delegateClass, @selector(application:didFinishLaunchingWithOptions:));
        [self setCleverPushReactNativeDelegate:delegate];
    });
}

- (BOOL)cleverPushApplication:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
    [RCTCleverPush.sharedInstance initCleverPush:launchOptions];
    
    if ([self respondsToSelector:@selector(cleverPushApplication:didFinishLaunchingWithOptions:)])
        return [self cleverPushApplication:application didFinishLaunchingWithOptions:launchOptions];
    return YES;
}

@end
