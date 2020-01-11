import { NativeModules, NativeEventEmitter, NetInfo, Platform } from 'react-native';
import invariant from 'invariant';

const RNCleverPush = NativeModules.CleverPush;

const eventBroadcastNames = [
  'CleverPush-notificationOpened',
  'CleverPush-subscribed'
];

var CleverPushEventEmitter;

var _eventNames = ['opened', 'subscribed'];

var _notificationHandler = new Map();
var _notificationCache = new Map();
var _listeners = [];

if (RNCleverPush != null) {
  CleverPushEventEmitter = new NativeEventEmitter(RNCleverPush);

  for (var i = 0; i < eventBroadcastNames.length; i++) {
    var eventBroadcastName = eventBroadcastNames[i];
    var eventName = _eventNames[i];

    _listeners[eventName] = handleEventBroadcast(eventName, eventBroadcastName)
  }
}

function handleEventBroadcast(type, broadcast) {
  return CleverPushEventEmitter.addListener(
    broadcast, (notification) => {
      var handler = _notificationHandler.get(type);

      if (handler) {
        handler(notification);
      } else {
        _notificationCache.set(type, notification);
      }
    }
  );
}

function checkIfInitialized() {
  return RNCleverPush != null;
}

export default class CleverPush {
  static addEventListener(type, handler) {
    if (!checkIfInitialized()) return;

    invariant(
      type === 'opened' || type === 'subscribed',
      'CleverPush only supports `opened`, and `subscribed` events'
    );

    _notificationHandler.set(type, handler);

    var cache = _notificationCache.get(type);
    if (handler && cache) {
      handler(cache);
      _notificationCache.delete(type);
    }
  }

  static removeEventListener(type, handler) {
    if (!checkIfInitialized()) return;

    invariant(
      type === 'opened' || type === 'subscribed',
      'CleverPush only supports `opened`, and `subscribed` events'
    );

    _notificationHandler.delete(type);
  }

  static clearListeners() {
    if (!checkIfInitialized()) return;

    for (var i = 0; i < _eventNames.length; i++) {
      _listeners[_eventNames].remove();
    }
  }

  static init(channelId, options) {
    RNCleverPush.init(Object.assign({ channelId }, options));
  }

  static getAvailableTags(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.getAvailableTags(callback);
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

  static subscribe() {
    if (!checkIfInitialized()) return;

    RNCleverPush.subscribe();
  }

  static unsubscribe() {
    if (!checkIfInitialized()) return;

    RNCleverPush.unsubscribe();
  }

  static showTopicsDialog() {
    if (!checkIfInitialized()) return;

    RNCleverPush.showTopicsDialog();
  }

  static getNotifications(callback) {
    if (!checkIfInitialized()) return;

    return RNCleverPush.getNotifications(callback);
  }

  static showAppBanners(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.showAppBanners(callback);
  }

  // iOS only
  static setAutoClearBadge(callback) {
    if (!checkIfInitialized()) return;

    RNCleverPush.setAutoClearBadge(callback);
  }
}
