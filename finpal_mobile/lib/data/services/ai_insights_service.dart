import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/weekly_spending_trend_model.dart';
import 'api_service.dart';
import 'storage_service.dart';

/// AI Insights Service
/// Service để gọi các API AI Insights từ http://175.41.150.228:8000
class AIInsightsService {
  static const String baseUrl = 'http://175.41.150.228:8000/api';
  final StorageService _storageService = StorageService();

  static final AIInsightsService _instance = AIInsightsService._internal();
  factory AIInsightsService() => _instance;
  AIInsightsService._internal();

  /// Lấy headers với token
  Future<Map<String, String>> _getHeaders() async {
    final token = await _storageService.getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null && token.isNotEmpty) 'Authorization': 'Bearer $token',
    };
  }

  /// Lấy xu hướng chi tiêu tuần này
  /// GET /api/insights/weekly-spending-trend/{user_id}
  Future<WeeklySpendingTrendResponse> getWeeklySpendingTrend(int userId) async {
    try {
      final headers = await _getHeaders();
      final url = '$baseUrl/insights/weekly-spending-trend/$userId';
      print('🤖 AI Insights: GET $url');
      print('🔑 Headers: $headers');

      final response = await http.get(Uri.parse(url), headers: headers);

      print('📊 AI Response Status: ${response.statusCode}');
      print('📄 AI Response Body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        print('✅ AI Insights: Parsing weekly trend data...');
        return WeeklySpendingTrendResponse.fromJson(data);
      } else {
        print('❌ AI Insights Error: ${response.statusCode} - ${response.body}');
        throw ApiException(
          message:
              'Không thể tải xu hướng chi tiêu tuần này (${response.statusCode})',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      print('❌ AI Insights Exception: $e');
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server AI: ${e.toString()}',
        type: 'connection',
      );
    }
  }

  /// Lấy gợi ý tiết kiệm
  /// GET /api/insights/savings-suggestions/{user_id}
  Future<Map<String, dynamic>> getSavingsSuggestions(int userId) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/insights/savings-suggestions/$userId'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        throw ApiException(
          message: 'Không thể tải gợi ý tiết kiệm',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server AI',
        type: 'connection',
      );
    }
  }

  /// Lấy mẫu chi tiêu
  /// GET /api/insights/spending-patterns/{user_id}
  Future<Map<String, dynamic>> getSpendingPatterns(int userId) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/insights/spending-patterns/$userId'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        throw ApiException(
          message: 'Không thể tải mẫu chi tiêu',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server AI',
        type: 'connection',
      );
    }
  }

  /// Lấy thông tin chi tiêu chủ động
  /// GET /api/insights/proactive-insights/{user_id}
  Future<Map<String, dynamic>> getProactiveInsights(int userId) async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/insights/proactive-insights/$userId'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        throw ApiException(
          message: 'Không thể tải thông tin chi tiêu chủ động',
          statusCode: response.statusCode,
        );
      }
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException(
        message: 'Không thể kết nối đến server AI',
        type: 'connection',
      );
    }
  }
}
