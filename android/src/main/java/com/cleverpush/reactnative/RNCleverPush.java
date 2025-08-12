package com.cleverpush.reactnative;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.cleverpush.ActivityLifecycleListener;
import com.cleverpush.ChannelTag;
import com.cleverpush.ChannelTopic;
import com.cleverpush.CleverPush;
import com.cleverpush.CustomAttribute;
import com.cleverpush.Notification;
import com.cleverpush.NotificationOpenedResult;
import com.cleverpush.Subscription;
import com.cleverpush.listener.DeviceTokenListener;
import com.cleverpush.listener.NotificationReceivedCallbackListener;
import com.cleverpush.listener.SubscribedListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class RNCleverPush extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NOTIFICATION_OPENED_INTENT_FILTER = "CPNotificationOpened";
    public static final String NOTIFICATION_RECEIVED_INTENT_FILTER = "CPNotificationReceived";

    private CleverPush cleverPush;
    private ReactApplicationContext mReactApplicationContext;
    private ReactContext mReactContext;
    private boolean cleverPushInitDone;
    private boolean registeredEvents = false;
    private boolean showNotificationsInForeground = true;
    private boolean isAppInForeground = false;

    public RNCleverPush(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactApplicationContext = reactContext;
        mReactContext = reactContext;
        mReactContext.addLifecycleEventListener(this);
        initCleverPush();
    }

    private String channelIdFromManifest(ReactApplicationContext context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), context.getPackageManager().GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString("cleverpush_channel_id");
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private void initCleverPush() {
        if (!registeredEvents) {
            registeredEvents = true;
            registerNotificationsOpenedNotification();
            registerNotificationsReceivedNotification();
        }

        String channelId = channelIdFromManifest(mReactApplicationContext);

        if (channelId != null && channelId.length() > 0) {
            try {
                WritableMap map = new WritableNativeMap();
                map.putString("channelId", channelId);
                init(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendEvent(String eventName, Object params) {
        mReactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    private JSONObject jsonFromErrorMessageString(String errorMessage) throws JSONException {
        return new JSONObject().put("error", errorMessage);
    }

    @ReactMethod
    public void init(ReadableMap options) throws Exception {
        Context context = getCurrentActivity();

        if (cleverPushInitDone) {
            Log.e("cleverpush", "Already initialized the CleverPush React-Native SDK");
            return;
        }

        cleverPushInitDone = true;

        if (context == null) {
            context = mReactApplicationContext.getApplicationContext();
        }

        this.cleverPush = CleverPush.getInstance(context);

        NotificationReceivedCallbackListener notificationReceivedCallbackListener = new NotificationReceivedCallbackListener() {
            @Override
            public boolean notificationReceivedCallback(NotificationOpenedResult result) {
                Log.d("CleverPush", "notificationReceived");
                boolean appIsOpen = isAppOpen();

                if (appIsOpen) {
                    try {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("notification", result.getNotification());
                        bundle.putSerializable("subscription", result.getSubscription());

                        final Intent intent = new Intent(RNCleverPush.NOTIFICATION_RECEIVED_INTENT_FILTER);
                        intent.putExtras(bundle);

                        if (mReactContext.hasActiveCatalystInstance()) {
                            mReactContext.sendBroadcast(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("CleverPush", "Encountered an error attempting to convert CPNotification object to map: " + e.getMessage());
                    }
                }

                if (showNotificationsInForeground) {
                    return true;
                }

                return !appIsOpen;
            }
        };

        boolean autoRegister = options.hasKey("autoRegister") ? options.getBoolean("autoRegister") : true;

        cleverPush.init(options.getString("channelId"),
                notificationReceivedCallbackListener,
                new NotificationOpenedHandler(mReactContext),
                new SubscribedListener() {
                    @Override
                    public void subscribed(String subscriptionId) {
                        notifySubscribed(subscriptionId);
                    }
                },
                autoRegister
        );

        this.cleverPush.setAppBannerOpenedListener(action -> {
            WritableMap result = new WritableNativeMap();
            result.putString("type", action.getType());
            result.putString("name", action.getName());
            result.putString("url", action.getUrl());
            result.putString("urlType", action.getUrlType());

            sendEvent("CleverPush-appBannerOpened", result);
        });
    }

    @ReactMethod
    public void getSubscriptionTags(final Callback callback) {
        Set<String> tags = this.cleverPush.getSubscriptionTags();
        WritableArray writableArray = new WritableNativeArray();
        for (String tag : tags) {
            writableArray.pushString(tag);
        }

        if (callback != null) {
            callback.invoke(null, writableArray);
        }
    }

    @ReactMethod
    public void hasSubscriptionTag(String tagId, final Callback callback) {
        boolean hasTag = this.cleverPush.hasSubscriptionTag(tagId);

        if (callback != null) {
            callback.invoke(null, hasTag);
        }
    }

    @ReactMethod
    public void getSubscriptionTopics(final Callback callback) {
        Set<String> topics = this.cleverPush.getSubscriptionTopics();
        WritableArray writableArray = new WritableNativeArray();
        for (String topic : topics) {
            writableArray.pushString(topic);
        }

        if (callback != null) {
            callback.invoke(null, writableArray);
        }
    }

    @ReactMethod
    public void getSubscriptionAttributes(final Callback callback) {
        Map<String, Object> attributes = this.cleverPush.getSubscriptionAttributes();
        WritableMap writableMap = new WritableNativeMap();
        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            writableMap.putString(attribute.getKey(), attribute.getValue().toString());
        }

        if (callback != null) {
            callback.invoke(null, writableMap);
        }
    }

    @ReactMethod
    public void getSubscriptionAttribute(String attributeId, final Callback callback) {
        Object value = this.cleverPush.getSubscriptionAttribute(attributeId);

        if (callback != null) {
            callback.invoke(null, value);
        }
    }

    @ReactMethod
    public void getAvailableTags(final Callback callback) {
        this.cleverPush.getAvailableTags(tags -> {
            WritableArray writableArray = new WritableNativeArray();
            for (ChannelTag tag : tags) {
                WritableMap writeableMapTag = new WritableNativeMap();
                writeableMapTag.putString("id", tag.getId());
                writeableMapTag.putString("name", tag.getName());
                writableArray.pushMap(writeableMapTag);
            }

            if (callback != null) {
                callback.invoke(null, writableArray);
            }
        });
    }

    @ReactMethod
    public void getAvailableTopics(final Callback callback) {
        this.cleverPush.getAvailableTopics(topics -> {
            WritableArray writableArray = new WritableNativeArray();
            for (ChannelTopic topic : topics) {
                WritableMap writeableMapTopic = new WritableNativeMap();
                writeableMapTopic.putString("id", topic.getId());
                writeableMapTopic.putString("name", topic.getName());
                writableArray.pushMap(writeableMapTopic);
            }

            if (callback != null) {
                callback.invoke(null, writableArray);
            }
        });
    }

    @ReactMethod
    public void getAvailableAttributes(final Callback callback) {
        this.cleverPush.getAvailableAttributes(attributes -> {
            WritableArray writableArray = new WritableNativeArray();
            for (CustomAttribute attribute : attributes) {
                WritableMap writeableMapTag = new WritableNativeMap();
                writeableMapTag.putString("id", attribute.getId());
                writeableMapTag.putString("name", attribute.getName());
                writableArray.pushMap(writeableMapTag);
            }

            if (callback != null) {
                callback.invoke(null, writableArray);
            }
        });
    }

    @ReactMethod
    public void addSubscriptionTag(String tagId) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.addSubscriptionTag(tagId);
    }

    @ReactMethod
    public void removeSubscriptionTag(String tagId) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.removeSubscriptionTag(tagId);
    }

    @ReactMethod
    public void setSubscriptionTopics(ReadableArray topicIdsReadableArray) {
        if (this.cleverPush == null) {
            return;
        }
        String[] topicIds = new String[topicIdsReadableArray.size()];
        for (int i = 0; i < topicIdsReadableArray.size(); i++) {
            topicIds[i] = topicIdsReadableArray.getString(i);
        }
        this.cleverPush.setSubscriptionTopics(topicIds);
    }

    @ReactMethod
    public void addSubscriptionTopic(String topicId) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.addSubscriptionTopic(topicId);
    }

    @ReactMethod
    public void removeSubscriptionTopic(String topicId) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.removeSubscriptionTopic(topicId);
    }

    @ReactMethod
    public void setSubscriptionAttribute(String attributeId, String value) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.setSubscriptionAttribute(attributeId, value);
    }

    @ReactMethod
    public void isSubscribed(final Callback callback) {
        boolean isSubscribed = this.cleverPush.isSubscribed();

        if (callback != null) {
            callback.invoke(null, isSubscribed);
        }
    }

    @ReactMethod
    public void getSubscriptionId(final Callback callback) {
        this.cleverPush.getSubscriptionId(subscriptionId -> {
            if (callback != null) {
                callback.invoke(null, subscriptionId);
            }
        });
    }

    @ReactMethod
    public void areNotificationsEnabled(final Callback callback) {
        boolean notificationsEnabled = this.cleverPush.areNotificationsEnabled();

        if (callback != null) {
            callback.invoke(null, notificationsEnabled);
        }
    }

    @ReactMethod
    public void setSubscriptionLanguage(String language) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.setSubscriptionLanguage(language);
    }

    @ReactMethod
    public void setSubscriptionCountry(String country) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.setSubscriptionCountry(country);
    }

    @ReactMethod
    public void subscribe() {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.subscribe();
    }

    @ReactMethod
    public void unsubscribe() {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.unsubscribe();
    }

    @ReactMethod
    public void showTopicsDialog() {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.showTopicsDialog();
    }

    @ReactMethod
    public void enableDevelopmentMode() {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.enableDevelopmentMode();
    }

    @ReactMethod
    public void showAppBanners(final Callback callback) {
        // Deprecated
    }

    @ReactMethod
    public void getNotifications(final Callback callback) {
        Set<Notification> notifications = this.cleverPush.getNotifications();
        WritableArray writableArray = new WritableNativeArray();
        Gson gson = new Gson();
        for (Notification notification : notifications) {
            WritableMap writeableMap = new WritableNativeMap();
            writeableMap.putString("_id", notification.getId());
            writeableMap.putString("title", notification.getTitle());
            writeableMap.putString("text", notification.getText());
            writeableMap.putString("url", notification.getUrl());
            writeableMap.putString("iconUrl", notification.getIconUrl());
            writeableMap.putString("mediaUrl", notification.getMediaUrl());
            writeableMap.putString("createdAt", notification.getCreatedAt());
            writeableMap.putString("tag", notification.getTag());
            if (notification.getActions() != null) {
                String actions = gson.toJson(notification.getActions());
                writeableMap.putString("actions", actions);
            }
            if (notification.getCustomData() != null) {
                String customData = gson.toJson(notification.getCustomData());
                if (customData != null) {
                    try {
                        JSONObject customDataJson = new JSONObject(customData);
                        WritableMap customDataMap = new WritableNativeMap();
                        Iterator<String> keys = customDataJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = customDataJson.optString(key, "");
                            customDataMap.putString(key, value);
                        }
                        writeableMap.putMap("customData", customDataMap);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            writeableMap.putBoolean("chatNotification", notification.isChatNotification());
            writeableMap.putBoolean("carouselEnabled", notification.isCarouselEnabled());
            if (notification.getCarouselItems() != null) {
                String carouselItems = gson.toJson(notification.getCarouselItems());
                writeableMap.putString("carouselItems", carouselItems);
            }
            writeableMap.putString("appBanner", notification.getAppBanner());
            writeableMap.putString("inboxAppBanner", notification.getInboxAppBanner());

            writableArray.pushMap(writeableMap);
        }

        if (callback != null) {
            callback.invoke(null, writableArray);
        }
    }

    @ReactMethod
    public void requestLocationPermission() {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.requestLocationPermission();
    }

    private void registerNotificationsOpenedNotification() {
        IntentFilter intentFilter = new IntentFilter(NOTIFICATION_OPENED_INTENT_FILTER);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyNotificationOpened(intent.getExtras());
            }
        };
        registerReceiverWithCompatibility(mReactContext, receiver, intentFilter, true);
    }

    private void registerNotificationsReceivedNotification() {
        IntentFilter intentFilter = new IntentFilter(NOTIFICATION_RECEIVED_INTENT_FILTER);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyNotificationReceived(intent.getExtras());
            }
        };
        registerReceiverWithCompatibility(mReactContext, receiver, intentFilter, true);
    }

    /**
     * Registers a broadcast receiver to listen for notifications opened events.
     * <p>
     * This method sets up an IntentFilter for the specified notification opened intent
     * and registers a BroadcastReceiver to handle these intents. For devices running
     * Android 14 (API level 34) and above, it uses the Context.RECEIVER_EXPORTED or
     * Context.RECEIVER_NOT_EXPORTED flag to ensure the receiver is private to the app.
     * For lower API levels, it registers the receiver without this flag.
     * <p>
     * RECEIVER_NOT_EXPORTED: Use this flag if the receiver should not be accessible by other applications.
     * <p>
     * RECEIVER_EXPORTED: Use this flag if the receiver should be accessible by other applications.
     */
    private void registerReceiverWithCompatibility(Context context, BroadcastReceiver receiver, IntentFilter filter, boolean exported) {
        if (Build.VERSION.SDK_INT >= 34 && context.getApplicationInfo().targetSdkVersion >= 34) {
            context.registerReceiver(
                receiver, filter, exported ? Context.RECEIVER_EXPORTED : Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver, filter);
        }
    }

    @ReactMethod
    public void setAutoClearBadge(boolean autoClear) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.setAutoClearBadge(autoClear);
    }

    @ReactMethod
    public void setIncrementBadge(boolean increment) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.setIncrementBadge(increment);
    }

    @ReactMethod
    public void setBadgeCount(int count) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.setBadgeCount(count);
    }

    @ReactMethod
    public void getBadgeCount(final Callback callback) {
        if (this.cleverPush == null || callback == null) {
            return;
        }
        int count = this.cleverPush.getBadgeCount();
        callback.invoke(null, count);
    }

    @ReactMethod
    public void setAutoResubscribe(boolean autoResubscribe) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.setAutoResubscribe(autoResubscribe);
    }

    @ReactMethod
    public void trackPageView(String url, ReadableMap params) {
        if (this.cleverPush == null) {
            return;
        }

        if (params != null) {
            HashMap<String, String> paramsMap = new HashMap<>();
            ReadableMapKeySetIterator iterator = params.keySetIterator();
            while (iterator.hasNextKey()) {
                String paramKey = iterator.nextKey();
                paramsMap.put(paramKey, params.getString(paramKey));
            }
            this.cleverPush.trackPageView(url, paramsMap);
        } else {
            this.cleverPush.trackPageView(url);
        }
    }

    @ReactMethod
    public void trackEvent(String name, ReadableMap properties) {
        if (this.cleverPush == null) {
            return;
        }

        if (properties != null) {
            HashMap<String, Object> propertiesMap = new HashMap<>();
            ReadableMapKeySetIterator iterator = properties.keySetIterator();
            while (iterator.hasNextKey()) {
                String propertyKey = iterator.nextKey();
                propertiesMap.put(propertyKey, properties.getString(propertyKey));
            }
            this.cleverPush.trackEvent(name, propertiesMap);
        } else {
            this.cleverPush.trackEvent(name);
        }
    }

    @ReactMethod
    public void setShowNotificationsInForeground(boolean show) {
        this.showNotificationsInForeground = show;
    }

    @ReactMethod
    public void clearNotificationsFromNotificationCenter() {
        try {
            NotificationManager notificationManager = (NotificationManager)
                    mReactApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void removeNotification(String notificationId) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.removeNotification(notificationId);
    }

    @ReactMethod
    public void removeNotificationWithCenter(String notificationId, boolean removeFromNotificationCenter) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.removeNotification(notificationId, removeFromNotificationCenter);
    }

    @ReactMethod
    public void enableAppBanners() {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.enableAppBanners();
    }

    @ReactMethod
    public void disableAppBanners() {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.disableAppBanners();
    }

    @ReactMethod
    public void getDeviceToken(final Callback callback) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.getDeviceToken(new DeviceTokenListener() {
            @Override
            public void complete(String deviceToken) {
                if (callback != null) {
                    callback.invoke(null, deviceToken);
                }
            }
        });
    }

    /**
     * Required for NativeEventEmitter
     */
    @ReactMethod
    public void addListener(String eventName) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @ReactMethod
    public void removeListeners(int count) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    private void notifySubscribed(String subscriptionId) {
        try {
            WritableMap result = new WritableNativeMap();
            result.putString("id", subscriptionId);
            sendEvent("CleverPush-subscribed", result);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void notifyNotificationOpened(Bundle bundle) {
        try {
            WritableMap result = new WritableNativeMap();

            Notification notification = (Notification) bundle.getSerializable("notification");
            if (notification != null) {
                Gson gson = new Gson();
                WritableMap notificationMap = new WritableNativeMap();
                notificationMap.putString("id", notification.getId());
                notificationMap.putString("title", notification.getTitle());
                notificationMap.putString("text", notification.getText());
                notificationMap.putString("url", notification.getUrl());
                notificationMap.putString("iconUrl", notification.getIconUrl());
                notificationMap.putString("mediaUrl", notification.getMediaUrl());
                notificationMap.putString("tag", notification.getTag());
                if (notification.getActions() != null) {
                    String actions = gson.toJson(notification.getActions());
                    notificationMap.putString("actions", actions);
                }
                if (notification.getCustomData() != null) {
                    String customData = gson.toJson(notification.getCustomData());
                    if (customData != null) {
                        try {
                            JSONObject customDataJson = new JSONObject(customData);
                            WritableMap customDataMap = new WritableNativeMap();
                            Iterator<String> keys = customDataJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                String value = customDataJson.optString(key, "");
                                customDataMap.putString(key, value);
                            }
                            notificationMap.putMap("customData", customDataMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                notificationMap.putBoolean("chatNotification", notification.isChatNotification());
                notificationMap.putBoolean("carouselEnabled", notification.isCarouselEnabled());
                if (notification.getCarouselItems() != null) {
                    String carouselItems = gson.toJson(notification.getCarouselItems());
                    notificationMap.putString("carouselItems", carouselItems);
                }
                notificationMap.putString("soundFilename", notification.getSoundFilename());
                notificationMap.putBoolean("silent", notification.isSilent());
                notificationMap.putString("createdAt", notification.getCreatedAt());
                notificationMap.putString("appBanner", notification.getAppBanner());
                notificationMap.putString("inboxAppBanner", notification.getInboxAppBanner());
                result.putMap("notification", notificationMap);
            }

            Subscription subscription = (Subscription) bundle.getSerializable("subscription");
            if (subscription != null) {
                WritableMap subscriptionMap = new WritableNativeMap();
                subscriptionMap.putString("id", subscription.getId());
                result.putMap("subscription", subscriptionMap);
            }

            sendEvent("CleverPush-notificationOpened", result);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void notifyNotificationReceived(Bundle bundle) {
        try {
            WritableMap result = new WritableNativeMap();

            Notification notification = (Notification) bundle.getSerializable("notification");
            if (notification != null) {
                Gson gson = new Gson();
                WritableMap notificationMap = new WritableNativeMap();
                notificationMap.putString("id", notification.getId());
                notificationMap.putString("title", notification.getTitle());
                notificationMap.putString("text", notification.getText());
                notificationMap.putString("url", notification.getUrl());
                notificationMap.putString("iconUrl", notification.getIconUrl());
                notificationMap.putString("mediaUrl", notification.getMediaUrl());
                notificationMap.putString("tag", notification.getTag());
                if (notification.getActions() != null) {
                    String actions = gson.toJson(notification.getActions());
                    notificationMap.putString("actions", actions);
                }
                if (notification.getCustomData() != null) {
                    String customData = gson.toJson(notification.getCustomData());
                    if (customData != null) {
                        try {
                            JSONObject customDataJson = new JSONObject(customData);
                            WritableMap customDataMap = new WritableNativeMap();
                            Iterator<String> keys = customDataJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                String value = customDataJson.optString(key, "");
                                customDataMap.putString(key, value);
                            }
                            notificationMap.putMap("customData", customDataMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                notificationMap.putBoolean("chatNotification", notification.isChatNotification());
                notificationMap.putBoolean("carouselEnabled", notification.isCarouselEnabled());
                if (notification.getCarouselItems() != null) {
                    String carouselItems = gson.toJson(notification.getCarouselItems());
                    notificationMap.putString("carouselItems", carouselItems);
                }
                notificationMap.putString("soundFilename", notification.getSoundFilename());
                notificationMap.putBoolean("silent", notification.isSilent());
                notificationMap.putString("createdAt", notification.getCreatedAt());
                notificationMap.putString("appBanner", notification.getAppBanner());
                notificationMap.putString("inboxAppBanner", notification.getInboxAppBanner());
                result.putMap("notification", notificationMap);
            }

            Subscription subscription = (Subscription) bundle.getSerializable("subscription");
            if (subscription != null) {
                WritableMap subscriptionMap = new WritableNativeMap();
                subscriptionMap.putString("id", subscription.getId());
                result.putMap("subscription", subscriptionMap);
            }

            sendEvent("CleverPush-notificationReceived", result);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "CleverPush";
    }

    @Override
    public void onHostDestroy() {
        isAppInForeground = false;
    }

    @Override
    public void onHostPause() {
        isAppInForeground = false;
    }

    @Override
    public void onHostResume() {
        isAppInForeground = true;
        initCleverPush();
        Context context = getCurrentActivity();
        if (context == null) {
            context = mReactApplicationContext.getApplicationContext();
        }
        this.cleverPush = CleverPush.getInstance(context);
        ActivityLifecycleListener.currentActivity = getCurrentActivity();
    }

    public boolean isAppOpen() {
        return isAppInForeground;
    }
}
