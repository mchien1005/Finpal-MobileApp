import 'dart:convert';
import 'package:http/http.dart' as http;
import 'storage_service.dart';

/// FCM Service - Xử lý Firebase Cloud Messaging
///
/// Chức năng:
/// - Đăng ký FCM token với backend
/// - Hủy đăng ký khi logout
/// - Quản lý notification settings

class FcmService {
  static const String baseUrl = 'http://175.41.150.228:8080/api';
  final StorageService _storageService = StorageService();

  static final FcmService _instance = FcmService._internal();
  factory FcmService() => _instance;
  FcmService._internal();

  /// Lấy headers với token
  Future<Map<String, String>> _getHeaders() async {
    final token = await _storageService.getToken();
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }

  /// Đăng ký FCM token với backend
  /// Gọi method này khi:
  /// - User đăng nhập thành công
  /// - App được mở lại
  /// - FCM token được refresh
  Future<bool> registerFcmToken(String fcmToken) async {
    try {
      final headers = await _getHeaders();
      final response = await http.post(
        Uri.parse('$baseUrl/devices/register-token'),
        headers: headers,
        body: jsonEncode({'fcmToken': fcmToken}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['success'] == true;
      }
      return false;
    } catch (e) {
      print('Error registering FCM token: $e');
      return false;
    }
  }

  /// Hủy đăng ký FCM token (khi user logout)
  Future<bool> unregisterFcmToken() async {
    try {
      final headers = await _getHeaders();
      final response = await http.delete(
        Uri.parse('$baseUrl/devices/unregister-token'),
        headers: headers,
      );

      return response.statusCode == 200;
    } catch (e) {
      print('Error unregistering FCM token: $e');
      return false;
    }
  }

  /// Lấy notification settings
  Future<Map<String, dynamic>?> getNotificationSettings() async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/devices/notification-settings'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      }
      return null;
    } catch (e) {
      print('Error getting notification settings: $e');
      return null;
    }
  }

  /// Cập nhật notification settings (bật/tắt notifications)
  Future<bool> updateNotificationSettings(bool enabled) async {
    try {
      final headers = await _getHeaders();
      final response = await http.put(
        Uri.parse('$baseUrl/devices/notification-settings'),
        headers: headers,
        body: jsonEncode({'notificationEnabled': enabled}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['success'] == true;
      }
      return false;
    } catch (e) {
      print('Error updating notification settings: $e');
      return false;
    }
  }
}
