# Hướng dẫn Tích hợp Firebase Push Notifications cho Flutter

## Tổng quan

Tài liệu này hướng dẫn cách tích hợp Firebase Cloud Messaging (FCM) vào FinPal Flutter app để nhận push notifications.

## Bước 1: Thêm App vào Firebase Console

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project **finpal-5ee31**
3. Click **Add app** và chọn:
   - **Android**: Thêm app Android
   - **iOS**: Thêm app iOS

### Cấu hình Android

1. Package name: `com.example.finpal_mobile` (hoặc package name thực tế)
2. Download `google-services.json`
3. Đặt file vào: `android/app/google-services.json`

### Cấu hình iOS

1. Bundle ID: `com.example.finpalMobile` (hoặc bundle ID thực tế)
2. Download `GoogleService-Info.plist`
3. Đặt file vào: `ios/Runner/GoogleService-Info.plist`

## Bước 2: Cấu hình Android

### 2.1. android/build.gradle

```gradle
buildscript {
    dependencies {
        // ... existing dependencies
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

### 2.2. android/app/build.gradle

```gradle
plugins {
    id "com.android.application"
    id "kotlin-android"
    id "dev.flutter.flutter-gradle-plugin"
    id "com.google.gms.google-services"  // Thêm dòng này
}

android {
    defaultConfig {
        // ... existing config
        minSdkVersion 21  // Đảm bảo >= 21
    }
}
```

### 2.3. android/app/src/main/AndroidManifest.xml

```xml
<manifest>
    <application>
        <!-- ... existing content -->

        <!-- FCM Notification Channel -->
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="high_importance_channel" />

        <!-- FCM Default Icon -->
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_icon"
            android:resource="@mipmap/ic_launcher" />
    </application>
</manifest>
```

## Bước 3: Cấu hình iOS

### 3.1. ios/Podfile

Đảm bảo platform iOS >= 12.0:

```ruby
platform :ios, '12.0'
```

### 3.2. Thêm Push Notification Capability

1. Mở Xcode: `open ios/Runner.xcworkspace`
2. Chọn **Runner** > **Signing & Capabilities**
3. Click **+ Capability** > **Push Notifications**
4. Click **+ Capability** > **Background Modes**
5. Check **Remote notifications**

### 3.3. ios/Runner/AppDelegate.swift

```swift
import UIKit
import Flutter
import FirebaseCore
import FirebaseMessaging

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    FirebaseApp.configure()

    // Request notification permissions
    UNUserNotificationCenter.current().delegate = self

    let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
    UNUserNotificationCenter.current().requestAuthorization(
      options: authOptions,
      completionHandler: { _, _ in }
    )

    application.registerForRemoteNotifications()

    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  override func application(_ application: UIApplication,
                          didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    Messaging.messaging().apnsToken = deviceToken
  }
}
```

## Bước 4: Cấu hình Flutter Code

### 4.1. Tạo firebase_options.dart

Chạy FlutterFire CLI:

```bash
# Cài đặt FlutterFire CLI
dart pub global activate flutterfire_cli

# Cấu hình Firebase
cd finpal_mobile
flutterfire configure --project=finpal-5ee31
```

Điều này sẽ tự động tạo file `lib/firebase_options.dart`.

### 4.2. Cập nhật main.dart

```dart
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'firebase_options.dart';
import 'data/services/firebase_push_handler.dart';

// Background message handler (phải là top-level function)
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  print('Handling background message: ${message.messageId}');
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize Firebase
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );

  // Set up background message handler
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

  // Initialize push handler
  final pushHandler = FirebasePushHandler();
  await pushHandler.initialize();

  runApp(const MyApp());
}
```

### 4.3. Uncomment code trong firebase_push_handler.dart

Mở file `lib/data/services/firebase_push_handler.dart` và uncomment các phần có `// TODO: Uncomment khi đã thêm firebase_messaging`:

```dart
import 'package:firebase_messaging/firebase_messaging.dart';

// ... trong initialize():
await FirebaseMessaging.instance.requestPermission(
  alert: true,
  badge: true,
  sound: true,
);

FirebaseMessaging.onMessage.listen((RemoteMessage message) {
  // ... handle foreground message
});

// ... và các phần khác
```

## Bước 5: Test Push Notification

### 5.1. Chạy app và đăng nhập

```bash
cd finpal_mobile
flutter run
```

### 5.2. Kiểm tra FCM token đã đăng ký

Trong logs, bạn sẽ thấy:

```
📱 FCM Token: dXyz123abc456...
✅ FCM token registered with backend
```

### 5.3. Gửi test notification từ Firebase Console

1. Vào [Firebase Console](https://console.firebase.google.com/) > Cloud Messaging
2. Chọn **New notification**
3. Nhập:
   - Title: "Test Notification"
   - Body: "Đây là thông báo test"
4. Target: Single device > Paste FCM token
5. Send

### 5.4. Hoặc chờ scheduler tự động gửi

Backend sẽ tự động gửi notifications theo lịch:

- **Budget Alerts**: Mỗi 4 giờ
- **Goal Reminders**: 8:00 AM hàng ngày
- **Anomaly Detection**: 7:00 PM hàng ngày
- **AI Suggestions**: 9:00 AM Chủ nhật

## Troubleshooting

### 1. Không nhận được notification

- **Android**: Kiểm tra `google-services.json` đúng vị trí
- **iOS**: Kiểm tra provisioning profile có Push Notification capability
- Kiểm tra FCM token đã đăng ký với backend

### 2. Lỗi khi build

```bash
# Android
cd android && ./gradlew clean && cd ..
flutter clean
flutter pub get
flutter run

# iOS
cd ios && pod install --repo-update && cd ..
flutter clean
flutter pub get
flutter run
```

### 3. Token không refresh

Gọi lại `FirebasePushHandler().registerToken()` khi app resume từ background.

## Files đã tạo/cập nhật

| File                                                | Mô tả                          |
| --------------------------------------------------- | ------------------------------ |
| `lib/data/models/notification_model.dart`           | Model cho notification         |
| `lib/data/services/notification_service.dart`       | API service cho notifications  |
| `lib/data/services/fcm_service.dart`                | Service đăng ký FCM token      |
| `lib/data/services/firebase_push_handler.dart`      | Handler cho push notifications |
| `lib/data/services/auth_service.dart`               | Cập nhật tích hợp FCM          |
| `lib/presentation/screens/notification_screen.dart` | UI với API thực                |
| `pubspec.yaml`                                      | Thêm Firebase dependencies     |

## API Endpoints

| Method | Endpoint                             | Mô tả                   |
| ------ | ------------------------------------ | ----------------------- |
| POST   | `/api/devices/register-token`        | Đăng ký FCM token       |
| DELETE | `/api/devices/unregister-token`      | Hủy đăng ký token       |
| GET    | `/api/devices/notification-settings` | Lấy settings            |
| PUT    | `/api/devices/notification-settings` | Cập nhật settings       |
| GET    | `/api/notifications`                 | Lấy danh sách thông báo |
| PUT    | `/api/notifications/{id}/read`       | Đánh dấu đã đọc         |
| PUT    | `/api/notifications/read-all`        | Đánh dấu tất cả đã đọc  |
| DELETE | `/api/notifications/{id}`            | Xóa thông báo           |
| DELETE | `/api/notifications/read`            | Xóa thông báo đã đọc    |

---

**Lưu ý**: Sau khi hoàn thành các bước trên, push notifications sẽ hoạt động hoàn toàn tự động!
