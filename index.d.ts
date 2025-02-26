declare module 'cleverpush-react-native' {
	export default class CleverPush {
		static init(channelId: string, options?: InitOptions): void;

		static enableDevelopmentMode(): void;
		static requestLocationPermission(): void;
		static isSubscribed(callback: (error, isSubscribed: boolean) => void): void;
		static getSubscriptionId(callback: (error, subscriptionId: string) => void): void;
		static areNotificationsEnabled(callback: (error, notificationsEnabled: boolean) => void): void;
		static subscribe(): void;
		static unsubscribe(): void;
		static showTopicsDialog(): void;
		static setShowNotificationsInForeground(show: boolean): void;
		static setIncrementBadge(increment: boolean): void;
		static setAutoClearBadge(autoClear: boolean): void;
		static setAutoResubscribe(autoResubscribe: boolean): void;
		static setBadgeCount(count: number): void;
		static getBadgeCount(callback: (error, count: number) => void): void;
		static clearNotificationsFromNotificationCenter(): void;

		static addEventListener(
			type: EventType,
			handler: (result: { notification: Notification; subscription: Subscription }) => void
		): void;
		static removeEventListener(
			type: EventType,
			handler: (result: { notification: Notification; subscription: Subscription }) => void
		): void;
		static clearListeners(): void;

		static getAvailableTags(callback: (error, channelTags: Tag[]) => void): void;
		static getSubscriptionTags(callback: (error, tagIds: string[]) => void): void;
		static addSubscriptionTag(tagId: string): void;
		static removeSubscriptionTag(tagId: string): void;
		static hasSubscriptionTag(tagId: string, callback: (error, hasTag: boolean) => void): void;
		static getAvailableAttributes(callback: (error, channelAttribute: Attribute[]) => void): void;
		static getSubscriptionAttributes(callback: (error, attributes: Record<string, string>) => void): void;
		static setSubscriptionAttribute(attributeId: string, value: string): void;
		static getSubscriptionAttribute(attributeId: string, callback: (error, attributeValue: string) => void): void;
		static getSubscriptionTopics(callback: (error, topics: string[]) => void): void;
		static getAvailableTopics(callback: (error, channelTopics: Topic[]) => void): void;
		static setSubscriptionTopics(topicIds: string[]): void;
		static addSubscriptionTopic(topicId: string): void;
		static removeSubscriptionTopic(topicId: string): void;
		static trackEvent(url: string, properties: Record<string, any>): void;
		static trackEvent(url: string): void;

		static setSubscriptionLanguage(value: string): void;
		static setSubscriptionCountry(value: string): void;
		static trackPageView(url: string, params: Record<string, any>): void;

		static getNotifications(callback: (error, notifications: Notification[]) => void): void;
	}

	export type InitOptions = {
		autoRegister?: boolean;
	};

	type EventType = 'received' | 'opened' | 'subscribed' | 'appBannerOpened';

	export interface Tag {
		id: string;
		name: string;
	}

    export interface Topic {
		id: string;
		name: string;
	}

	export interface Attribute {
		id: string;
		name: string;
	}

	export interface Subscription {
		id: string;
	}

	export interface Notification {
		id: string;
		tag: string;
		title: string;
		text: string;
		url: string;
		iconUrl: string;
		mediaUrl: string;
		actions?: NotificationAction[];
		customData: Record<string, any>;
		chatNotification: boolean;
		carouselEnabled: boolean;
		carouselItems?: NotificationCarouselItem[];
		category?: NotificationCategory;
		soundFilename?: string;
		silent: boolean;
		createdAt: string;
		appBanner: string;
		inboxAppBanner?: string;
		voucherCode?: string;
		autoHandleDeepLink?: boolean;
	}

	export interface NotificationAction {
		title: string;
		url: string;
		icon: string;
		phone: string;
		id: string;
		type: string;
	}

	export interface NotificationCategory {
		id: string;
		group: NotificationCategoryGroup;
		name: string;
		description: string;
		soundEnabled: boolean;
		soundFilename: string;
		vibrationEnabled: boolean;
		vibrationPattern: string;
		ledColorEnabled: boolean;
		ledColor: string;
		lockScreen: string;
		importance: string;
		badgeDisabled: boolean;
		backgroundColor: string;
		foregroundColor: string;
	}

	export interface NotificationCategoryGroup {
		id: string;
		name: string;
	}

	export interface NotificationCarouselItem {
		mediaUrl: string;
	}
}