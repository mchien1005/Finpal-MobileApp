import 'package:flutter/material.dart';
import '../../presentation/screens/home/dashboard_screen.dart';
import '../../presentation/screens/transactions/add_transaction_screen.dart';
import '../../presentation/screens/insights/ai_insights_screen.dart';
import '../../presentation/screens/savings/savings_goals_screen.dart';
import '../../presentation/screens/transactions/transactions_screen.dart';

/// Helper class for bottom navigation bar navigation
class BottomNavHelper {
  /// Navigate to the screen corresponding to the index
  /// Replaces current route to avoid stacking screens
  static void navigateToIndex(
    BuildContext context,
    int index,
    int currentIndex,
  ) {
    // Don't navigate if already on the same screen
    if (index == currentIndex) return;

    Widget? targetScreen;

    switch (index) {
      case 0:
        targetScreen = const DashboardScreen();
        break;
      case 1:
        targetScreen = const TransactionsScreen();
        break;
      case 2:
        targetScreen = const AddTransactionScreen();
        break;
      case 3:
        targetScreen = const AIInsightsScreen();
        break;
      case 4:
        targetScreen = const SavingsGoalsScreen();
        break;
      default:
        return; // Invalid index, do nothing
    }

    if (targetScreen != null) {
      Navigator.pushAndRemoveUntil(
        context,
        PageRouteBuilder(
          pageBuilder: (context, animation, secondaryAnimation) =>
              targetScreen!,
          transitionsBuilder: (context, animation, secondaryAnimation, child) {
            return FadeTransition(opacity: animation, child: child);
          },
          transitionDuration: const Duration(milliseconds: 200),
        ),
        (route) => false,
      );
    }
  }
}
