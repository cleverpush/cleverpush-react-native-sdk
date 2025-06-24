#import "RCTCleverPushEventEmitter.h"
#import <CleverPush/CleverPush.h>
#import <UserNotifications/UserNotifications.h>

#import "RCTCleverPush.h"

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"


@implementation RCTCleverPushEventEmitter {
    BOOL hasListeners;
}

static BOOL _didStartObserving = false;

+ (BOOL)hasSetBridge {
    return _didStartObserving;
}

+(BOOL)requiresMainQueueSetup {
    return YES;
}

RCT_EXPORT_MODULE(RCTCleverPush)

-(instancetype)init {
    if (self = [super init]) {
        NSLog(@"CleverPush: Initialized RCTCleverPushEventEmitter");

        for (NSString *eventName in [self supportedEvents])
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(emitEvent:) name:eventName object:nil];
    }

    return self;
}

-(void)startObserving {
    hasListeners = true;
    NSLog(@"CleverPush: RCTCleverPushEventEmitter did start observing");

    [[NSNotificationCenter defaultCenter] postNotificationName:@"didSetBridge" object:nil];

    _didStartObserving = true;
}

-(void)stopObserving {
    hasListeners = false;
    NSLog(@"CleverPush: RCTCleverPushEventEmitter did stop observing");
}

-(NSArray<NSString *> *)supportedEvents {
    NSMutableArray *events = [NSMutableArray new];

    for (int i = 0; i < CPNotificationEventTypesArray.count; i++)
        [events addObject:CPEventString(i)];

    return events;
}

- (void)emitEvent:(NSNotification *)notification {
    if (!hasListeners) {
        NSLog(@"CleverPush: Attempted to send an event (%@) when no listeners were set.", notification.name);
        return;
    }

    [self sendEventWithName:notification.name body:notification.userInfo];
}

+ (void)sendEventWithName:(NSString *)name withBody:(NSDictionary *)body {
    NSLog(@"CleverPush: sending event %@", name);
    [[NSNotificationCenter defaultCenter] postNotificationName:name object:nil userInfo:body];
}

RCT_EXPORT_METHOD(init:(NSDictionary *)options) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[RCTCleverPush sharedInstance] init:options];
    });
}

RCT_EXPORT_METHOD(getAvailableTags:(RCTResponseSenderBlock)callback) {
    NSArray* channelTags = [CleverPush getAvailableTags];
    callback(@[[NSNull null], channelTags]);
}

RCT_EXPORT_METHOD(getAvailableTopics:(RCTResponseSenderBlock)callback) {
    NSArray *channelTopics = [CleverPush getAvailableTopics];
    NSMutableArray *topicsArray = [NSMutableArray new];
    
    for (CPChannelTopic *topic in channelTopics) {
        NSMutableDictionary *topicDict = [NSMutableDictionary dictionary];
        
        if (topic.id != nil && ![topic.id isKindOfClass:[NSNull class]] && ![topic.id isEqualToString:@""]) {
            [topicDict setObject:topic.id forKey:@"id"];
        } else {
            [topicDict setObject:@"" forKey:@"id"];
        }
        
        if (topic.name != nil && ![topic.name isKindOfClass:[NSNull class]] && ![topic.name isEqualToString:@""]) {
            [topicDict setObject:topic.name forKey:@"name"];
        } else {
            [topicDict setObject:@"" forKey:@"name"];
        }
        
        [topicsArray addObject:topicDict];
    }
    
    callback(@[[NSNull null], topicsArray]);
}

RCT_EXPORT_METHOD(getAvailableAttributes:(RCTResponseSenderBlock)callback) {
    NSDictionary* customAttributes = [CleverPush getAvailableAttributes];
    callback(@[[NSNull null], customAttributes]);
}

RCT_EXPORT_METHOD(getSubscriptionTags:(RCTResponseSenderBlock)callback) {
    NSArray* subscriptionTags = [CleverPush getSubscriptionTags];
    callback(@[[NSNull null], subscriptionTags]);
}

RCT_EXPORT_METHOD(getSubscriptionTopics:(RCTResponseSenderBlock)callback) {
    NSArray* subscriptionTopics = [CleverPush getSubscriptionTopics];
    callback(@[[NSNull null], subscriptionTopics]);
}

RCT_EXPORT_METHOD(getSubscriptionAttributes:(RCTResponseSenderBlock)callback) {
    NSDictionary* subscriptionAttributes = [CleverPush getSubscriptionAttributes];
    callback(@[[NSNull null], subscriptionAttributes]);
}

RCT_EXPORT_METHOD(getSubscriptionAttribute:(NSString *)attributeId callback:(RCTResponseSenderBlock)callback) {
    NSString* attributeValue = [CleverPush getSubscriptionAttribute:attributeId];
    callback(@[[NSNull null], attributeValue]);
}

RCT_EXPORT_METHOD(hasSubscriptionTag:(NSString *)tagId callback:(RCTResponseSenderBlock)callback) {
    bool hasTag = [CleverPush hasSubscriptionTag:tagId];
    callback(@[[NSNull null], [NSNumber numberWithBool:hasTag]]);
}

