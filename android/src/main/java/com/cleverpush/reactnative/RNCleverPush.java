package com.cleverpush.reactnative;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import com.cleverpush.ActivityLifecycleListener;
import com.cleverpush.ChannelTag;
import com.cleverpush.ChannelTopic;
import com.cleverpush.CleverPush;
import com.cleverpush.CustomAttribute;
import com.cleverpush.Notification;
import com.cleverpush.Subscription;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class RNCleverPush extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NOTIFICATION_OPENED_INTENT_FILTER = "CPNotificationOpened";

    private CleverPush cleverPush;
    private ReactApplicationContext mReactApplicationContext;
    private ReactContext mReactContext;
    private boolean cleverPushInitDone;
    private boolean registeredEvents = false;

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
        cleverPush.init(options.getString("channelId"), new NotificationOpenedHandler(mReactContext), new SubscribedListener() {
            @Override
            public void subscribed(String subscriptionId) {
                notifySubscribed(subscriptionId);
            }
        });

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
        for (Notification notification : notifications) {
            WritableMap writeableMap = new WritableNativeMap();
            writeableMap.putString("_id", notification.getId());
            writeableMap.putString("title", notification.getTitle());
            writeableMap.putString("text", notification.getText());
            writeableMap.putString("url", notification.getUrl());
            writeableMap.putString("iconUrl", notification.getIconUrl());
            writeableMap.putString("mediaUrl", notification.getMediaUrl());
            writeableMap.putString("createdAt", notification.getCreatedAt());
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
        mReactContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyNotificationOpened(intent.getExtras());
            }
        }, intentFilter);
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
                WritableMap notificationMap = new WritableNativeMap();
                notificationMap.putString("id", notification.getId());
                notificationMap.putString("title", notification.getTitle());
                notificationMap.putString("text", notification.getText());
                notificationMap.putString("url", notification.getUrl());
                notificationMap.putString("iconUrl", notification.getIconUrl());
                notificationMap.putString("mediaUrl", notification.getMediaUrl());
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

    @Override
    public String getName() {
        return "CleverPush";
    }

    @Override
    public void onHostDestroy() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostResume() {
        initCleverPush();
        Context context = getCurrentActivity();
        if (context == null) {
            context = mReactApplicationContext.getApplicationContext();
        }
        this.cleverPush = CleverPush.getInstance(context);
        ActivityLifecycleListener.currentActivity = getCurrentActivity();
    }
}
