import 'package:finpal_mobile/core/constants/app_colors.dart';
import 'package:flutter/material.dart';
import 'package:finpal_mobile/data/services/auth_service.dart';
import '../widgets/confirmation_dialog.dart';
import '../widgets/scrollable_app_bar_scaffold.dart';
import '../../presentation/screens/auth/login_screen.dart';
import '../../presentation/screens/profile/app_settings_screen.dart';

/// Utility class for creating scrollable AppBar with drawer functionality
/// All screens should use AppBarWithDrawer.scrollable() method
class AppBarWithDrawer {
  /// Creates a scrollable scaffold with app bar that hides when scrolling down
  /// and shows when scrolling up
  static Widget scrollable(
    BuildContext context, {
    required Widget body,
    String userName = 'Nguyễn Văn A',
    int notificationCount = 0,
    Widget? bottomNavigationBar,
    Color? backgroundColor,
    VoidCallback? onNotificationPressed,
    bool showSearchAction = false,
  }) {
    return ScrollableAppBarScaffold(
      userName: userName,
      notificationCount: notificationCount,
      body: body,
      bottomNavigationBar: bottomNavigationBar,
      backgroundColor: backgroundColor,
      onNotificationPressed: onNotificationPressed,
      showSearchAction: showSearchAction,
      onLogoutPressed: () {
        ConfirmationDialog.show(
          context,
          title: 'Xác nhận đăng xuất',
          message:
              'Bạn có chắc chắn muốn đăng xuất khỏi tài khoản? Bạn sẽ cần đăng nhập lại để tiếp tục sử dụng FinPal.',
          confirmText: 'Đăng xuất',
          cancelText: 'Hủy',
          confirmColor: AppColors.primary,
          onConfirm: () async {
            // Gọi logout để xóa token và unregister FCM
            await AuthService().logout();

            if (context.mounted) {
              Navigator.pushAndRemoveUntil(
                context,
                MaterialPageRoute(builder: (context) => const LoginScreen()),
                (route) => false,
              );
            }
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
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Đổi ngôn ngữ: $language')));
      },
      onThemeChanged: (isDark) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Đổi chủ đề: ${isDark ? "Tối" : "Sáng"}')),
        );
      },
    );
  }
}
