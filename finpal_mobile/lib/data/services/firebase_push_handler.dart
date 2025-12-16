import 'package:flutter/foundation.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'fcm_service.dart';

/// Firebase Push Notification Handler
///
/// Quản lý Firebase Cloud Messaging cho push notifications.
///
/// Cách sử dụng:
/// 1. Thêm firebase_core và firebase_messaging vào pubspec.yaml
/// 2. Cấu hình Firebase trong firebase_options.dart
/// 3. Gọi FirebasePushHandler.initialize() trong main()
/// 4. Gọi FirebasePushHandler.registerToken() sau khi user đăng nhập

class FirebasePushHandler {
  static final FirebasePushHandler _instance = FirebasePushHandler._internal();
  factory FirebasePushHandler() => _instance;
  FirebasePushHandler._internal();

  final FcmService _fcmService = FcmService();

  // Callback khi nhận notification foreground
  Function(Map<String, dynamic>)? onForegroundMessage;

  // Callback khi tap notification
  Function(Map<String, dynamic>)? onNotificationTap;

  // Callback khi cần navigate
  Function(String)? onNavigate;

  /// Khởi tạo Firebase Messaging
  /// Gọi method này trong main() sau Firebase.initializeApp()
  Future<void> initialize() async {
    try {
      // Request permission (iOS)
      NotificationSettings settings = await FirebaseMessaging.instance
          .requestPermission(
            alert: true,
            badge: true,
            sound: true,
            provisional: false,
          );

      if (kDebugMode) {
        print(
          '🔔 User notification permission: ${settings.authorizationStatus}',
        );
      }

      // Foreground message handler
      FirebaseMessaging.onMessage.listen((RemoteMessage message) {
        if (kDebugMode) {
          print('📩 Foreground message: ${message.notification?.title}');
        }

        if (onForegroundMessage != null) {
          onForegroundMessage!({
            'title': message.notification?.title,
            'body': message.notification?.body,
            'data': message.data,
          });
        }
      });

      // Background/Terminated - tap notification
      FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
        if (kDebugMode) {
          print('📲 Notification tapped: ${message.data}');
        }

        if (onNotificationTap != null) {
          onNotificationTap!(message.data);
        }

        _handleNotificationNavigation(message.data);
      });

      // Check if app was opened from a notification
      RemoteMessage? initialMessage = await FirebaseMessaging.instance
          .getInitialMessage();
      if (initialMessage != null) {
        _handleNotificationNavigation(initialMessage.data);
      }

      if (kDebugMode) {
        print('✅ Firebase Push Handler initialized successfully');
      }
    } catch (e) {
      if (kDebugMode) {
        print('❌ Error initializing Firebase Push Handler: $e');
      }
    }
  }

  /// Đăng ký FCM token với backend
  /// Gọi method này sau khi user đăng nhập thành công
  Future<bool> registerToken() async {
    try {
      String? token = await FirebaseMessaging.instance.getToken();

      if (token != null) {
        if (kDebugMode) {
          print('📱 FCM Token: ${token.substring(0, 20)}...');
        }

        bool success = await _fcmService.registerFcmToken(token);

        if (success) {
          if (kDebugMode) {
            print('✅ FCM token registered with backend');
          }
        }

        // Listen for token refresh
        FirebaseMessaging.instance.onTokenRefresh.listen((newToken) async {
          if (kDebugMode) {
            print('🔄 FCM Token refreshed');
          }
          await _fcmService.registerFcmToken(newToken);
        });

        return success;
      }

      return false;
    } catch (e) {
      if (kDebugMode) {
        print('❌ Error registering FCM token: $e');
      }
      return false;
    }
  }

  /// Hủy đăng ký FCM token (khi logout)
  Future<bool> unregisterToken() async {
    try {
      await FirebaseMessaging.instance.deleteToken();
      return await _fcmService.unregisterFcmToken();
    } catch (e) {
      if (kDebugMode) {
        print('❌ Error unregistering FCM token: $e');
      }
      return false;
    }
  }

  /// Xử lý navigation khi tap notification
  void _handleNotificationNavigation(Map<String, dynamic> data) {
    String? type = data['type'];
    String? actionUrl = data['actionUrl'];

    String route;

    switch (type) {
      case 'BUDGET_ALERT':
        route = '/budgets';
        break;
      case 'SAVINGS_SUGGESTION':
      case 'SPENDING_INSIGHT':
        route = '/insights';
        break;
      case 'GOAL_REMINDER':
        route = '/savings-goals';
        break;
      case 'ANOMALY_ALERT':
      case 'TRANSACTION':
        route = '/transactions';
        break;
      default:
        route = actionUrl ?? '/notifications';
    }

    if (onNavigate != null) {
      onNavigate!(route);
    }
  }

  /// Hiển thị local notification (optional)
  Future<void> showLocalNotification({
    required String title,
    required String body,
    Map<String, dynamic>? data,
  }) async {
    // FCM tự động hiển thị notification khi app ở background
    // Method này dùng cho custom foreground notifications nếu cần
    if (kDebugMode) {
      print('📢 Local Notification: $title - $body');
    }
  }
}
