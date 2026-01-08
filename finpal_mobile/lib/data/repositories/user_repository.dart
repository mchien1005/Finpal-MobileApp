import 'dart:io';
import '../models/user.dart';
import '../services/user_service.dart';
import '../services/api_service.dart';
import '../services/storage_service.dart';

class UserRepository {
  final UserService _userService = UserService();
  final StorageService _storageService = StorageService();

  static final UserRepository _instance = UserRepository._internal();
  factory UserRepository() => _instance;
  UserRepository._internal();

  /// Lấy thông tin profile từ API
  Future<UserProfile> getProfile() async {
    try {
      print('📌 UserRepository: Getting profile...');
      final profile = await _userService.getProfile();
      print('✅ UserRepository: Profile retrieved successfully');
      return profile;
    } on ApiException catch (e) {
      print('⚠️ UserRepository: ApiException - ${e.message}');
      rethrow;
    } catch (e, stackTrace) {
      print('❌ UserRepository: Unexpected error - $e');
      print('Stack trace: $stackTrace');
      throw ApiException(
        message: 'Có lỗi xảy ra khi tải thông tin profile: $e',
        type: 'server',
      );
    }
  }

  /// Cập nhật thông tin profile
  Future<UserProfile> updateProfile({
    String? fullName,
    String? email,
    String? gender,
    String? phoneNumber,
    String? dateOfBirth,
  }) async {
    try {
      final request = UpdateProfileRequest(
        fullName: fullName,
        email: email,
        gender: gender,
        phoneNumber: phoneNumber,
        dateOfBirth: dateOfBirth,
      );

      final updatedProfile = await _userService.updateProfile(request);

      // Cập nhật local storage
      await _storageService.saveUserData(updatedProfile.toJson());

      return updatedProfile;
    } on ApiException catch (e) {
      throw e;
    } catch (e) {
      throw ApiException(
        message: 'Có lỗi xảy ra khi cập nhật profile',
        type: 'server',
      );
    }
  }

  /// Upload ảnh đại diện
  Future<String> uploadAvatar(File imageFile) async {
    try {
      final response = await _userService.uploadAvatar(imageFile);

      // Lấy profile mới để cập nhật avatarUrl
      final updatedProfile = await getProfile();
      await _storageService.saveUserData(updatedProfile.toJson());

      return response.avatarUrl;
    } on ApiException catch (e) {
      throw e;
    } catch (e) {
      throw ApiException(
        message: 'Có lỗi xảy ra khi upload ảnh đại diện',
        type: 'server',
      );
    }
  }

  /// Lấy thông tin user từ local storage
  Future<Map<String, dynamic>?> getLocalUserData() async {
    return await _storageService.getUserData();
  }
}
