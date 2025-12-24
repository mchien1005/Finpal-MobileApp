import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'presentation/screens/splash/splash_screen.dart';
import 'data/services/firebase_push_handler.dart';
import 'core/utils/bottom_nav_helper.dart';

/// Background message handler - phải là top-level function
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp();
  print('🔔 Background message received: ${message.notification?.title}');
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize Firebase
  await Firebase.initializeApp();

  // Set up background message handler
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

  // Initialize push handler
  final pushHandler = FirebasePushHandler();
  await pushHandler.initialize();

  // Reset back press time khi app khởi động
  BottomNavHelper.resetBackPressTime();

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Finpal',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFFD7006E)),
        useMaterial3: true,
      ),
      home: const SplashScreen(),
    );
  }
}
