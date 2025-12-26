import 'dart:convert';
import 'package:http/http.dart' as http;
import 'storage_service.dart';

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
  final StorageService _storageService = StorageService();

  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  Future<Map<String, String>> _getHeaders({bool includeAuth = true}) async {
    final headers = {'Content-Type': 'application/json'};

    if (includeAuth) {
      final token = await _storageService.getToken();
      if (token != null && token.isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }
    }

    return headers;
  }

  Future<Map<String, dynamic>> post(
    String endpoint,
    Map<String, dynamic> body, {
    bool includeAuth = true,
  }) async {
    try {
      final headers = await _getHeaders(includeAuth: includeAuth);

      // Debug logging: request details
      final url = '$baseUrl$endpoint';
      try {
        print('📡 POST: $url');
        print('🔑 Headers: $headers');
        print('🧾 Body: ${jsonEncode(body)}');
      } catch (_) {}

      final response = await http.post(
        Uri.parse(url),
        headers: headers,
        body: jsonEncode(body),
      );

      // Debug logging: response details
      try {
        print('📊 Response Status: ${response.statusCode}');
        print('📄 Response Body: ${response.body}');
      } catch (_) {}

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

  Future<dynamic> get(String endpoint, {bool includeAuth = true}) async {
    try {
      final url = '$baseUrl$endpoint';
      print('📡 GET: $url');

      final headers = await _getHeaders(includeAuth: includeAuth);
      print('🔑 Headers: $headers');

      final response = await http.get(Uri.parse(url), headers: headers);

      print('📊 Response Status: ${response.statusCode}');
      print('📄 Response Body: ${response.body}');

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        try {
          final error = jsonDecode(response.body);
          throw ApiException(
            message: error['message'] ?? error['error'] ?? 'Có lỗi xảy ra',
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
      print('❌ Connection error: $e');
      throw ApiException(
        message:
            'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.',
        type: 'connection',
      );
    }
  }

  Future<dynamic> put(
    String endpoint,
    Map<String, dynamic> body, {
    bool includeAuth = true,
  }) async {
    try {
      final headers = await _getHeaders(includeAuth: includeAuth);
      final response = await http.put(
        Uri.parse('$baseUrl$endpoint'),
        headers: headers,
        body: jsonEncode(body),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body);
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

  Future<void> delete(String endpoint, {bool includeAuth = true}) async {
    try {
      final headers = await _getHeaders(includeAuth: includeAuth);
      final response = await http.delete(
        Uri.parse('$baseUrl$endpoint'),
        headers: headers,
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
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
