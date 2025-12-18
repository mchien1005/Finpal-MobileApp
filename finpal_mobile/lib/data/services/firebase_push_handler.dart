import 'package:flutter/foundation.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'fcm_service.dart';
import 'auth_service.dart';

/// Firebase Push Notification Handler
///
/// Quản lý Firebase Cloud Messaging và Local Notifications.
class FirebasePushHandler {
  static final FirebasePushHandler _instance = FirebasePushHandler._internal();
  factory FirebasePushHandler() => _instance;
  FirebasePushHandler._internal();

  final FcmService _fcmService = FcmService();
  final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  // Channel ID cho high priority notification
  static const AndroidNotificationChannel channel = AndroidNotificationChannel(
    'high_importance_channel', // id trùng với backend
    'High Importance Notifications', // title
    description:
        'This channel is used for important notifications.', // description
    importance: Importance.max,
    showBadge: true,
    playSound: true,
  );

  // Callback khi nhận notification foreground
  Function(Map<String, dynamic>)? onForegroundMessage;

  // Callback khi tap notification
  Function(Map<String, dynamic>)? onNotificationTap;

  // Callback khi cần navigate
  Function(String)? onNavigate;

  // Debounce duplicate messages
  String? _lastMessageId;
  DateTime? _lastMessageTime;

  /// Khởi tạo Firebase Messaging
  /// Gọi method này trong main() sau Firebase.initializeApp()
  Future<void> initialize() async {
    try {
      // 1. Initialize Local Notifications
      const AndroidInitializationSettings initializationSettingsAndroid =
          AndroidInitializationSettings('@mipmap/ic_launcher');

      const DarwinInitializationSettings initializationSettingsDarwin =
          DarwinInitializationSettings(
            requestAlertPermission: true,
            requestBadgePermission: true,
            requestSoundPermission: true,
          );

      const InitializationSettings initializationSettings =
          InitializationSettings(
            android: initializationSettingsAndroid,
            iOS: initializationSettingsDarwin,
          );

      await flutterLocalNotificationsPlugin.initialize(
        initializationSettings,
        onDidReceiveNotificationResponse: (NotificationResponse response) {
          if (response.payload != null) {
            // TODO: Handle local notification tap
            if (kDebugMode)
              print('Local Notification Tapped: ${response.payload}');
          }
        },
      );

      // Create the channel on the device (Android specific)
      await flutterLocalNotificationsPlugin
          .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin
          >()
          ?.createNotificationChannel(channel);

      // 2. Setup Firebase Messaging

      // Check initial message (Terminated state)
      RemoteMessage? initialMessage = await FirebaseMessaging.instance
          .getInitialMessage();
      if (initialMessage != null) {
        _handleNotificationNavigation(initialMessage.data);
      }

      // Tap background/terminated
      FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
        _handleNotificationNavigation(message.data);
      });

      // Request Permission
      NotificationSettings settings = await FirebaseMessaging.instance
          .requestPermission(
            alert: true,
            badge: true,
            sound: true,
            provisional: false,
          );

      // Cấu hình Foreground Presentation Options
      // - Android: false (vì ta tự show local notification)
      // - iOS: true (để iOS tự show popup)
      await FirebaseMessaging.instance
          .setForegroundNotificationPresentationOptions(
            alert: true, // iOS needs this true to show heads-up in foreground
            badge: true,
            sound: true,
          );

      if (kDebugMode) {
        print('🔔 Permission status: ${settings.authorizationStatus}');
      }

      // 3. Listen to Foreground Messages
      FirebaseMessaging.onMessage.listen((RemoteMessage message) async {
        // Chỉ hiển thị thông báo nếu user đang đăng nhập
        bool isLoggedIn = await AuthService().isLoggedIn();
        if (!isLoggedIn) {
          if (kDebugMode) print('⛔ User logged out. Skipping notification.');
          return;
        }

        // Check Duplicate (Debounce 3s)
        final now = DateTime.now();
        // Fallback dùng content nếu không có messageId
        final uniqueId =
            message.messageId ??
            '${message.notification?.title}|${message.notification?.body}|${message.sentTime}';

        if (_lastMessageId == uniqueId &&
            _lastMessageTime != null &&
            now.difference(_lastMessageTime!).inSeconds < 5) {
          if (kDebugMode) print('⚠️ Ignored duplicate notification: $uniqueId');
          return;
        }

        _lastMessageId = uniqueId;
        _lastMessageTime = now;

        RemoteNotification? notification = message.notification;
        AndroidNotification? android = message.notification?.android;

        // Nếu là Android và có notification payload -> Show Local Notification
        if (notification != null && android != null && !kIsWeb) {
          flutterLocalNotificationsPlugin.show(
            notification.hashCode,
            notification.title,
            notification.body,
            NotificationDetails(
              android: AndroidNotificationDetails(
                channel.id,
                channel.name,
                channelDescription: channel.description,
                icon: '@mipmap/ic_launcher', // hoặc icon khác
                importance: Importance.max,
                priority: Priority.high,
                fullScreenIntent: true, // Heads-up notification
                // parse payload từ message.data để dùng khi tap
                // styleInformation: BigTextStyleInformation(''), // Expandable text
              ),
            ),
            payload: message.data.toString(), // Truyền data payload vào
          );
        }

        // Callback UI updates
        if (onForegroundMessage != null) {
          onForegroundMessage!({
            'title': notification?.title,
            'body': notification?.body,
            'data': message.data,
          });
        }
      });

      // Token Refresh
      FirebaseMessaging.instance.onTokenRefresh.listen((fcmToken) {
        registerToken(token: fcmToken);
      });
    } catch (e) {
      print('❌ Error initializing Firebase/Local Notifications: $e');
    }
  }

  Future<void> registerToken({String? token}) async {
    try {
      String? fcmToken = token ?? await FirebaseMessaging.instance.getToken();
      if (fcmToken != null) {
        await _fcmService.registerFcmToken(fcmToken);
      }
    } catch (e) {
      print('❌ Error registering token: $e');
    }
  }

  Future<void> unregisterToken() async {
    try {
      await _fcmService.unregisterFcmToken();
    } catch (e) {
      print('❌ Error unregistering token: $e');
    }
  }

  void _handleNotificationNavigation(Map<String, dynamic> data) {
    if (data.isEmpty) return;
    if (onNotificationTap != null) onNotificationTap!(data);

    String? type = data['type'];
    if (type == 'SAVINGS_SUGGESTION') {
      if (onNavigate != null) onNavigate!('/insights');
    }
  }
}
