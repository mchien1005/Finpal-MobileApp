import 'dart:io';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart' as http_parser;
import 'api_service.dart';
import 'storage_service.dart';
import '../models/user.dart';

class UserService {
  final ApiService _apiService = ApiService();

  static final UserService _instance = UserService._internal();
  factory UserService() => _instance;
  UserService._internal();

  /// Lấy thông tin profile của user hiện tại
  /// GET /api/user/profile
  Future<UserProfile> getProfile() async {
    try {
      print('🔍 UserService: Calling /user/profile...');
      final response = await _apiService.get('/user/profile');
      print('📦 UserService: Response type: ${response.runtimeType}');
      print('📦 UserService: Response data: $response');

      if (response is! Map<String, dynamic>) {
        throw ApiException(
          message: 'Invalid response format from server',
          type: 'validation',
        );
      }

      // Extract data from wrapper object {success: true, data: {...}}
      final data = response['data'] as Map<String, dynamic>;
      print('📦 UserService: Extracted data: $data');

      final profile = UserProfile.fromJson(data);
      print('✅ UserService: Profile parsed successfully');
      return profile;
    } catch (e, stackTrace) {
      print('❌ UserService Error: $e');
      print('Stack: $stackTrace');
      rethrow;
    }
  }

  /// Cập nhật thông tin profile
  /// PUT /api/user/profile
  Future<UserProfile> updateProfile(UpdateProfileRequest request) async {
    try {
      print('🔄 UserService: Updating profile with data: ${request.toJson()}');
      final response = await _apiService.put('/user/profile', request.toJson());
      print('📦 UserService: Update response: $response');

      if (response is! Map<String, dynamic>) {
        throw ApiException(
          message: 'Invalid response format from server',
          type: 'validation',
        );
      }

      // Extract data from wrapper object {success: true, data: {...}}
      final data = response['data'] as Map<String, dynamic>;
      print('📦 UserService: Extracted updated data: $data');

      final profile = UserProfile.fromJson(data);
      print('✅ UserService: Profile updated successfully');
      return profile;
    } catch (e, stackTrace) {
      print('❌ UserService Update Error: $e');
      print('Stack: $stackTrace');
      rethrow;
    }
  }

  /// Upload ảnh đại diện
  /// POST /api/user/avatar
  Future<UploadAvatarResponse> uploadAvatar(File imageFile) async {
    try {
      print('📤 UserService: Uploading avatar...');
      // Get storage service instance
      final storageService = StorageService();
      final token = await storageService.getToken();
      if (token == null || token.isEmpty) {
        throw ApiException(
          message: 'Không tìm thấy token đăng nhập',
          type: 'validation',
        );
      }

      final uri = Uri.parse('${ApiService.baseUrl}/user/avatar');
      final request = http.MultipartRequest('POST', uri);

      // Add headers
      request.headers['Authorization'] = 'Bearer $token';

      // Add file with proper content type
      final stream = http.ByteStream(imageFile.openRead());
      final length = await imageFile.length();

      // Determine mime type
      String mimeType = 'image/jpeg';
      final fileName = imageFile.path.split('/').last.toLowerCase();
      if (fileName.endsWith('.png')) {
        mimeType = 'image/png';
      } else if (fileName.endsWith('.jpg') || fileName.endsWith('.jpeg')) {
        mimeType = 'image/jpeg';
      }

      print('📤 File: $fileName, Size: $length bytes, MimeType: $mimeType');

      // Try 'file' as field name (common in Spring Boot)
      final multipartFile = http.MultipartFile(
        'file',
        stream,
        length,
        filename: fileName,
        contentType: http_parser.MediaType.parse(mimeType),
      );
      request.files.add(multipartFile);

      print('📤 Sending avatar to: $uri');
      // Send request
      final streamedResponse = await request.send();
      final response = await http.Response.fromStream(streamedResponse);

      print('📊 Upload Response Status: ${response.statusCode}');
      print('📄 Upload Response Body: ${response.body}');

      if (response.statusCode == 200) {
        final jsonResponse = jsonDecode(response.body) as Map<String, dynamic>;
        print('✅ Avatar uploaded successfully');

        // Parse the response which has both message and data
        return UploadAvatarResponse.fromJson(jsonResponse);
      } else {
        try {
          final error = jsonDecode(response.body);
          throw ApiException(
            message: error['message'] ?? 'Có lỗi xảy ra khi upload ảnh',
            statusCode: response.statusCode,
            type: 'validation',
          );
        } catch (e) {
          if (e is ApiException) rethrow;
          throw ApiException(
            message: 'Có lỗi xảy ra khi upload ảnh',
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
