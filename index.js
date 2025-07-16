import { NativeModules, NativeEventEmitter, NetInfo, Platform } from 'react-native';
import invariant from 'invariant';

const RNCleverPush = NativeModules.CleverPush;
const cleverPushEventEmitter = new NativeEventEmitter(RNCleverPush);

function checkIfInitialized() {
  return RNCleverPush != null;
}

export default class CleverPush {

  static init(channelId, options) {
    RNCleverPush.init(Object.assign({ channelId }, options));
  }

  static addSubscribedListener(listener) {
    return cleverPushEventEmitter.addListener('CleverPush-subscribed', listener);
  }

  static addNotificationOpenedListener(listener) {
    return cleverPushEventEmitter.addListener('CleverPush-notificationOpened', listener);
  }

  static addNotificationReceivedListener(listener) {
    return cleverPushEventEmitter.addListener('CleverPush-notificationReceived', listener);
  }

  static addAppBannerOpenedListener(listener) {
    return cleverPushEventEmitter.addListener('CleverPush-appBannerOpened', listener);
  }

  static removeEventListener(subscription) {
    if (subscription && typeof subscription.remove === 'function') {
      subscription.remove();
    }
  }

  static getAvailableTags(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getAvailableTags(callback);
  }

  static getAvailableTopics(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getAvailableTopics(callback);
  }

  static getAvailableAttributes(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getAvailableAttributes(callback);
  }

  static addSubscriptionTag(tagId) {
    if (!checkIfInitialized()) return;

    RNCleverPush.addSubscriptionTag(tagId);
  }

  static removeSubscriptionTag(tagId) {
    if (!checkIfInitialized()) return;

    RNCleverPush.removeSubscriptionTag(tagId);
  }

  static getSubscriptionTags(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getSubscriptionTags(callback);
  }

  static hasSubscriptionTag(tagId, callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.hasSubscriptionTag(tagId, callback);
  }

  static getSubscriptionTopics(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getSubscriptionTopics(callback);
  }

  static setSubscriptionTopics(topicIds) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setSubscriptionTopics(topicIds);
  }

  static addSubscriptionTopic(topicId) {
    if (!checkIfInitialized()) return;

    RNCleverPush.addSubscriptionTopic(topicId);
  }

  static removeSubscriptionTopic(topicId) {
    if (!checkIfInitialized()) return;

    RNCleverPush.removeSubscriptionTopic(topicId);
  }

  static getSubscriptionAttributes(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getSubscriptionAttributes(callback);
  }

  static getSubscriptionAttribute(attributeId, callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getSubscriptionAttribute(attributeId, callback);
  }

  static setSubscriptionAttribute(attributeId, value) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setSubscriptionAttribute(attributeId, value);
  }

  static setSubscriptionLanguage(value) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setSubscriptionLanguage(value);
  }

  static setSubscriptionCountry(value) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setSubscriptionCountry(value);
  }

  static isSubscribed(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.isSubscribed(callback);
  }

  static getSubscriptionId(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getSubscriptionId(callback);
  }

  static subscribe() {
    if (!checkIfInitialized()) return;

    RNCleverPush.subscribe();
  }

  static unsubscribe() {
    if (!checkIfInitialized()) return;

    RNCleverPush.unsubscribe();
  }

  static areNotificationsEnabled(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.areNotificationsEnabled(callback);
  }

  static showTopicsDialog() {
    if (!checkIfInitialized()) return;

    RNCleverPush.showTopicsDialog();
  }

  static enableDevelopmentMode() {
    if (!checkIfInitialized()) return;

    RNCleverPush.enableDevelopmentMode();
  }

  static getNotifications(callback) {
    if (!checkIfInitialized()) return;

    return RNCleverPush.getNotifications(callback);
  }

  static showAppBanners(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.showAppBanners(callback);
  }

  static requestLocationPermission() {
    if (!checkIfInitialized()) return;

    RNCleverPush.requestLocationPermission();
  }

  static trackPageView(url, params) {
    if (!checkIfInitialized()) return;

    RNCleverPush.trackPageView(url, params);
  }

  static trackEvent(url, properties) {
    if (!checkIfInitialized()) return;

    RNCleverPush.trackEvent(url, properties);
  }

  static setAutoResubscribe(autoResubscribe) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setAutoResubscribe(autoResubscribe);
  }

  static setShowNotificationsInForeground(show) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setShowNotificationsInForeground(show);
  }

  // iOS only
  static setAutoClearBadge(autoClear) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setAutoClearBadge(autoClear);
  }

  // iOS only
  static setIncrementBadge(increment) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setIncrementBadge(increment);
  }

  static setBadgeCount(count) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setBadgeCount(count);
  }

  static getBadgeCount(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getBadgeCount(callback);
  }

  static clearNotificationsFromNotificationCenter() {
    if (!checkIfInitialized()) return;

    RNCleverPush.clearNotificationsFromNotificationCenter();
  }

  static removeNotification(notificationId, removeFromNotificationCenter = false) {
    if (!checkIfInitialized()) return;

    if (removeFromNotificationCenter) {
      RNCleverPush.removeNotificationWithCenter(notificationId, removeFromNotificationCenter);
    } else {
      RNCleverPush.removeNotification(notificationId);
    }
  }

  static enableAppBanners() {
    if (!checkIfInitialized()) return;

    RNCleverPush.enableAppBanners();
  }

  static disableAppBanners() {
    if (!checkIfInitialized()) return;

    RNCleverPush.disableAppBanners();
  }

}
