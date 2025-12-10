import 'package:finpal_mobile/core/constants/app_colors.dart';
import 'package:flutter/material.dart';
import '../widgets/custom_app_bar.dart';
import '../widgets/custom_drawer.dart';
import '../widgets/confirmation_dialog.dart';
import '../../presentation/screens/auth/login_screen.dart';
import '../../presentation/screens/profile/app_settings_screen.dart';

/// Wrapper widget that provides both AppBar and Drawer functionality
/// Use this in Scaffold instead of separate appBar and endDrawer
class AppBarWithDrawer extends StatelessWidget {
  final String userName;
  final int notificationCount;
  final VoidCallback? onNotificationPressed;
  final VoidCallback? onLogoutPressed;
  final VoidCallback? onSettingsPressed;
  final Function(String)? onLanguageChanged;
  final Function(bool)? onThemeChanged;

  const AppBarWithDrawer({
    super.key,
    this.userName = 'Nguyễn Văn A',
    this.notificationCount = 0,
    this.onNotificationPressed,
    this.onLogoutPressed,
    this.onSettingsPressed,
    this.onLanguageChanged,
    this.onThemeChanged,
  });

  /// Factory constructor with common shared event handlers
  /// Use this for consistent behavior across all screens
  factory AppBarWithDrawer.common(
    BuildContext context, {
    String userName = 'Nguyễn Văn A',
    int notificationCount = 0,
    VoidCallback? onNotificationPressed,
  }) {
    return AppBarWithDrawer(
      userName: userName,
      notificationCount: notificationCount,
      onNotificationPressed: onNotificationPressed,
      onLogoutPressed: () {
        ConfirmationDialog.show(
          context,
          title: 'Xác nhận đăng xuất',
          message:
              'Bạn có chắc chắn muốn đăng xuất khỏi tài khoản? Bạn sẽ cần đăng nhập lại để tiếp tục sử dụng FinPal.',
          confirmText: 'Đăng xuất',
          cancelText: 'Hủy',
          confirmColor: AppColors.primary,
          onConfirm: () {
            Navigator.pushAndRemoveUntil(
              context,
              MaterialPageRoute(builder: (context) => const LoginScreen()),
              (route) => false,
            );
          },
        );
      },
      onSettingsPressed: () {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => const AppSettingsScreen()),
        );
      },
      onLanguageChanged: (language) {
        // TODO: Implement language change logic
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Đổi ngôn ngữ: $language')));
      },
      onThemeChanged: (isDark) {
        // TODO: Implement theme change logic
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Đổi chủ đề: ${isDark ? "Tối" : "Sáng"}')),
        );
      },
    );
  }

  /// Returns the AppBar widget
  PreferredSizeWidget get appBar => CustomAppBar(
    userName: userName,
    notificationCount: notificationCount,
    onNotificationPressed: onNotificationPressed,
  );

  /// Returns the Drawer widget
  Widget get drawer => CustomDrawer(
    onLogoutPressed: onLogoutPressed,
    onSettingsPressed: onSettingsPressed,
    onLanguageChanged: onLanguageChanged,
    onThemeChanged: onThemeChanged,
  );

  @override
  Widget build(BuildContext context) {
    // This widget is not meant to be used in the widget tree
    // Use the appBar and drawer getters instead
    throw UnimplementedError(
      'AppBarWithDrawer should not be built directly. '
      'Use appBar and drawer properties instead.',
    );
  }
}
