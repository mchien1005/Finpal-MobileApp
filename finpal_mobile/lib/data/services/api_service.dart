import 'dart:convert';
import 'package:http/http.dart' as http;

class ApiException implements Exception {
  final String message;
  final int? statusCode;
  final String type; // 'connection', 'validation', 'server'

  ApiException({required this.message, this.statusCode, this.type = 'server'});

  @override
  String toString() => message;
}

class ApiService {
  static const String baseUrl = 'http://175.41.150.228:8080/api';

  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  Future<Map<String, dynamic>> post(
    String endpoint,
    Map<String, dynamic> body,
  ) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl$endpoint'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(body),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body) as Map<String, dynamic>;
      } else {
        try {
          final error = jsonDecode(response.body);
          throw ApiException(
            message: error['message'] ?? 'Có lỗi xảy ra',
            statusCode: response.statusCode,
            type: 'validation',
          );
        } catch (e) {
          if (e is ApiException) rethrow;
          throw ApiException(
            message: 'Có lỗi xảy ra',
            statusCode: response.statusCode,
            type: 'validation',
          );
        }
      }
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException(
        message:
            'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.',
        type: 'connection',
      );
    }
  }

  Future<Map<String, dynamic>> get(
    String endpoint, {
    Map<String, String>? headers,
  }) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl$endpoint'),
        headers: headers ?? {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body) as Map<String, dynamic>;
      } else {
        try {
          final error = jsonDecode(response.body);
          throw ApiException(
            message: error['message'] ?? 'Có lỗi xảy ra',
            statusCode: response.statusCode,
            type: 'validation',
          );
        } catch (e) {
          if (e is ApiException) rethrow;
          throw ApiException(
            message: 'Có lỗi xảy ra',
            statusCode: response.statusCode,
            type: 'validation',
          );
        }
      }
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException(
        message:
            'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.',
        type: 'connection',
      );
    }
  }
}
