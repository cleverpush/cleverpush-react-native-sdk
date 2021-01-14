package com.cleverpush.reactnative;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;

import com.cleverpush.ChannelTag;
import com.cleverpush.CleverPush;
import com.cleverpush.CustomAttribute;
import com.cleverpush.Notification;
import com.cleverpush.Subscription;
import com.cleverpush.listener.SubscribedListener;
import com.cleverpush.listener.AppBannerUrlOpenedListener;
import com.cleverpush.listener.AppBannerOpenedListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public class RNCleverPush extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NOTIFICATION_OPENED_INTENT_FILTER = "CPNotificationOpened";

    private CleverPush cleverPush;
    private ReactApplicationContext mReactApplicationContext;
    private ReactContext mReactContext;
    private boolean cleverPushInitDone;
    private boolean registeredEvents = false;

    private Callback pendingGetAvailableTagsCallback;
    private Callback pendingGetAvailableAttributesCallback;
    private Callback pendingGetSubscriptionTagsCallback;
    private Callback pendingGetSubscriptionAttributesCallback;
    private Callback pendingHasSubscriptionTagCallback;
    private Callback pendingGetSubscriptionAttributeCallback;
    private Callback pendingIsSubscribedCallback;
    private Callback pendingGetNotificationsCallback;
    private Callback pendingShowAppBannersCallback;
    private Callback pendingSetAppBannerOpenedCallback;

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
    }

    @ReactMethod
    public void getSubscriptionTags(final Callback callback) {
        if (pendingGetSubscriptionTagsCallback == null)
            pendingGetSubscriptionTagsCallback = callback;

        Set<String> tags = this.cleverPush.getSubscriptionTags();
        WritableArray writableArray = new WritableNativeArray();
        for (String tag : tags) {
            writableArray.pushString(tag);
        }

        if (pendingGetSubscriptionTagsCallback != null)
            pendingGetSubscriptionTagsCallback.invoke(writableArray);

        pendingGetSubscriptionTagsCallback = null;
    }

    @ReactMethod
    public void hasSubscriptionTag(String tagId, final Callback callback) {
        if (pendingHasSubscriptionTagCallback == null)
            pendingHasSubscriptionTagCallback = callback;

        boolean hasTag = this.cleverPush.hasSubscriptionTag(tagId);

        if (pendingHasSubscriptionTagCallback != null)
            pendingHasSubscriptionTagCallback.invoke(hasTag);

        pendingHasSubscriptionTagCallback = null;
    }

    @ReactMethod
    public void getSubscriptionAttributes(final Callback callback) {
        if (pendingGetSubscriptionAttributesCallback == null)
            pendingGetSubscriptionAttributesCallback = callback;

        Map<String, String> attributes = this.cleverPush.getSubscriptionAttributes();
        WritableMap writableMap = new WritableNativeMap();
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            writableMap.putString(attribute.getKey(), attribute.getValue());
        }

        if (pendingGetSubscriptionAttributesCallback != null)
            pendingGetSubscriptionAttributesCallback.invoke(writableMap);

        pendingGetSubscriptionAttributesCallback = null;
    }

    @ReactMethod
    public void getSubscriptionAttribute(String attributeId, final Callback callback) {
        if (pendingGetSubscriptionAttributeCallback == null)
            pendingGetSubscriptionAttributeCallback = callback;

        String value = this.cleverPush.getSubscriptionAttribute(attributeId);

        if (pendingGetSubscriptionAttributeCallback != null)
            pendingGetSubscriptionAttributeCallback.invoke(value);

        pendingGetSubscriptionAttributeCallback = null;
    }

    @ReactMethod
    public void getAvailableTags(final Callback callback) {
        if (pendingGetAvailableTagsCallback == null)
            pendingGetAvailableTagsCallback = callback;

        Set<ChannelTag> tags = this.cleverPush.getAvailableTags();
        WritableArray writableArray = new WritableNativeArray();
        for (ChannelTag tag : tags) {
            WritableMap writeableMapTag = new WritableNativeMap();
            writeableMapTag.putString("id", tag.getId());
            writeableMapTag.putString("name", tag.getName());
            writableArray.pushMap(writeableMapTag);
        }

        if (pendingGetAvailableTagsCallback != null)
            pendingGetAvailableTagsCallback.invoke(writableArray);

        pendingGetAvailableTagsCallback = null;
    }

    @ReactMethod
    public void getAvailableAttributes(final Callback callback) {
        if (pendingGetAvailableAttributesCallback == null)
            pendingGetAvailableAttributesCallback = callback;

        Set<CustomAttribute> attributes = this.cleverPush.getAvailableAttributes();
        WritableArray writableArray = new WritableNativeArray();
        for (CustomAttribute attribute : attributes) {
            WritableMap writeableMapTag = new WritableNativeMap();
            writeableMapTag.putString("id", attribute.getId());
            writeableMapTag.putString("name", attribute.getName());
            writableArray.pushMap(writeableMapTag);
        }

        if (pendingGetAvailableAttributesCallback != null)
            pendingGetAvailableAttributesCallback.invoke(writableArray);

        pendingGetAvailableAttributesCallback = null;
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
    public void removeSubscriptionTag(String attributeId, String value) {
        if (this.cleverPush == null) {
            return;
        }
        this.cleverPush.setSubscriptionAttribute(attributeId, value);
    }

    @ReactMethod
    public void isSubscribed(final Callback callback) {
        if (pendingIsSubscribedCallback == null)
            pendingIsSubscribedCallback = callback;

        boolean isSubscribed = this.cleverPush.isSubscribed();

        if (pendingIsSubscribedCallback != null)
            pendingIsSubscribedCallback.invoke(null, isSubscribed);

        pendingIsSubscribedCallback = null;
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
        if (pendingShowAppBannersCallback == null)
            pendingShowAppBannersCallback = callback;

        this.cleverPush.showAppBanners(new AppBannerUrlOpenedListener() {
            @Override
            public void opened(String url) {
                if (pendingShowAppBannersCallback != null)
                    pendingShowAppBannersCallback.invoke(null, url);

                pendingShowAppBannersCallback = null;
            }
        });
    }

    @ReactMethod
    public void setAppBannerOpenedCallback(final Callback callback) {
        if (pendingSetAppBannerOpenedCallback == null)
            pendingSetAppBannerOpenedCallback = callback;

        this.cleverPush..setAppBannerOpenedListener(action -> {
            if (pendingSetAppBannerOpenedCallback != null)
                WritableMap result = new WritableNativeMap();
                result.putString("type", action.getType());
                result.putString("name", action.getName());
                result.putString("url", action.getUrl());
                result.putString("urlType", action.getUrlType());

                pendingSetAppBannerOpenedCallback.invoke(null, result);

                pendingSetAppBannerOpenedCallback = null;
        });
    }

    @ReactMethod
    public void getNotifications(final Callback callback) {
        if (pendingGetNotificationsCallback == null)
            pendingGetNotificationsCallback = callback;

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

        if (pendingGetNotificationsCallback != null)
            pendingGetNotificationsCallback.invoke(null, writableArray);

        pendingGetNotificationsCallback = null;
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
    }
}
