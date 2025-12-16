import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/notification_model.dart';
import 'api_service.dart';
import 'storage_service.dart';

/// Notification Service - Xử lý API thông báo
///
/// API Endpoints:
/// - GET /api/notifications - Lấy danh sách thông báo
/// - GET /api/notifications/unread-count - Đếm thông báo chưa đọc
/// - PUT /api/notifications/{id}/read - Đánh dấu đã đọc
/// - PUT /api/notifications/read-all - Đánh dấu tất cả đã đọc
/// - DELETE /api/notifications/{id} - Xóa thông báo
/// - DELETE /api/notifications/read - Xóa tất cả đã đọc

class NotificationService {
  static const String baseUrl = 'http://175.41.150.228:8080/api';
  final StorageService _storageService = StorageService();

  static final NotificationService _instance = NotificationService._internal();
  factory NotificationService() => _instance;
  NotificationService._internal();

  /// Lấy headers với token
  Future<Map<String, String>> _getHeaders() async {
    final token = await _storageService.getToken();
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }

  /// Lấy danh sách thông báo
  /// [isRead] - null: tất cả, true: đã đọc, false: chưa đọc
  Future<List<NotificationModel>> getNotifications({bool? isRead}) async {
    try {
      final headers = await _getHeaders();
      String url = '$baseUrl/notifications';

      if (isRead != null) {
        url += '?isRead=$isRead';
      }

      final response = await http.get(Uri.parse(url), headers: headers);

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => NotificationModel.fromJson(json)).toList();
      } else {
        throw ApiException(
          message: 'Không thể tải thông báo',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server',
        type: 'connection',
      );
    }
  }

  /// Lấy số lượng thông báo chưa đọc
  Future<int> getUnreadCount() async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/notifications/unread-count'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        return int.parse(response.body);
      } else {
        throw ApiException(
          message: 'Không thể lấy số thông báo chưa đọc',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server',
        type: 'connection',
      );
    }
  }

  /// Đánh dấu thông báo là đã đọc
  Future<NotificationModel> markAsRead(int notificationId) async {
    try {
      final headers = await _getHeaders();
      final response = await http.put(
        Uri.parse('$baseUrl/notifications/$notificationId/read'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        return NotificationModel.fromJson(jsonDecode(response.body));
      } else {
        throw ApiException(
          message: 'Không thể đánh dấu đã đọc',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server',
        type: 'connection',
      );
    }
  }

  /// Đánh dấu tất cả thông báo là đã đọc
  Future<void> markAllAsRead() async {
    try {
      final headers = await _getHeaders();
      final response = await http.put(
        Uri.parse('$baseUrl/notifications/read-all'),
        headers: headers,
      );

      if (response.statusCode != 200) {
        throw ApiException(
          message: 'Không thể đánh dấu tất cả đã đọc',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server',
        type: 'connection',
      );
    }
  }

  /// Xóa một thông báo
  Future<void> deleteNotification(int notificationId) async {
    try {
      final headers = await _getHeaders();
      final response = await http.delete(
        Uri.parse('$baseUrl/notifications/$notificationId'),
        headers: headers,
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw ApiException(
          message: 'Không thể xóa thông báo',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server',
        type: 'connection',
      );
    }
  }

  /// Xóa tất cả thông báo đã đọc
  Future<void> deleteAllRead() async {
    try {
      final headers = await _getHeaders();
      final response = await http.delete(
        Uri.parse('$baseUrl/notifications/read'),
        headers: headers,
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw ApiException(
          message: 'Không thể xóa thông báo đã đọc',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server',
        type: 'connection',
      );
    }
  }
}
