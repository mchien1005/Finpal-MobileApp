import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/savings_goal_model.dart';
import 'api_service.dart';
import 'auth_service.dart';

class SavingsGoalService {
  static final _apiService = ApiService();
  static final _authService = AuthService();
  static const bool _useMockData = false; // Set to true to test without API

  // Get all savings goals
  static Future<List<SavingsGoalResponse>> getAllSavingsGoals({
    String? status,
  }) async {
    // Return mock data for testing when API is not available
    if (_useMockData) {
      await Future.delayed(
        const Duration(seconds: 1),
      ); // Simulate network delay
      return _getMockGoals();
    }

    try {
      String endpoint = '/savings-goals';
      if (status != null) {
        endpoint += '?status=$status';
      }

      final url = '${ApiService.baseUrl}$endpoint';
      final token = await _authService.getToken();

      print('Calling API: $url');
      print('Token available: ${token != null}');

      final response = await http
          .get(
            Uri.parse(url),
            headers: {
              'Content-Type': 'application/json',
              if (token != null) 'Authorization': 'Bearer $token',
            },
          )
          .timeout(const Duration(seconds: 10));

      print('API Response Status: ${response.statusCode}');
      print('API Response Body: ${response.body}');

      if (response.statusCode == 200) {
        final List<dynamic> jsonList = jsonDecode(response.body);
        return jsonList
            .map(
              (json) =>
                  SavingsGoalResponse.fromJson(json as Map<String, dynamic>),
            )
            .toList();
      } else if (response.statusCode == 401) {
        throw ApiException(
          message: 'Bạn cần đăng nhập để xem mục tiêu tiết kiệm',
          statusCode: response.statusCode,
          type: 'validation',
        );
      } else {
        throw ApiException(
          message: 'API trả về lỗi ${response.statusCode}: ${response.body}',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      print('Exception in getAllSavingsGoals: $e');
      if (e is ApiException) rethrow;
      throw ApiException(
        message:
            'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.',
        type: 'connection',
      );
    }
  }

  // Mock data for testing
  static List<SavingsGoalResponse> _getMockGoals() {
    return [
      SavingsGoalResponse(
        id: 1,
        userId: 1,
        name: 'Tai nghe Sony WH-1000XM5',
        description: 'Tai nghe cao cấp chống ồn',
        targetAmount: 3000000,
        currentAmount: 850000,
        deadline: '2025-12-31',
        status: 'ACTIVE',
        createdAt: DateTime.now().subtract(const Duration(days: 30)),
        updatedAt: DateTime.now(),
        progressPercentage: 28.3,
        remainingAmount: 2150000,
        daysRemaining: 40,
        progressStatus: 'ON_TRACK',
        recentContributions: [],
      ),
      SavingsGoalResponse(
        id: 2,
        userId: 1,
        name: 'Du lịch Đà Lạt',
        description: 'Chuyến du lịch gia đình',
        targetAmount: 5000000,
        currentAmount: 2500000,
        deadline: '2026-01-15',
        status: 'ACTIVE',
        createdAt: DateTime.now().subtract(const Duration(days: 60)),
        updatedAt: DateTime.now(),
        progressPercentage: 50.0,
        remainingAmount: 2500000,
        daysRemaining: 55,
        progressStatus: 'ON_TRACK',
        recentContributions: [],
      ),
      SavingsGoalResponse(
        id: 3,
        userId: 1,
        name: 'Laptop MacBook Air',
        description: 'Laptop cho công việc',
        targetAmount: 25000000,
        currentAmount: 8000000,
        deadline: '2026-06-30',
        status: 'ACTIVE',
        createdAt: DateTime.now().subtract(const Duration(days: 90)),
        updatedAt: DateTime.now(),
        progressPercentage: 32.0,
        remainingAmount: 17000000,
        daysRemaining: 221,
        progressStatus: 'ON_TRACK',
        recentContributions: [],
      ),
    ];
  }

  // Get single savings goal by ID
  static Future<SavingsGoalResponse> getSavingsGoalById(int id) async {
    try {
      final token = await _authService.getToken();
      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/savings-goals/$id'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        return SavingsGoalResponse.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        throw ApiException(
          message: 'Failed to load savings goal',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Error loading savings goal: $e',
        type: 'connection',
      );
    }
  }

  // Create new savings goal
  static Future<SavingsGoalResponse> createSavingsGoal(
    SavingsGoalRequest request,
  ) async {
    try {
      final token = await _authService.getToken();

      print('Creating savings goal with data: ${request.toJson()}');
      print('Token available: ${token != null}');

      final response = await http.post(
        Uri.parse('${ApiService.baseUrl}/savings-goals'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
        body: jsonEncode(request.toJson()),
      );

      print('Create goal response status: ${response.statusCode}');
      print('Create goal response body: ${response.body}');

      if (response.statusCode == 200 || response.statusCode == 201) {
        return SavingsGoalResponse.fromJson(
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
        message: 'Error creating savings goal: $e',
        type: 'connection',
      );
    }
  }

  // Update savings goal
  static Future<SavingsGoalResponse> updateSavingsGoal(
    int id,
    SavingsGoalRequest request,
  ) async {
    try {
      final token = await _authService.getToken();
      final response = await http.put(
        Uri.parse('${ApiService.baseUrl}/savings-goals/$id'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        return SavingsGoalResponse.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        throw ApiException(
          message: 'Failed to update savings goal',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Error updating savings goal: $e',
        type: 'connection',
      );
    }
  }

  // Delete savings goal
  static Future<void> deleteSavingsGoal(int id) async {
    try {
      final token = await _authService.getToken();

      print('🗑️ Deleting savings goal: $id');
      print('Token available: ${token != null}');

      final response = await http.delete(
        Uri.parse('${ApiService.baseUrl}/savings-goals/$id'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
      );

      print('Delete response status: ${response.statusCode}');
      print('Delete response body: ${response.body}');

      // API có thể trả về 200 (OK) hoặc 204 (No Content)
      if (response.statusCode != 200 && response.statusCode != 204) {
        throw ApiException(
          message: 'API trả về lỗi ${response.statusCode}: ${response.body}',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Error deleting savings goal: $e',
        type: 'connection',
      );
    }
  }

  // Get contributions for a goal
  static Future<List<SavingsContributionResponse>> getContributions(
    int goalId,
  ) async {
    try {
      final token = await _authService.getToken();
      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/savings-goals/$goalId/contributions'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final List<dynamic> jsonList = jsonDecode(response.body);
        return jsonList
            .map(
              (json) => SavingsContributionResponse.fromJson(
                json as Map<String, dynamic>,
              ),
            )
            .toList();
      } else {
        throw ApiException(
          message: 'Failed to load contributions',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Error loading contributions: $e',
        type: 'connection',
      );
    }
  }

  // Add contribution to a goal
  static Future<SavingsGoalResponse> addContribution(
    int goalId,
    SavingsContributionRequest request,
  ) async {
    try {
      final token = await _authService.getToken();
      final response = await http.post(
        Uri.parse('${ApiService.baseUrl}/savings-goals/$goalId/contributions'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        return SavingsGoalResponse.fromJson(
          jsonDecode(response.body) as Map<String, dynamic>,
        );
      } else {
        throw ApiException(
          message: 'Failed to add contribution',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Error adding contribution: $e',
        type: 'connection',
      );
    }
  }
}
