## 1.7.19 (01.08.2025)
* Updated to latest iOS + Android SDKs

## 1.7.18 (29.07.2025)
* Updated to latest iOS + Android SDKs

## 1.7.17 (21.07.2025)
* Fixed the issue where the `subscribed` listener was not being called on Android

## 1.7.16 (06.07.2025)
* Implemented `enableAppBanners` and `disableAppBanners` methods on Android and iOS
* Fixed an issue in `getAvailableTopics` on iOS, which now returns a list of topic objects instead of null.

## 1.7.15 (17.06.2025)
* Optimized `initFeatures` functions for preventing crashes regarding the UI thread.
* Updated to latest iOS + Android SDKs

## 1.7.14 (14.05.2025)
* Implemented `clearNotificationsFromNotificationCenter` method on Android
* Implemented `removeNotification` method on Android and iOS
* Updated to latest iOS + Android SDKs

## 1.7.13
* Removed automatic `setAutoClearBadge(false)` call on iOS

## 1.7.12
* Updated to latest iOS + Android SDKs

## 1.7.11 (05.03.2025)
* Updated to latest iOS + Android SDKs
* Fixed an issue on Android, where notifications were also being hidden in the background when using `setShowNotificationsInForeground(false)`.

## 1.7.10
* Implemented `clearNotificationsFromNotificationCenter` method on iOS

## 1.7.9
* Implemented `getBadgeCount` and `setBadgeCount` methods

## 1.7.8
* Fixed `autoRegister` parameter for Android.

## 1.7.7
* Fixed warning in Android: new `NativeEventEmitter()` was called with a non-null argument without the required method.

## 1.7.6
* Improved TypeScript definitions
* Updated to latest iOS + Android SDKs

## 1.7.5
* Implemented `setShowNotificationsInForeground` method
* Fixed `received` event for Android

## 1.7.4
* Fixed previous release

## 1.7.3
* Updated to latest iOS + Android SDKs
* Implemented `getSubscriptionId`

## 1.7.2
* Updated to latest iOS + Android SDKs
* Fix for Android targetSdkVersion >= 34

## 1.7.1
* Implemented `setAutoResubscribe`

## 1.7.0
* Updated to latest iOS + Android SDKs

## 1.6.1
* Fixed `trackEvent` method on Android

## 1.6.0
* Added `trackEvent` method
* Updated native SDKs

## 1.5.5
* Hotfix for build failure on Android

## 1.5.4
* Hotfix for notification custom data payload for Android

## 1.5.3
* Hotfix for notification custom data payload for Android

## 1.5.2
* Hotfix for `areNotificationsEnabled` method on iOS

## 1.5.1
* Added `areNotificationsEnabled` method

## 1.5.0
* Updated to latest iOS + Android SDKs
* Implemented new topic-related methods
* Fixed callback arguments for some Android methods

## 1.4.11
* Updated to latest iOS + Android SDKs

## 1.4.10
* Updated to latest Android SDK

## 1.4.9
* Updated to latest iOS SDK

## 1.4.8
* Updated to latest iOS SDK

## 1.4.7
* Fixed Notification Opened handler for iOS when application was not open in the background

## 1.4.6
* Fixed setSubscriptionAttribute on Android

## 1.4.5
* Updated to latest iOS SDK

## 1.4.4
* Updated to latest Android & iOS SDKs

## 1.4.3
* Android: Updated native SDK to latest version to reflect latest Android 12 changes

## 1.4.2
* Resolved crash when opening notifications on iOS

## 1.4.1
* Fixed `trackPageView` on Android
* Updated native iOS SDK

## 1.4.0

* Updated native Android & iOS SDKs
* Implemented `trackPageView` method
