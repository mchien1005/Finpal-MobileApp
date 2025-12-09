import 'package:flutter/material.dart';
import '../widgets/custom_app_bar.dart';
import '../widgets/custom_drawer.dart';

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
