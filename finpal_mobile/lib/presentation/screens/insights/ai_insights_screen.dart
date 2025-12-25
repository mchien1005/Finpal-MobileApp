import 'package:flutter/material.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../../data/services/ai_insights_service.dart';
import '../../../data/services/notification_service.dart';
import '../../../data/services/storage_service.dart';
import '../../../data/models/weekly_spending_trend_model.dart';
import '../../../data/models/notification_model.dart';
import 'package:intl/intl.dart';

class AIInsightsScreen extends StatefulWidget {
  const AIInsightsScreen({super.key});

  @override
  State<AIInsightsScreen> createState() => _AIInsightsScreenState();
}

class _AIInsightsScreenState extends State<AIInsightsScreen> {
  final AIInsightsService _aiInsightsService = AIInsightsService();
  final NotificationService _notificationService = NotificationService();
  final StorageService _storageService = StorageService();

  bool _isLoading = true;
  int _unreadCount = 0;
  List<NotificationModel> _notifications = [];
  WeeklySpendingTrendResponse? _weeklyTrend;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final userData = await _storageService.getUserData();
      print('👤 AI Insights: User data loaded: $userData');

      if (userData == null) {
        setState(() {
          _errorMessage = 'Vui lòng đăng nhập lại';
          _isLoading = false;
        });
        return;
      }

      final userId = userData['id'] as int;
      print('🆔 AI Insights: User ID: $userId');

      // Chỉ lấy các loại thông báo liên quan đến AI insights
      final aiNotificationTypes = [
        'SAVINGS_SUGGESTION',
        'ANOMALY_ALERT',
        'SPENDING_ACHIEVEMENT',
        'SPENDING_TIP',
        'SMART_TIP',
      ];

      // Load data in parallel
      print('📡 AI Insights: Loading data...');
      final results = await Future.wait([
        _notificationService.getUnreadCount(),
        _notificationService.getNotifications(
          isRead: false,
          types: aiNotificationTypes,
        ),
        _aiInsightsService.getWeeklySpendingTrend(userId),
      ]);

