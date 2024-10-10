package com.cleverpush.reactnative;

import android.content.Intent;
import android.os.Bundle;

import com.cleverpush.NotificationOpenedResult;
import com.cleverpush.listener.NotificationReceivedListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;

public class NotificationReceivedHandler implements NotificationReceivedListener {

  private ReactContext mReactContext;

  public NotificationReceivedHandler(ReactContext reactContext) {
    mReactContext = reactContext;
  }

  @Override
  public void notificationReceived(NotificationOpenedResult result) {
    Bundle bundle = new Bundle();
    bundle.putSerializable("notification", result.getNotification());
    bundle.putSerializable("subscription", result.getSubscription());

    final Intent intent = new Intent(RNCleverPush.NOTIFICATION_RECEIVED_INTENT_FILTER);
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
