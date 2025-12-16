import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_service.dart';
import 'storage_service.dart';

/// User Request Service - Xử lý API yêu cầu của người dùng
///
/// API Endpoints:
/// - POST /api/user-requests - Tạo yêu cầu mới (xuất dữ liệu, xóa tài khoản)
/// - GET /api/user-requests/my-requests - Lấy yêu cầu của tôi

/// Loại yêu cầu
enum UserRequestType {
  exportData('EXPORT_DATA'),
  deleteAccount('DELETE_ACCOUNT');

  final String value;
  const UserRequestType(this.value);
}

/// Trạng thái yêu cầu
enum UserRequestStatus {
  pending('PENDING'),
  approved('APPROVED'),
  rejected('REJECTED');

  final String value;
  const UserRequestStatus(this.value);

  static UserRequestStatus fromString(String value) {
    return UserRequestStatus.values.firstWhere(
      (e) => e.value == value,
      orElse: () => UserRequestStatus.pending,
    );
  }
}

/// Model yêu cầu của người dùng
class UserRequest {
  final int id;
  final int userId;
  final UserRequestType requestType;
  final UserRequestStatus status;
  final String? reason;
  final String? adminNote;
  final DateTime createdAt;
  final DateTime? processedAt;

  UserRequest({
    required this.id,
    required this.userId,
    required this.requestType,
    required this.status,
    this.reason,
    this.adminNote,
    required this.createdAt,
    this.processedAt,
  });

  factory UserRequest.fromJson(Map<String, dynamic> json) {
    return UserRequest(
      id: json['id'] as int,
      userId: json['userId'] as int,
      requestType: json['requestType'] == 'EXPORT_DATA'
          ? UserRequestType.exportData
          : UserRequestType.deleteAccount,
      status: UserRequestStatus.fromString(json['status'] as String),
      reason: json['reason'] as String?,
      adminNote: json['adminNote'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      processedAt: json['processedAt'] != null
          ? DateTime.parse(json['processedAt'] as String)
          : null,
    );
  }

  String get statusText {
    switch (status) {
      case UserRequestStatus.pending:
        return 'Đang chờ xử lý';
      case UserRequestStatus.approved:
        return 'Đã duyệt';
      case UserRequestStatus.rejected:
        return 'Đã từ chối';
    }
  }

  String get requestTypeText {
    switch (requestType) {
      case UserRequestType.exportData:
        return 'Xuất dữ liệu';
      case UserRequestType.deleteAccount:
        return 'Xóa tài khoản';
    }
  }
}

class UserRequestService {
  static const String baseUrl = 'http://175.41.150.228:8080/api';
  final StorageService _storageService = StorageService();

  static final UserRequestService _instance = UserRequestService._internal();
  factory UserRequestService() => _instance;
  UserRequestService._internal();

  /// Lấy headers với token
  Future<Map<String, String>> _getHeaders() async {
    final token = await _storageService.getToken();
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }

  /// Tạo yêu cầu xuất dữ liệu
  Future<UserRequest> requestExportData({String? reason}) async {
    return _createRequest(UserRequestType.exportData, reason: reason);
  }

  /// Tạo yêu cầu xóa tài khoản
  Future<UserRequest> requestDeleteAccount({String? reason}) async {
    return _createRequest(UserRequestType.deleteAccount, reason: reason);
  }

  /// Tạo yêu cầu mới
  Future<UserRequest> _createRequest(
    UserRequestType requestType, {
    String? reason,
  }) async {
    try {
      final headers = await _getHeaders();
      final body = {
        'requestType': requestType.value,
        if (reason != null && reason.isNotEmpty) 'reason': reason,
      };

      final response = await http.post(
        Uri.parse('$baseUrl/user-requests'),
        headers: headers,
        body: jsonEncode(body),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return UserRequest.fromJson(jsonDecode(response.body));
      } else {
        final error = jsonDecode(response.body);
        throw ApiException(
          message: error['message'] ?? 'Không thể gửi yêu cầu',
          statusCode: response.statusCode,
          type: 'validation',
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message:
            'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.',
        type: 'connection',
      );
    }
  }

  /// Lấy danh sách yêu cầu của tôi
  Future<List<UserRequest>> getMyRequests() async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/user-requests/my-requests'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        // API có thể trả về list trực tiếp hoặc trong một field
        final List<dynamic> list = data is List
            ? data
            : (data['content'] ?? []);
        return list.map((json) => UserRequest.fromJson(json)).toList();
      } else {
        throw ApiException(
          message: 'Không thể tải danh sách yêu cầu',
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

  /// Kiểm tra xem có yêu cầu đang pending không
  Future<bool> hasPendingRequest(UserRequestType type) async {
    try {
      final requests = await getMyRequests();
      return requests.any(
        (r) => r.requestType == type && r.status == UserRequestStatus.pending,
      );
    } catch (e) {
      return false;
    }
  }
}
