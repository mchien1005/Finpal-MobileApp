import 'package:flutter/material.dart';
import '../constants/app_colors.dart';
import '../widgets/custom_drawer.dart';
import '../../presentation/screens/notification_screen.dart';
import '../../presentation/screens/transactions/search_transaction_screen.dart';

/// A scaffold with a scrollable app bar that hides when scrolling down
class ScrollableAppBarScaffold extends StatelessWidget {
  final String userName;
  final int notificationCount;
  final Widget body;
  final Widget? bottomNavigationBar;
  final Color? backgroundColor;
  final VoidCallback? onNotificationPressed;
  final VoidCallback? onLogoutPressed;
  final VoidCallback? onSettingsPressed;
  final Function(String)? onLanguageChanged;
  final Function(bool)? onThemeChanged;
  final bool showSearchAction;
  final String? customTitle; // Tiêu đề tùy chỉnh thay vì 'Xin chào, userName'

  const ScrollableAppBarScaffold({
    super.key,
    required this.body,
    this.userName = 'Nguyễn Văn A',
    this.notificationCount = 0,
    this.bottomNavigationBar,
    this.backgroundColor,
    this.onNotificationPressed,
    this.onLogoutPressed,
    this.onSettingsPressed,
    this.onLanguageChanged,
    this.onThemeChanged,
    this.showSearchAction = false,
    this.customTitle,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: backgroundColor ?? AppColors.background,
      endDrawer: CustomDrawer(
        onLogoutPressed: onLogoutPressed,
        onSettingsPressed: onSettingsPressed,
        onLanguageChanged: onLanguageChanged,
        onThemeChanged: onThemeChanged,
      ),
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            backgroundColor: AppColors.primary,
            elevation: 0,
            floating: true,
            snap: true,
            pinned: false,
            automaticallyImplyLeading: false,
            title: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'FinPal',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 12,
                    fontWeight: FontWeight.normal,
                  ),
                ),
                Text(
                  customTitle ?? 'Xin chào, $userName',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            actions: [
              if (showSearchAction)
                IconButton(
                  icon: const Icon(Icons.search, color: Colors.white),
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const SearchTransactionScreen(),
                      ),
                    );
                  },
                ),
              Stack(
                children: [
                  IconButton(
                    icon: const Icon(
                      Icons.notifications_outlined,
                      color: Colors.white,
                    ),
                    onPressed:
                        onNotificationPressed ??
                        () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => const NotificationScreen(),
                            ),
                          );
                        },
                  ),
                  if (notificationCount > 0)
                    Positioned(
                      right: 8,
                      top: 8,
                      child: Container(
                        padding: const EdgeInsets.all(4),
                        decoration: const BoxDecoration(
                          color: Colors.white,
                          shape: BoxShape.circle,
                        ),
                        constraints: const BoxConstraints(
                          minWidth: 16,
                          minHeight: 16,
                        ),
                        child: Text(
                          notificationCount > 9 ? '9+' : '$notificationCount',
                          style: const TextStyle(
                            color: AppColors.primary,
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ),
                ],
              ),
              Builder(
                builder: (BuildContext context) {
                  return IconButton(
                    icon: const Icon(Icons.menu_outlined, color: Colors.white),
                    onPressed: () {
                      Scaffold.of(context).openEndDrawer();
                    },
                  );
                },
              ),
            ],
          ),
          SliverToBoxAdapter(child: body),
        ],
      ),
      bottomNavigationBar: bottomNavigationBar,
    );
  }
}
