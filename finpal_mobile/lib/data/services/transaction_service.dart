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
    if (merchant != null && merchant.isNotEmpty) {
      requestBody['merchant'] = merchant;
    }
    if (description != null && description.isNotEmpty) {
      requestBody['description'] = description;
    }
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

  Future<Map<String, dynamic>> getTransactions({
    int page = 0,
    int size = 10,
  }) async {
    final url = Uri.parse('$_baseUrl/transactions?page=$page&size=$size');
    final token = await _authService.getToken();
    final headers = {'Content-Type': 'application/json'};

    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    print('🔍 Fetching transactions from: $url');
    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      final errorMessage = response.body.isNotEmpty
          ? jsonDecode(response.body)['message'] ??
                'Failed to fetch transactions'
          : 'Failed to fetch transactions';
      throw Exception(errorMessage);
    }
  }

  /// Tìm kiếm giao dịch theo từ khóa (paginated)
  Future<Map<String, dynamic>> searchTransactions(String keyword, {int page = 0, int size = 10}) async {
    final encoded = Uri.encodeQueryComponent(keyword);
    final url = Uri.parse('$_baseUrl/transactions/search?keyword=$encoded&page=$page&size=$size');
    final token = await _authService.getToken();
    final headers = {'Content-Type': 'application/json'};

    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    print('🔎 Searching transactions with keyword "$keyword" at: $url');
    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      return jsonDecode(response.body) as Map<String, dynamic>;
    } else {
      final errorMessage = response.body.isNotEmpty
          ? jsonDecode(response.body)['message'] ?? 'Failed to search transactions'
          : 'Failed to search transactions';
      throw Exception(errorMessage);
    }
  }

  Future<void> updateTransaction({
    required int id,
    required String type,
    required double amount,
    required String transactionSource,
    int? categoryId,
    String? description,
    required DateTime transactionDate,
  }) async {
    final url = Uri.parse('$_baseUrl/transactions/$id');
    final token = await _authService.getToken();

    final requestBody = {
      'type': type.toUpperCase(),
      'amount': amount,
      'transactionSource': transactionSource,
      'transactionDate': transactionDate.toIso8601String(),
    };

    if (categoryId != null) requestBody['categoryId'] = categoryId;
    if (description != null) requestBody['description'] = description;

    final headers = {'Content-Type': 'application/json'};
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    print('🔄 Updating transaction $id at: $url');
    final response = await http.put(
      url,
      headers: headers,
      body: jsonEncode(requestBody),
    );

    if (response.statusCode != 200 && response.statusCode != 204) {
      final errorMessage = response.body.isNotEmpty
          ? jsonDecode(response.body)['message'] ??
                'Failed to update transaction'
          : 'Failed to update transaction';
      throw Exception(errorMessage);
    }
  }

  Future<void> deleteTransaction(int id) async {
    final url = Uri.parse('$_baseUrl/transactions/$id');
    final token = await _authService.getToken();
    final headers = {'Content-Type': 'application/json'};

    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    print('🗑️ Deleting transaction $id at: $url');
    final response = await http.delete(url, headers: headers);

    if (response.statusCode != 200 && response.statusCode != 204) {
      final errorMessage = response.body.isNotEmpty
          ? jsonDecode(response.body)['message'] ??
                'Failed to delete transaction'
          : 'Failed to delete transaction';
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
    }

    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      try {
        final List<dynamic> jsonList = jsonDecode(response.body);
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

  /// Lấy transaction theo id
  Future<Map<String, dynamic>> getTransactionById(int id) async {
    final url = Uri.parse('$_baseUrl/transactions/$id');
    final token = await _authService.getToken();
    final headers = {'Content-Type': 'application/json'};

    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    print('🔍 Fetching transaction by id from: $url');
    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      return jsonDecode(response.body) as Map<String, dynamic>;
    } else {
      final errorMessage = response.body.isNotEmpty
          ? jsonDecode(response.body)['message'] ?? 'Failed to fetch transaction'
          : 'Failed to fetch transaction';
      throw Exception(errorMessage);
    }
  }
}
