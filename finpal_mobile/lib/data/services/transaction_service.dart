import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/category.dart';
import 'auth_service.dart';

class TransactionService {
  final String _baseUrl = 'http://175.41.150.228:8080/api';
  final AuthService _authService = AuthService();

  Future<Map<String, dynamic>> addTransaction({
    required String type,
    required double amount,
    required String transactionSource,
    int? categoryId,
    String? merchant,
    String? description,
    required DateTime transactionDate,
    bool isAuto = false,
    String? notes,
  }) async {
    final url = Uri.parse('$_baseUrl/transactions');
    final token = await _authService.getToken();

    final requestBody = {
      'type': type.toUpperCase(), // INCOME or EXPENSE
      'amount': amount,
      'transactionSource': transactionSource,
      'transactionDate': transactionDate.toIso8601String(),
      'isAuto': isAuto,
    };

    // Add optional fields only if they are provided
    if (categoryId != null) requestBody['categoryId'] = categoryId;
    if (merchant != null && merchant.isNotEmpty)
      requestBody['merchant'] = merchant;
    if (description != null && description.isNotEmpty)
      requestBody['description'] = description;
    if (notes != null && notes.isNotEmpty) requestBody['notes'] = notes;

    final headers = {'Content-Type': 'application/json'};

    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    final response = await http.post(
      url,
      headers: headers,
      body: jsonEncode(requestBody),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      return jsonDecode(response.body);
    } else {
      final errorMessage = response.body.isNotEmpty
          ? jsonDecode(response.body)['message'] ?? 'Failed to add transaction'
          : 'Failed to add transaction';
      throw Exception(errorMessage);
    }
  }

  Future<List<Category>> getCategories({String? type}) async {
    var url = Uri.parse('$_baseUrl/categories');

    // Add type query parameter if provided
    if (type != null) {
      url = Uri.parse('$_baseUrl/categories?type=$type');
    }

    print('🔍 Fetching categories from: $url');

    final token = await _authService.getToken();
    final headers = {'Content-Type': 'application/json'};

    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
      print('🔑 Using token: ${token.substring(0, 20)}...');
    } else {
      print('⚠️ No token available');
    }

    final response = await http.get(url, headers: headers);

    print('📊 Categories API Status: ${response.statusCode}');
    print('📄 Categories API Response: ${response.body}');

    if (response.statusCode == 200) {
      try {
        final List<dynamic> jsonList = jsonDecode(response.body);
        print('✅ Found ${jsonList.length} categories');
        // Debug: In ra icon của mỗi category
        for (var json in jsonList) {
          print('📌 Category: ${json['name']} - Icon: ${json['icon']}');
        }
        return jsonList.map((json) => Category.fromJson(json)).toList();
      } catch (e) {
        print('❌ Error parsing categories: $e');
        throw Exception('Error parsing categories: $e');
      }
    } else if (response.statusCode == 403) {
      // Authentication required - use fallback categories
      print('⚠️ Authentication required (403). Using fallback categories.');
      return _getFallbackCategories(type);
    } else {
      final errorMessage = response.body.isNotEmpty
          ? jsonDecode(response.body)['message'] ?? 'Failed to fetch categories'
          : 'Failed to fetch categories';
      print('❌ API Error: $errorMessage');
      throw Exception(errorMessage);
    }
  }

  List<Category> _getFallbackCategories(String? type) {
    final now = DateTime.now();

    if (type == 'EXPENSE') {
      return [
        Category(
          id: 1,
          name: 'Ăn uống',
          type: 'EXPENSE',
          isSystem: true,
          displayOrder: 1,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 2,
          name: 'Di chuyển',
          type: 'EXPENSE',
          isSystem: true,
          displayOrder: 2,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 3,
          name: 'Mua sắm',
          type: 'EXPENSE',
          isSystem: true,
          displayOrder: 3,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 4,
          name: 'Giải trí',
          type: 'EXPENSE',
          isSystem: true,
          displayOrder: 4,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 5,
          name: 'Y tế',
          type: 'EXPENSE',
          isSystem: true,
          displayOrder: 5,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 6,
          name: 'Học tập',
          type: 'EXPENSE',
          isSystem: true,
          displayOrder: 6,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 99,
          name: 'Khác',
          type: 'EXPENSE',
          isSystem: true,
          displayOrder: 99,
          createdAt: now,
          updatedAt: now,
        ),
      ];
    } else if (type == 'INCOME') {
      return [
        Category(
          id: 101,
          name: 'Lương',
          type: 'INCOME',
          isSystem: true,
          displayOrder: 1,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 102,
          name: 'Thưởng',
          type: 'INCOME',
          isSystem: true,
          displayOrder: 2,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 103,
          name: 'Đầu tư',
          type: 'INCOME',
          isSystem: true,
          displayOrder: 3,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 104,
          name: 'Kinh doanh',
          type: 'INCOME',
          isSystem: true,
          displayOrder: 4,
          createdAt: now,
          updatedAt: now,
        ),
        Category(
          id: 199,
          name: 'Thu nhập khác',
          type: 'INCOME',
          isSystem: true,
          displayOrder: 99,
          createdAt: now,
          updatedAt: now,
        ),
      ];
    }

    return [];
  }
}