RCT_EXPORT_METHOD(addSubscriptionTag:(NSString *)tagId) {
    [CleverPush addSubscriptionTag:tagId];
}

RCT_EXPORT_METHOD(removeSubscriptionTag:(NSString *)tagId) {
    [CleverPush removeSubscriptionTag:tagId];
}

RCT_EXPORT_METHOD(addSubscriptionTopic:(NSString *)topicId) {
    [CleverPush addSubscriptionTopic:topicId];
}

RCT_EXPORT_METHOD(removeSubscriptionTopic:(NSString *)topicId) {
    [CleverPush removeSubscriptionTopic:topicId];
}

RCT_EXPORT_METHOD(setSubscriptionTopics:(NSArray *)topicIds) {
    [CleverPush setSubscriptionTopics:topicIds];
}

RCT_EXPORT_METHOD(setSubscriptionAttribute:(NSString *)attributeId value:(NSString*)value) {
    [CleverPush setSubscriptionAttribute:attributeId value:value];
}

RCT_EXPORT_METHOD(isSubscribed:(RCTResponseSenderBlock)callback) {
    bool isSubscribed = [CleverPush isSubscribed];
    callback(@[[NSNull null], [NSNumber numberWithBool:isSubscribed]]);
}

RCT_EXPORT_METHOD(getSubscriptionId:(RCTResponseSenderBlock)callback) {
    [CleverPush getSubscriptionId:^(NSString* subscriptionId) {
        callback(@[[NSNull null], subscriptionId]);
    }];
}

RCT_EXPORT_METHOD(areNotificationsEnabled:(RCTResponseSenderBlock)callback) {
    [CleverPush areNotificationsEnabled:^(BOOL notificationsEnabled) {
        callback(@[[NSNull null], [NSNumber numberWithBool:notificationsEnabled]]);
    }];
}

RCT_EXPORT_METHOD(setSubscriptionLanguage:(NSString *)language) {
    [CleverPush setSubscriptionLanguage:language];
}

RCT_EXPORT_METHOD(setSubscriptionCountry:(NSString *)country) {
    [CleverPush setSubscriptionCountry:country];
}

RCT_EXPORT_METHOD(subscribe) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [CleverPush subscribe];
    });
}

RCT_EXPORT_METHOD(unsubscribe) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [CleverPush unsubscribe];
    });
}

RCT_EXPORT_METHOD(showTopicsDialog) {
    [CleverPush showTopicsDialog];
}

RCT_EXPORT_METHOD(enableDevelopmentMode) {
    [CleverPush enableDevelopmentMode];
}

RCT_EXPORT_METHOD(getNotifications:(RCTResponseSenderBlock)callback) {
    NSArray* notifications = [CleverPush getNotifications];
    callback(@[[NSNull null], notifications]);
}

RCT_EXPORT_METHOD(requestLocationPermission) {
    // not supported, yet
}

RCT_EXPORT_METHOD(setAutoClearBadge:(BOOL)autoClear) {
    [CleverPush setAutoClearBadge:autoClear];
}

RCT_EXPORT_METHOD(setIncrementBadge:(BOOL)increment) {
    [CleverPush setIncrementBadge:increment];
}

RCT_EXPORT_METHOD(setBadgeCount:(NSInteger)count) {
    [CleverPush setBadgeCount:count];
}

RCT_EXPORT_METHOD(getBadgeCount:(RCTResponseSenderBlock)callback) {
    [CleverPush getBadgeCount:^(NSInteger badge) {
        callback(@[[NSNull null], [NSNumber numberWithInteger:badge]]);
    }];
}

RCT_EXPORT_METHOD(trackPageView:(NSString*)url params:(NSDictionary*)params) {
    if (params != nil) {
        [CleverPush trackPageView:url];
    } else {
        [CleverPush trackPageView:url params:params];
    }
}

RCT_EXPORT_METHOD(trackEvent:(NSString*)name properties:(NSDictionary*)properties) {
    if (properties != nil) {
        [CleverPush trackEvent:name];
    } else {
        [CleverPush trackEvent:name properties:properties];
    }
}

RCT_EXPORT_METHOD(setAutoResubscribe:(BOOL)autoResubscribe) {
    [CleverPush setAutoResubscribe:autoResubscribe];
}

RCT_EXPORT_METHOD(setShowNotificationsInForeground:(BOOL)show) {
    [CleverPush setShowNotificationsInForeground:show];
}

RCT_EXPORT_METHOD(clearNotificationsFromNotificationCenter) {
    [[UNUserNotificationCenter currentNotificationCenter] removeAllDeliveredNotifications];
}

RCT_EXPORT_METHOD(removeNotificationWithCenter:(NSString *)notificationId removeFromNotificationCenter:(BOOL)removeFromNotificationCenter) {
    [CleverPush removeNotification:notificationId removeFromNotificationCenter:removeFromNotificationCenter];
}

RCT_EXPORT_METHOD(removeNotification:(NSString *)notificationId) {
    [CleverPush removeNotification:notificationId];
}

@end
