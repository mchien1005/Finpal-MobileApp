import 'api_service.dart';
import 'storage_service.dart';
import 'firebase_push_handler.dart';

class AuthService {
  final ApiService _apiService = ApiService();
  final StorageService _storageService = StorageService();
  final FirebasePushHandler _fcmHandler = FirebasePushHandler();

  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;
  AuthService._internal();

  Future<Map<String, dynamic>> login(String username, String password) async {
    try {
      final response = await _apiService.post('/auth/login', {
        'username': username,
        'password': password,
      });

      // Lưu token nếu có
      if (response['token'] != null) {
        await _storageService.saveToken(response['token']);

        // Đăng ký FCM token sau khi login thành công
        await _fcmHandler.registerToken();
      }

      // Lưu thông tin user nếu có
      if (response['user'] != null) {
        await _storageService.saveUserData(response['user']);
      }

      return response;
    } catch (e) {
      rethrow;
    }
  }

  Future<Map<String, dynamic>> register(
    String username,
    String email,
    String fullName,
    String password,
  ) async {
    try {
      final response = await _apiService.post('/auth/register', {
        'username': username,
        'email': email,
        'fullName': fullName,
        'password': password,
      });
      return response;
    } catch (e) {
      rethrow;
    }
  }

  Future<void> logout() async {
    // Hủy đăng ký FCM token trước khi logout
    await _fcmHandler.unregisterToken();
    await _storageService.clearAll();
  }

  Future<bool> isLoggedIn() async {
    final token = await _storageService.getToken();
    return token != null && token.isNotEmpty;
  }

  Future<String?> getToken() async {
    return await _storageService.getToken();
  }
}
