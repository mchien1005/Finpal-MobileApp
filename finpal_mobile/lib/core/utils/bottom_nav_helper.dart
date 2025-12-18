import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../presentation/screens/home/dashboard_screen.dart';
import '../../presentation/screens/transactions/add_transaction_screen.dart';
import '../../presentation/screens/insights/ai_insights_screen.dart';
import '../../presentation/screens/savings/savings_goals_screen.dart';
import '../../presentation/screens/transactions/transactions_screen.dart';

/// Helper class for bottom navigation bar navigation
class BottomNavHelper {
  /// Thời điểm back lần trước (dùng cho double back to exit)
  static DateTime? _lastBackPressTime;

  /// Reset thời điểm back - gọi khi app khởi động hoặc chuyển màn hình
  static void resetBackPressTime() {
    _lastBackPressTime = null;
  }

  /// Navigate to the screen corresponding to the index
  /// Sử dụng pushAndRemoveUntil để đảm bảo navigation stack nhất quán
  static void navigateToIndex(
    BuildContext context,
    int index,
    int currentIndex,
  ) {
    // Don't navigate if already on the same screen
    if (index == currentIndex) return;

    // Reset back press time khi chuyển màn hình
    resetBackPressTime();

    late Widget targetScreen;

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

    // Sử dụng pushAndRemoveUntil để xóa hết stack
    // Mỗi màn hình sẽ tự xử lý back button bằng PopScope
    Navigator.pushAndRemoveUntil(
      context,
      PageRouteBuilder(
        pageBuilder: (context, animation, secondaryAnimation) => targetScreen,
        transitionsBuilder: (context, animation, secondaryAnimation, child) {
          return FadeTransition(opacity: animation, child: child);
        },
        transitionDuration: const Duration(milliseconds: 200),
      ),
      (route) => false,
    );
  }

  /// Xử lý back button - double back để thoát từ bất kỳ màn hình nào
  static Future<bool> handleBackButton(BuildContext context) async {
    final now = DateTime.now();

    if (_lastBackPressTime == null) {
      _lastBackPressTime = now;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vuốt thêm lần nữa để thoát'),
          duration: Duration(seconds: 2),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return false;
    }

    final difference = now.difference(_lastBackPressTime!);

    if (difference > const Duration(seconds: 2)) {
      // Quá 2 giây - reset và hiển thị snackbar
      _lastBackPressTime = now;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vuốt thêm lần nữa để thoát'),
          duration: Duration(seconds: 2),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return false;
    } else {
      // Trong 2 giây -> Thoát app
      SystemNavigator.pop();
      return true;
    }
  }
}
