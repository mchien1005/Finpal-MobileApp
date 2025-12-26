import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/notification_model.dart';
import 'api_service.dart';
import 'storage_service.dart';

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
  /// [types] - danh sách các loại thông báo cần lấy
  Future<List<NotificationModel>> getNotifications({
    bool? isRead,
    List<String>? types,
  }) async {
    try {
      final headers = await _getHeaders();
      String url = '$baseUrl/notifications';

      final queryParams = <String>[];
      if (isRead != null) {
        queryParams.add('isRead=$isRead');
      }
      if (types != null && types.isNotEmpty) {
        for (var type in types) {
          queryParams.add('type=$type');
        }
      }

      if (queryParams.isNotEmpty) {
        url += '?${queryParams.join('&')}';
      }

      print('🔔 Notification List: GET $url');
      print('🔑 Headers: $headers');

      final response = await http.get(Uri.parse(url), headers: headers);

      print('📊 Notification List Response Status: ${response.statusCode}');
      print('📄 Notification List Response Body: ${response.body}');

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        print('✅ Notification List: Found ${data.length} notifications');
        return data.map((json) => NotificationModel.fromJson(json)).toList();
      } else {
        print(
          '❌ Notification List Error: ${response.statusCode} - ${response.body}',
        );
        throw ApiException(
          message: 'Không thể tải thông báo (${response.statusCode})',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      print('❌ Notification List Exception: $e');
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server notification: ${e.toString()}',
        type: 'connection',
      );
    }
  }

  /// Lấy số lượng thông báo chưa đọc
  Future<int> getUnreadCount() async {
    try {
      final headers = await _getHeaders();
      final url = '$baseUrl/notifications/unread-count';
      print('🔔 Notification: GET $url');
      print('🔑 Headers: $headers');

      final response = await http.get(Uri.parse(url), headers: headers);

      print('📊 Notification Response Status: ${response.statusCode}');
      print('📄 Notification Response Body: ${response.body}');

      if (response.statusCode == 200) {
        try {
          // API trả về JSON object {"unreadCount": 106}
          final data = jsonDecode(response.body);
          final count = data['unreadCount'] as int? ?? 0;
          print('✅ Notification: Unread count = $count');
          return count;
        } catch (parseError) {
          print(
            '⚠️ Notification: Failed to parse unread count, defaulting to 0',
          );
          print('⚠️ Parse error: $parseError');
          return 0;
        }
      } else {
        print(
          '❌ Notification Error: ${response.statusCode} - ${response.body}',
        );
        throw ApiException(
          message:
              'Không thể lấy số thông báo chưa đọc (${response.statusCode})',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      print('❌ Notification Exception: $e');
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server notification: ${e.toString()}',
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
