import 'dart:convert';
import 'package:http/http.dart' as http;

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
        final error = jsonDecode(response.body);
        throw Exception(error['message'] ?? 'Có lỗi xảy ra');
      }
    } catch (e) {
      throw Exception('Không thể kết nối đến server: $e');
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
        final error = jsonDecode(response.body);
        throw Exception(error['message'] ?? 'Có lỗi xảy ra');
      }
    } catch (e) {
      throw Exception('Không thể kết nối đến server: $e');
    }
  }

  Future<Map<String, dynamic>> put(
    String endpoint,
    Map<String, dynamic> body,
  ) async {
    try {
      final response = await http.put(
        Uri.parse('$baseUrl$endpoint'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(body),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body) as Map<String, dynamic>;
      } else {
        final error = jsonDecode(response.body);
        throw Exception(error['message'] ?? 'Có lỗi xảy ra');
      }
    } catch (e) {
      throw Exception('Không thể kết nối đến server: $e');
    }
  }
}
