// User Model - Model cho thông tin người dùng
// Dùng để quản lý thông tin cá nhân của người dùng

/// Request model để cập nhật thông tin profile
class UpdateProfileRequest {
  final String? fullName;
  final String? email;
  final String? gender;
  final String? phoneNumber;
  final String? dateOfBirth; // Format: dd/MM/yyyy

  UpdateProfileRequest({
    this.fullName,
    this.email,
    this.gender,
    this.phoneNumber,
    this.dateOfBirth,
  });

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = {};

    if (fullName != null) data['fullName'] = fullName;
    if (email != null) data['email'] = email;
    if (gender != null) data['gender'] = gender;
    if (phoneNumber != null) data['phone'] = phoneNumber; // API uses 'phone'
    if (dateOfBirth != null) data['dateOfBirth'] = dateOfBirth;

    return data;
  }
}

/// Response model từ API
class UserProfile {
  final int id;
  final String username;
  final String email;
  final String? fullName;
  final String? phoneNumber;
  final String? gender;
  final String? dateOfBirth; // Format: dd/MM/yyyy
  final String? avatarUrl;
  final DateTime? createdAt;
  final DateTime? updatedAt;
  final bool enabled;

  UserProfile({
    required this.id,
    required this.username,
    required this.email,
    this.fullName,
    this.phoneNumber,
    this.gender,
    this.dateOfBirth,
    this.avatarUrl,
    this.createdAt,
    this.updatedAt,
    required this.enabled,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    try {
      print('🔄 Parsing UserProfile from: $json');

      // Get avatar URL and convert to full URL if it's a relative path
      String? avatarUrl = json['avatarUrl'] as String?;
      if (avatarUrl != null &&
          avatarUrl.isNotEmpty &&
          !avatarUrl.startsWith('http')) {
        // Convert relative path to full URL
        avatarUrl = 'http://175.41.150.228:8080$avatarUrl';
        print('🖼️ Converted avatar URL to: $avatarUrl');
      }

      return UserProfile(
        id: json['id'] as int,
        username: json['username'] as String,
        email: json['email'] as String,
        fullName: json['fullName'] as String?,
        phoneNumber:
            json['phone'] as String?, // API uses 'phone' not 'phoneNumber'
        gender: json['gender'] as String?,
        dateOfBirth: json['dateOfBirth'] as String?,
        avatarUrl: avatarUrl,
        createdAt: json['createdAt'] != null
            ? DateTime.tryParse(json['createdAt'] as String)
            : null,
        updatedAt: json['updatedAt'] != null
            ? DateTime.tryParse(json['updatedAt'] as String)
            : null,
        enabled: true, // Default to true as API doesn't return this field
      );
    } catch (e, stackTrace) {
      print('❌ Error parsing UserProfile: $e');
      print('JSON data: $json');
      print('Stack: $stackTrace');
      rethrow;
    }
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'email': email,
      'fullName': fullName,
      'phoneNumber': phoneNumber,
      'gender': gender,
      'dateOfBirth': dateOfBirth,
      'avatarUrl': avatarUrl,
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
      if (updatedAt != null) 'updatedAt': updatedAt!.toIso8601String(),
      'enabled': enabled,
    };
  }

  // Helper method to get avatar initials
  String getInitials() {
    if (fullName != null && fullName!.isNotEmpty) {
      final parts = fullName!.trim().split(' ');
      if (parts.isNotEmpty) {
        return parts.last[0].toUpperCase();
      }
    }
    return username[0].toUpperCase();
  }
}

/// Response cho việc upload avatar
class UploadAvatarResponse {
  final String avatarUrl;
  final String message;

  UploadAvatarResponse({required this.avatarUrl, required this.message});

  factory UploadAvatarResponse.fromJson(Map<String, dynamic> json) {
    // API returns both top-level avatarUrl and data.avatarUrl
    // Use data.avatarUrl if available, otherwise use top-level avatarUrl
    String avatarUrl = json['avatarUrl'] as String? ?? '';

    // If response has 'data' object, use avatarUrl from there
    if (json.containsKey('data') && json['data'] != null) {
      final data = json['data'] as Map<String, dynamic>;
      avatarUrl = data['avatarUrl'] as String? ?? avatarUrl;
    }

    return UploadAvatarResponse(
      avatarUrl: avatarUrl,
      message: json['message'] as String? ?? 'Upload thành công',
    );
  }
}
