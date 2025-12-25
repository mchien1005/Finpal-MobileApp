import 'package:finpal_mobile/presentation/screens/profile/Introduce_screen.dart';
import 'package:finpal_mobile/presentation/screens/profile/help_center_screen.dart';
import 'package:finpal_mobile/presentation/screens/profile/profile_screen.dart';
import 'package:finpal_mobile/presentation/screens/profile/change_password_screen.dart';
import 'package:flutter/material.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import '../../../core/constants/app_colors.dart';
import '../notifications/notifications_screen.dart';
import 'data_privacy_screen.dart';

class AppSettingsScreen extends StatelessWidget {
  const AppSettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      body: SafeArea(
        child: Container(
          color: const Color(0xFFEFF6FF),
          child: Column(
            children: [
              // Custom App Bar
              Container(
                decoration: BoxDecoration(
                  color: AppColors.primary,
                  borderRadius: const BorderRadius.only(
                    bottomLeft: Radius.circular(24),
                    bottomRight: Radius.circular(24),
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 15,
                      offset: const Offset(0, 10),
                    ),
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 6,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(16, 8, 16, 20),
                  child: Row(
                    children: [
                      InkWell(
                        onTap: () => Navigator.pop(context),
                        borderRadius: BorderRadius.circular(8),
                        child: const Padding(
                          padding: EdgeInsets.symmetric(
                            horizontal: 4,
                            vertical: 8,
                          ),
                          child: Row(
                            children: [
                              Icon(
                                Icons.arrow_back_ios,
                                color: Colors.white,
                                size: 14,
                              ),
                              SizedBox(width: 4),
                              Text(
                                'Quay lại',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 14,
                                  fontFamily: 'Arimo',
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const Spacer(),
                      const Text(
                        'Cài đặt',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          fontFamily: 'Arimo',
                        ),
                      ),
                      const Spacer(),
                      const SizedBox(width: 80),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 24),

              // Settings List
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Column(
                    children: [
                      _buildSettingCard(
                        context: context,
                        icon: Icons.person_outline,
                        title: 'Hồ sơ',
                        subtitle: 'Quản lý thông tin cá nhân',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => const ProfileScreen(),
                            ),
                          );
                        },
                      ),
                      const SizedBox(height: 12),
                      _buildSettingCard(
                        context: context,
                        icon: Icons.notifications_outlined,
                        title: 'Cài đặt thông báo',
                        subtitle: 'Tùy chỉnh thông báo của bạn',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => const NotificationsScreen(),
                            ),
                          );
                        },
                      ),
                      const SizedBox(height: 12),
                      _buildSettingCard(
                        context: context,
                        icon: Icons.lock_outline,
                        title: 'Thiết lập mật khẩu',
                        subtitle: 'Thay đổi mật khẩu bảo mật',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) =>
                                  const ChangePasswordScreen(),
                            ),
                          );
                        },
                      ),
                      const SizedBox(height: 12),
                      _buildSettingCard(
                        context: context,
                        icon: Icons.help_outline,
                        title: 'Trung tâm trợ giúp',
                        subtitle: 'Câu hỏi thường gặp & hỗ trợ',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => const HelpCenterScreen(),
                            ),
                          );
                        },
                      ),
                      const SizedBox(height: 12),
                      _buildSettingCard(
                        context: context,
                        icon: MdiIcons.databaseOutline,
                        title: 'Dữ liệu cá nhân',
                        subtitle: 'Quản lý dữ liệu cá nhân',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => const DataPrivacyScreen(),
                            ),
                          );
                        },
                      ),
                      const SizedBox(height: 12),
                      _buildSettingCard(
                        context: context,
                        icon: Icons.info_outline,
                        title: 'Giới thiệu',
                        subtitle: 'Về FinPal v1.0.0',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => const IntroduceScreen(),
                            ),
                          );
                        },
                      ),
                      const SizedBox(height: 24),
                      // Footer
                      Container(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        decoration: BoxDecoration(
                          border: Border(
                            top: BorderSide(
                              color: Colors.black.withOpacity(0.1),
                              width: 1,
                            ),
                          ),
                        ),
                        child: const Column(
                          children: [
                            Text(
                              'FinPal - Ví Thông Minh',
                              style: TextStyle(
                                fontSize: 16,
                                color: Color(0xFF6A7282),
                                fontFamily: 'Arimo',
                              ),
                              textAlign: TextAlign.center,
                            ),
                            SizedBox(height: 4),
                            Text(
                              'Version 1.0.0',
                              style: TextStyle(
                                fontSize: 16,
                                color: Color(0xFF6A7282),
                                fontFamily: 'Arimo',
                              ),
                              textAlign: TextAlign.center,
                            ),
                            SizedBox(height: 4),
                            Text(
                              '© 2025 FinPal. All rights reserved.',
                              style: TextStyle(
                                fontSize: 12,
                                color: Color(0xFF6A7282),
                                fontFamily: 'Arimo',
                              ),
                              textAlign: TextAlign.center,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSettingCard({
    required BuildContext context,
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(14),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 6,
              offset: const Offset(0, 4),
            ),
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 4,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: const Color(0xFFFDFDFD),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, size: 24, color: const Color(0xFF101828)),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 16,
                      color: Color(0xFF101828),
                      fontFamily: 'Arimo',
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    subtitle,
                    style: const TextStyle(
                      fontSize: 14,
                      color: Color(0xFF6A7282),
                      fontFamily: 'Arimo',
                    ),
                  ),
                ],
              ),
            ),
            const Icon(
              Icons.arrow_forward_ios,
              size: 20,
              color: Color(0xFF6A7282),
            ),
          ],
        ),
      ),
    );
  }
}
