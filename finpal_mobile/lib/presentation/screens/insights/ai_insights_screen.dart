import 'package:flutter/material.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/widgets/custom_app_bar.dart';
import '../../../core/widgets/custom_drawer.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import 'package:flutter_svg/flutter_svg.dart';

class AIInsightsScreen extends StatefulWidget {
  const AIInsightsScreen({super.key});

  @override
  State<AIInsightsScreen> createState() => _AIInsightsScreenState();
}

class _AIInsightsScreenState extends State<AIInsightsScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CustomAppBar(
        userName: 'Nguyễn Văn A',
        notificationCount: 0,
      ),
      endDrawer: const CustomDrawer(),
      body: SafeArea(
        child: Column(
          children: [
            // Header
            // _buildHeader(),

            // Content
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(10),
                child: Column(
                  children: [
                    const SizedBox(height: 16),

                    // AI Assistant Card
                    _buildAIAssistantCard(),

                    const SizedBox(height: 16),

                    // Alert Cards
                    _buildAlertCard(
                      type: AlertType.warning,
                      title: 'Cảnh báo chi tiêu',
                      message:
                          'Bạn đã chi 70% hạn mức "Ăn uống" của tháng này, chỉ còn 10 ngày nữa là hết tháng.',
                      category: 'Ăn uống',
                      action: 'Xem chi tiết →',
                    ),

                    const SizedBox(height: 12),

                    _buildAlertCard(
                      type: AlertType.suggestion,
                      title: 'Gợi ý tiết kiệm',
                      message:
                          'FinPal nhận thấy bạn chi trung bình 200.000đ cho "Trà sữa" mỗi tuần. Nếu bạn giảm còn 100.000đ, bạn sẽ tiết kiệm được 400.000đ/tháng.',
                      category: 'Ăn uống',
                      action: 'Đặt mục tiêu →',
                    ),

                    const SizedBox(height: 12),

                    _buildAlertCard(
                      type: AlertType.anomaly,
                      title: 'Phát hiện bất thường',
                      message:
                          'Hóa đơn tiền điện tháng này của bạn (500.000đ) cao hơn 30% so với trung bình (350.000đ).',
                      category: 'Hóa đơn',
                      action: 'Xem lịch sử →',
                    ),

                    const SizedBox(height: 12),

                    _buildAlertCard(
                      type: AlertType.achievement,
                      title: 'Thành tích',
                      message:
                          'Chúc mừng! Bạn đã tiết kiệm được 500.000đ so với tháng trước.',
                      category: 'Tiết kiệm',
                      action: 'Xem báo cáo →',
                    ),

                    const SizedBox(height: 16),

                    // Weekly Trend Card
                    _buildWeeklyTrendCard(),

                    const SizedBox(height: 16),

                    // Category Analysis Card
                    _buildCategoryAnalysisCard(),

                    const SizedBox(height: 16), // Bottom navigation spacing
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: CustomBottomNavBar(
        currentIndex: 3,
        onTap: (index) {
          BottomNavHelper.navigateToIndex(context, index, 3);
        },
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
              color: Colors.white.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
            child: SvgPicture.asset(
              'assets/icons/thongbao.svg',
              color: Colors.white,
              width: 24,
              height: 24,
            ),
          ),
          const SizedBox(width: 12),
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Trợ lý AI',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 14,
                    fontWeight: FontWeight.w400,
                  ),
                ),
                SizedBox(height: 4),
                Text(
                  'Có 4 thông báo mới',
                  style: TextStyle(
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

  Widget _buildAlertCard({
    required AlertType type,
    required String title,
    required String message,
    required String category,
    required String action,
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
                onPressed: () {},
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
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: Colors.black.withOpacity(0.1), width: 1.145),
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

          // Weekly spending items
          _buildWeeklyItem('T2', '☕', '150.000đ', 0.36),
          const SizedBox(height: 12),
          _buildWeeklyItem('T3', '🍜', '280.000đ', 0.67),
          const SizedBox(height: 12),
          _buildWeeklyItem('T4', '🚗', '95.000đ', 0.23),
          const SizedBox(height: 12),
          _buildWeeklyItem('T5', '🛍️', '420.000đ', 1.0),
          const SizedBox(height: 12),
          _buildWeeklyItem('T6', '🍕', '320.000đ', 0.76),
          const SizedBox(height: 12),
          _buildWeeklyItem('T7', '🎬', '180.000đ', 0.43),
          const SizedBox(height: 12),
          _buildWeeklyItem('CN', '☕', '230.000đ', 0.55),

          const SizedBox(height: 16),

          // Info box
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: const Color(0xFFDBEAFE),
              border: Border.all(color: const Color(0xFFBEDBFF), width: 1.145),
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
                  child: const Text(
                    'Bạn thường chi nhiều nhất vào thứ 5. Hãy lập kế hoạch chi tiêu cẩn thận hơn vào ngày này.',
                    style: TextStyle(
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

  Widget _buildCategoryAnalysisCard() {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: Colors.black.withOpacity(0.1), width: 1.145),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Phân tích theo danh mục',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w400,
              color: Color(0xFF030213),
            ),
          ),
          const SizedBox(height: 24),
          _buildCategoryItem(
            iconAsset: 'assets/icons/anuong.svg',
            iconColor: const Color(0xFFFB2C36),
            bgColor: const Color(0xFFFEF2F2),
            title: 'Ăn uống',
            subtitle: '3.5M / 5M đ',
            percentage: '70%',
            badgeColor: const Color(0xFFD4183D),
            progress: 0.7,
            progressBg: const Color(0xFFFFC9C9),
            progressColor: const Color(0xFFFB2C36),
          ),
          const SizedBox(height: 12),
          _buildCategoryItem(
            iconAsset: 'assets/icons/muasam.svg',
            iconColor: const Color(0xFF00C950),
            bgColor: const Color(0xFFF0FDF4),
            title: 'Mua sắm',
            subtitle: '2M / 4M đ',
            percentage: '50%',
            badgeColor: const Color(0xFFB9F8CF),
            badgeTextColor: const Color(0xFF016630),
            progress: 0.5,
            progressBg: const Color(0xFFB9F8CF),
            progressColor: const Color(0xFF00C950),
          ),
          const SizedBox(height: 12),
          _buildCategoryItem(
            iconAsset: 'assets/icons/hoadon.svg',
            iconColor: const Color(0xFFFF6900),
            bgColor: const Color(0xFFFFFBEB),
            title: 'Hóa đơn',
            subtitle: '950K / 1M đ',
            percentage: '95%',
            badgeColor: const Color(0xFFFFD6A8),
            badgeTextColor: const Color(0xFF9F2D00),
            progress: 0.95,
            progressBg: const Color(0xFFFFD6A8),
            progressColor: const Color(0xFFFF6900),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoryItem({
    IconData? icon,
    String? iconAsset,
    required Color iconColor,
    required Color bgColor,
    required String title,
    required String subtitle,
    required String percentage,
    required Color badgeColor,
    Color badgeTextColor = Colors.white,
    required double progress,
    required Color progressBg,
    required Color progressColor,
  }) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        children: [
          Row(
            children: [
              iconAsset != null
                  ? SvgPicture.asset(
                      iconAsset,
                      color: iconColor,
                      width: 20,
                      height: 20,
                    )
                  : Icon(icon, color: iconColor, size: 20),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(
                        fontSize: 14,
                        color: Color(0xFF030213),
                      ),
                    ),
                    Text(
                      subtitle,
                      style: const TextStyle(
                        fontSize: 12,
                        color: Color(0xFF4A5565),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 3),
                decoration: BoxDecoration(
                  color: badgeColor,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  percentage,
                  style: TextStyle(color: badgeTextColor, fontSize: 12),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(100),
            child: LinearProgressIndicator(
              value: progress,
              backgroundColor: progressBg,
              valueColor: AlwaysStoppedAnimation<Color>(progressColor),
              minHeight: 8,
            ),
          ),
        ],
      ),
    );
  }

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