      print('✅ AI Insights: Data loaded successfully');
      setState(() {
        _unreadCount = results[0] as int;
        _notifications = results[1] as List<NotificationModel>;
        _weeklyTrend = results[2] as WeeklySpendingTrendResponse;
        _isLoading = false;
      });
    } catch (e, stackTrace) {
      print('❌ AI Insights Error: $e');
      print('📍 Stack trace: $stackTrace');
      setState(() {
        _errorMessage = 'Không thể tải dữ liệu: ${e.toString()}';
        _isLoading = false;
      });
    }
  }

  Future<void> _deleteNotification(int notificationId) async {
    try {
      print('🗑️ Deleting notification: $notificationId');
      await _notificationService.deleteNotification(notificationId);

      // Remove from local list and update count
      setState(() {
        _notifications.removeWhere((n) => n.id == notificationId);
        _unreadCount = _notifications.length;
      });

      print('✅ Notification deleted successfully');
    } catch (e) {
      print('❌ Failed to delete notification: $e');
      // Show error message to user
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Không thể xóa thông báo: ${e.toString()}'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop) return;
        await BottomNavHelper.handleBackButton(context);
      },
      child: AppBarWithDrawer.scrollable(
        context,
        userName: 'Nguyễn Văn A',
        notificationCount: _unreadCount,
        backgroundColor: Colors.white,
        customTitle: 'AI gợi ý',
        body: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _errorMessage != null
            ? Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(_errorMessage!),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _loadData,
                      child: const Text('Thử lại'),
                    ),
                  ],
                ),
              )
            : Padding(
                padding: const EdgeInsets.all(10),
                child: Column(
                  children: [
                    const SizedBox(height: 16),

                    // AI Assistant Card
                    _buildAIAssistantCard(),

                    const SizedBox(height: 16),

                    // Alert Cards from API
                    ..._notifications
                        .take(4)
                        .map(
                          (notification) => Padding(
                            padding: const EdgeInsets.only(bottom: 12),
                            child: _buildAlertCardFromNotification(
                              notification,
                            ),
                          ),
                        ),

                    const SizedBox(height: 16),

                    // Weekly Trend Card
                    _buildWeeklyTrendCard(),

                    const SizedBox(height: 16),

                    // Category Analysis Card
                    // _buildCategoryAnalysisCard(),
                    const SizedBox(height: 16), // Bottom navigation spacing
                  ],
                ),
              ),
        bottomNavigationBar: CustomBottomNavBar(
          currentIndex: 3,
          onTap: (index) {
            BottomNavHelper.navigateToIndex(context, index, 3);
          },
        ),
      ),
    );
  }

  Widget _buildAIAssistantCard() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFFAD46FF), Color(0xFFF6339A)],
          begin: Alignment.centerLeft,
          end: Alignment.centerRight,
        ),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.2),
              shape: BoxShape.circle,
            ),
            child: SvgPicture.asset(
              'assets/icons/thongbao.svg',
              colorFilter: const ColorFilter.mode(
                Colors.white,
                BlendMode.srcIn,
              ),
              width: 24,
              height: 24,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Trợ lý AI',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 14,
                    fontWeight: FontWeight.w400,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  'Có $_unreadCount thông báo mới',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 16,
                    fontWeight: FontWeight.w400,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAlertCardFromNotification(NotificationModel notification) {
    final type = _getAlertTypeFromNotification(notification.type);
    return _buildAlertCard(
      type: type,
      title: notification.title,
      message: notification.content,
      category: _getCategoryFromType(notification.type),
      action: 'Xem chi tiết →',
      notificationId: notification.id,
    );
  }

  AlertType _getAlertTypeFromNotification(String type) {
    switch (type) {
      case 'BUDGET_WARNING':
      case 'BUDGET_EXCEEDED':
        return AlertType.warning;
      case 'SAVINGS_SUGGESTION':
      case 'SPENDING_TIP':
        return AlertType.suggestion;
      case 'ANOMALY_ALERT':
        return AlertType.anomaly;
      case 'GOAL_COMPLETED':
      case 'SPENDING_ACHIEVEMENT':
        return AlertType.achievement;
      default:
        return AlertType.suggestion;
    }
  }

  String _getCategoryFromType(String type) {
    switch (type) {
      case 'BUDGET_WARNING':
      case 'BUDGET_EXCEEDED':
        return 'Ngân sách';
      case 'SAVINGS_SUGGESTION':
        return 'Tiết kiệm';
      case 'SPENDING_TIP':
        return 'Chi tiêu';
      case 'ANOMALY_ALERT':
        return 'Bất thường';
      case 'GOAL_COMPLETED':
      case 'SPENDING_ACHIEVEMENT':
        return 'Thành tích';
      default:
        return 'Thông báo';
    }
  }

  Widget _buildAlertCard({
    required AlertType type,
    required String title,
    required String message,
    required String category,
    required String action,
    int? notificationId,
  }) {
    final config = _getAlertConfig(type);

    return Container(
      padding: const EdgeInsets.all(17),
      decoration: BoxDecoration(
        color: config.bgColor,
        border: Border.all(color: config.borderColor, width: 1.145),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(config.icon, color: config.textColor, size: 20),
                  const SizedBox(width: 12),
                  Text(
                    title,
                    style: TextStyle(
                      color: config.textColor,
                      fontSize: 14,
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                ],
              ),
              IconButton(
                icon: Icon(Icons.close, color: config.textColor, size: 16),
                onPressed: notificationId != null
                    ? () => _deleteNotification(notificationId)
                    : null,
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
              ),
            ],
          ),

          const SizedBox(height: 8),

          // Message
          Text(
            message,
            style: TextStyle(
              color: config.textColor,
              fontSize: 14,
              height: 1.4,
            ),
          ),

          const SizedBox(height: 8),

          // Category and Action
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 3),
                decoration: BoxDecoration(
                  color: const Color(0xFFECEEF2),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  category,
                  style: const TextStyle(
                    color: Color(0xFF030213),
                    fontSize: 12,
                  ),
                ),
              ),
              const SizedBox(width: 8),
              InkWell(
                onTap: () {},
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 6,
                  ),
                  child: Text(
                    action,
                    style: TextStyle(color: config.textColor, fontSize: 12),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildWeeklyTrendCard() {
    if (_weeklyTrend == null) {
      return const SizedBox.shrink();
    }

    final dailySpending = _weeklyTrend!.dailySpending;
    final maxAmount = dailySpending.isEmpty
        ? 1.0
        : dailySpending
              .map((d) => d.totalSpending)
              .reduce((a, b) => a > b ? a : b);

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(
          color: Colors.black.withValues(alpha: 0.1),
          width: 1.145,
        ),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Xu hướng chi tiêu tuần này',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w400,
              color: Color(0xFF030213),
            ),
          ),
          const SizedBox(height: 24),

          // Weekly spending items from API
          ...dailySpending.asMap().entries.map((entry) {
            final daily = entry.value;
            final progress = maxAmount > 0
                ? daily.totalSpending / maxAmount
                : 0.0;
            final formatter = NumberFormat('#,###', 'vi_VN');

            return Column(
              children: [
                if (entry.key > 0) const SizedBox(height: 12),
                _buildWeeklyItem(
                  daily.dayName,
                  daily.emoji,
                  '${formatter.format(daily.totalSpending.toDouble())}đ',
                  progress,
                ),
              ],
            );
          }),

          const SizedBox(height: 16),

          // Info box with insight from API
          if (_weeklyTrend!.insight.message.isNotEmpty)
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: const Color(0xFFDBEAFE),
                border: Border.all(
                  color: const Color(0xFFBEDBFF),
                  width: 1.145,
                ),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Icon(
                    Icons.info_outline,
                    color: Color(0xFF193CB8),
                    size: 16,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      _weeklyTrend!.insight.message,
                      style: const TextStyle(
                        color: Color(0xFF193CB8),
                        fontSize: 14,
                        height: 1.4,
                      ),
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildWeeklyItem(
    String day,
    String emoji,
    String amount,
    double progress,
  ) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Text(
                  day,
                  style: const TextStyle(
                    fontSize: 12,
                    color: Color(0xFF030213),
                  ),
                ),
                const SizedBox(width: 8),
                Text(emoji, style: const TextStyle(fontSize: 18)),
              ],
            ),
            Text(
              amount,
              style: const TextStyle(fontSize: 14, color: Color(0xFF030213)),
            ),
          ],
        ),
        const SizedBox(height: 4),
        ClipRRect(
          borderRadius: BorderRadius.circular(100),
          child: LinearProgressIndicator(
            value: progress,
            backgroundColor: const Color(0xFFE5E7EB),
            valueColor: const AlwaysStoppedAnimation<Color>(Color(0xFF2B7FFF)),
            minHeight: 8,
          ),
        ),
      ],
    );
  }

  // Widget _buildCategoryAnalysisCard() {
  //   return Container(
  //     padding: const EdgeInsets.all(24),
  //     decoration: BoxDecoration(
  //       color: Colors.white,
  //       border: Border.all(color: Colors.black.withOpacity(0.1), width: 1.145),
  //       borderRadius: BorderRadius.circular(14),
  //     ),
  //     child: Column(
  //       crossAxisAlignment: CrossAxisAlignment.start,
  //       children: [
  //         const Text(
  //           'Phân tích theo danh mục',
  //           style: TextStyle(
  //             fontSize: 16,
  //             fontWeight: FontWeight.w400,
  //             color: Color(0xFF030213),
  //           ),
  //         ),
  //         const SizedBox(height: 24),
  //         _buildCategoryItem(
  //           iconAsset: 'assets/icons/anuong.svg',
  //           iconColor: const Color(0xFFFB2C36),
  //           bgColor: const Color(0xFFFEF2F2),
  //           title: 'Ăn uống',
  //           subtitle: '3.5M / 5M đ',
  //           percentage: '70%',
  //           badgeColor: const Color(0xFFD4183D),
  //           progress: 0.7,
  //           progressBg: const Color(0xFFFFC9C9),
  //           progressColor: const Color(0xFFFB2C36),
  //         ),
  //         const SizedBox(height: 12),
  //         _buildCategoryItem(
  //           iconAsset: 'assets/icons/muasam.svg',
  //           iconColor: const Color(0xFF00C950),
  //           bgColor: const Color(0xFFF0FDF4),
  //           title: 'Mua sắm',
  //           subtitle: '2M / 4M đ',
  //           percentage: '50%',
  //           badgeColor: const Color(0xFFB9F8CF),
  //           badgeTextColor: const Color(0xFF016630),
  //           progress: 0.5,
  //           progressBg: const Color(0xFFB9F8CF),
  //           progressColor: const Color(0xFF00C950),
  //         ),
  //         const SizedBox(height: 12),
  //         _buildCategoryItem(
  //           iconAsset: 'assets/icons/hoadon.svg',
  //           iconColor: const Color(0xFFFF6900),
  //           bgColor: const Color(0xFFFFFBEB),
  //           title: 'Hóa đơn',
  //           subtitle: '950K / 1M đ',
  //           percentage: '95%',
  //           badgeColor: const Color(0xFFFFD6A8),
  //           badgeTextColor: const Color(0xFF9F2D00),
  //           progress: 0.95,
  //           progressBg: const Color(0xFFFFD6A8),
  //           progressColor: const Color(0xFFFF6900),
  //         ),
  //       ],
  //     ),
  //   );
  // }

  // Widget _buildCategoryItem({
  //   IconData? icon,
  //   String? iconAsset,
  //   required Color iconColor,
  //   required Color bgColor,
  //   required String title,
  //   required String subtitle,
  //   required String percentage,
  //   required Color badgeColor,
  //   Color badgeTextColor = Colors.white,
  //   required double progress,
  //   required Color progressBg,
  //   required Color progressColor,
  // }) {
  //   return Container(
  //     padding: const EdgeInsets.all(12),
  //     decoration: BoxDecoration(
  //       color: bgColor,
  //       borderRadius: BorderRadius.circular(10),
  //     ),
  //     child: Column(
  //       children: [
  //         Row(
  //           children: [
  //             iconAsset != null
  //                 ? SvgPicture.asset(
  //                     iconAsset,
  //                     color: iconColor,
  //                     width: 20,
  //                     height: 20,
  //                   )
  //                 : Icon(icon, color: iconColor, size: 20),
  //             const SizedBox(width: 12),
  //             Expanded(
  //               child: Column(
  //                 crossAxisAlignment: CrossAxisAlignment.start,
  //                 children: [
  //                   Text(
  //                     title,
  //                     style: const TextStyle(
  //                       fontSize: 14,
  //                       color: Color(0xFF030213),
  //                     ),
  //                   ),
  //                   Text(
  //                     subtitle,
  //                     style: const TextStyle(
  //                       fontSize: 12,
  //                       color: Color(0xFF4A5565),
  //                     ),
  //                   ),
  //                 ],
  //               ),
  //             ),
  //             Container(
  //               padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 3),
  //               decoration: BoxDecoration(
  //                 color: badgeColor,
  //                 borderRadius: BorderRadius.circular(8),
  //               ),
  //               child: Text(
  //                 percentage,
  //                 style: TextStyle(color: badgeTextColor, fontSize: 12),
  //               ),
  //             ),
  //           ],
  //         ),
  //         const SizedBox(height: 8),
  //         ClipRRect(
  //           borderRadius: BorderRadius.circular(100),
  //           child: LinearProgressIndicator(
  //             value: progress,
  //             backgroundColor: progressBg,
  //             valueColor: AlwaysStoppedAnimation<Color>(progressColor),
  //             minHeight: 8,
  //           ),
  //         ),
  //       ],
  //     ),
  //   );
  // }

  AlertConfig _getAlertConfig(AlertType type) {
    switch (type) {
      case AlertType.warning:
        return AlertConfig(
          bgColor: const Color(0xFFFEF2F2),
          borderColor: const Color(0xFFFFC9C9),
          textColor: const Color(0xFF9F0712),
          icon: Icons.warning_amber_rounded,
        );
      case AlertType.suggestion:
        return AlertConfig(
          bgColor: const Color(0xFFFFFBEB),
          borderColor: const Color(0xFFFEE685),
          textColor: const Color(0xFF973C00),
          icon: Icons.lightbulb_outline,
        );
      case AlertType.anomaly:
        return AlertConfig(
          bgColor: const Color(0xFFFFF7ED),
          borderColor: const Color(0xFFFFD6A7),
          textColor: const Color(0xFF9F2D00),
          icon: Icons.trending_up,
        );
      case AlertType.achievement:
        return AlertConfig(
          bgColor: const Color(0xFFF0FDF4),
          borderColor: const Color(0xFFB9F8CF),
          textColor: const Color(0xFF016630),
          icon: Icons.check_circle_outline,
        );
    }
  }
}

enum AlertType { warning, suggestion, anomaly, achievement }

class AlertConfig {
  final Color bgColor;
  final Color borderColor;
  final Color textColor;
  final IconData icon;

  AlertConfig({
    required this.bgColor,
    required this.borderColor,
    required this.textColor,
    required this.icon,
  });
}
