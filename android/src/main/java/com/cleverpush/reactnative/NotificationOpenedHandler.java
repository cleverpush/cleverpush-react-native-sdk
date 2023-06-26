package com.cleverpush.reactnative;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.cleverpush.listener.NotificationOpenedListener;
import com.cleverpush.NotificationOpenedResult;

public class NotificationOpenedHandler implements NotificationOpenedListener {

    private ReactContext mReactContext;

    public NotificationOpenedHandler(ReactContext reactContext) {
        mReactContext = reactContext;
    }

    @Override
    public void notificationOpened(NotificationOpenedResult result) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("notification", result.getNotification());
        bundle.putSerializable("subscription", result.getSubscription());

        final Intent intent = new Intent(RNCleverPush.NOTIFICATION_OPENED_INTENT_FILTER);
        intent.putExtras(bundle);

        if (mReactContext.hasActiveCatalystInstance()) {
			mReactContext.sendBroadcast(intent);
            return;
        }

        mReactContext.addLifecycleEventListener(new LifecycleEventListener() {
            @Override
            public void onHostResume() {
              mReactContext.sendBroadcast(intent);
                      mReactContext.removeLifecycleEventListener(this);
            }

            @Override
            public void onHostPause() {

            }

            @Override
            public void onHostDestroy() {

            }
        });
    }
}
