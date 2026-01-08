import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_service.dart';
import 'auth_service.dart';
import '../models/budget_model.dart';

/// Service để quản lý ngân sách
class BudgetService {
  static final AuthService _authService = AuthService();

  /// Tạo ngân sách mới
  static Future<BudgetResponse> createBudget(BudgetRequest request) async {
    try {
      final token = await _authService.getToken();

      final response = await http.post(
        Uri.parse('${ApiService.baseUrl}/budgets'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return BudgetResponse.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        throw ApiException(
          message: 'API trả về lỗi ${response.statusCode}: ${response.body}',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Lỗi khi tạo ngân sách: $e',
        type: 'connection',
      );
    }
  }

  /// Lấy danh sách ngân sách
  static Future<List<BudgetResponse>> getAllBudgets() async {
    try {
      final token = await _authService.getToken();

      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/budgets'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        if (data is List) {
          return data
              .map((e) => BudgetResponse.fromJson(e as Map<String, dynamic>))
              .toList();
        } else if (data is Map && data['content'] != null) {
          return (data['content'] as List)
              .map((e) => BudgetResponse.fromJson(e as Map<String, dynamic>))
              .toList();
        }
        return [];
      } else {
        throw ApiException(
          message: 'API trả về lỗi ${response.statusCode}: ${response.body}',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Lỗi khi lấy danh sách ngân sách: $e',
        type: 'connection',
      );
    }
  }

  /// Lấy chi tiết ngân sách theo ID
  static Future<BudgetResponse> getBudgetById(int id) async {
    try {
      final token = await _authService.getToken();

      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/budgets/$id'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        return BudgetResponse.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        throw ApiException(
          message: 'API trả về lỗi ${response.statusCode}: ${response.body}',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Lỗi khi lấy chi tiết ngân sách: $e',
        type: 'connection',
      );
    }
  }

  /// Cập nhật ngân sách
  static Future<BudgetResponse> updateBudget(
    int id,
    BudgetRequest request,
  ) async {
    try {
      final token = await _authService.getToken();

      final response = await http.put(
        Uri.parse('${ApiService.baseUrl}/budgets/$id'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        return BudgetResponse.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        throw ApiException(
          message: 'API trả về lỗi ${response.statusCode}: ${response.body}',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Lỗi khi cập nhật ngân sách: $e',
        type: 'connection',
      );
    }
  }

  /// Xóa ngân sách
  static Future<void> deleteBudget(int id) async {
    try {
      final token = await _authService.getToken();

      final response = await http.delete(
        Uri.parse('${ApiService.baseUrl}/budgets/$id'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw ApiException(
          message: 'API trả về lỗi ${response.statusCode}: ${response.body}',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Lỗi khi xóa ngân sách: $e',
        type: 'connection',
      );
    }
  }
}
