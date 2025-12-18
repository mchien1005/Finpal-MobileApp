import 'package:flutter/material.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'widgets/add_goal_dialog.dart';
import 'widgets/edit_goal_dialog.dart';
import 'widgets/contribute_goal.dart';
import '../../../core/widgets/success_notification_dialog.dart';
import '../../../data/services/savings_goal_service.dart';
import '../../../data/models/savings_goal_model.dart';

class SavingsGoalsScreen extends StatefulWidget {
  const SavingsGoalsScreen({super.key});

  @override
  State<SavingsGoalsScreen> createState() => _SavingsGoalsScreenState();
}

class _SavingsGoalsScreenState extends State<SavingsGoalsScreen> {
  List<SavingsGoalResponse> _goals = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadGoals();
  }

  Future<void> _loadGoals() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final goals = await SavingsGoalService.getAllSavingsGoals();
      setState(() {
        _goals = goals;
        _isLoading = false;
      });
    } catch (e) {
      print('Error loading goals: $e'); // Debug log
      setState(() {
        // Improved error message
        if (e.toString().contains('connection')) {
          _errorMessage =
              'Không thể kết nối đến server.\nVui lòng kiểm tra kết nối mạng.';
        } else {
          _errorMessage = e.toString();
        }
        _isLoading = false;
      });
    }
  }

  double get _totalTarget {
    return _goals.fold(0, (sum, goal) => sum + goal.targetAmount);
  }

  double get _totalCurrent {
    return _goals.fold(0, (sum, goal) => sum + goal.currentAmount);
  }

  double get _overallProgress {
    if (_totalTarget == 0) return 0;
    return _totalCurrent / _totalTarget;
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
        notificationCount: 3,
        backgroundColor: Colors.white,
        body: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _errorMessage != null
            ? Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      'Lỗi tải dữ liệu',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: Colors.red,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 32),
                      child: Text(
                        _errorMessage!,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 14,
                          color: Colors.grey,
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _loadGoals,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFFD7006E),
                        padding: const EdgeInsets.symmetric(
                          horizontal: 24,
                          vertical: 12,
                        ),
                      ),
                      child: const Text(
                        'Thử lại',
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  ],
                ),
              )
            : SafeArea(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Summary Card
                      _buildSummaryCard(),

                      const SizedBox(height: 16),

                      // Add Goal Button
                      _buildAddGoalButton(),

                      const SizedBox(height: 20),

                      // Goals List - Dynamic from API
                      if (_goals.isEmpty)
                        const Center(
                          child: Padding(
                            padding: EdgeInsets.all(32.0),
                            child: Text(
                              'Chưa có mục tiêu nào.\nHãy tạo mục tiêu đầu tiên!',
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontSize: 16,
                                color: Colors.grey,
                              ),
                            ),
                          ),
                        )
                      else
                        ..._goals
                            .map(
                              (goal) => Padding(
                                padding: const EdgeInsets.only(bottom: 16),
                                child: _buildGoalCardFromApi(goal),
                              ),
                            )
                            .toList(),

                      const SizedBox(height: 16),

                      // AI Suggestion Card
                      _buildAISuggestionCard(),

                      const SizedBox(height: 24),
                    ],
                  ),
                ),
              ),
        bottomNavigationBar: CustomBottomNavBar(
          currentIndex: 4,
          onTap: (index) {
            BottomNavHelper.navigateToIndex(context, index, 4);
          },
        ),
      ),
    );
  }

  Widget _buildSummaryCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF00C853), Color(0xFF00E676)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF00C853).withOpacity(0.3),
            blurRadius: 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.25),
                  shape: BoxShape.circle,
                ),
                child: SvgPicture.asset(
                  'assets/icons/muctieu_icon.svg',
                  width: 28,
                  height: 28,
                  color: Colors.white,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Tổng tiết kiệm',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${_formatCurrencyShort(_totalCurrent)} / ${_formatCurrencyShort(_totalTarget)}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          ClipRRect(
            borderRadius: BorderRadius.circular(100),
            child: LinearProgressIndicator(
              value: _overallProgress,
              backgroundColor: Colors.white.withOpacity(0.3),
              valueColor: const AlwaysStoppedAnimation<Color>(
                Color.fromRGBO(205, 5, 135, 1),
              ),
              minHeight: 8,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'Đã đạt ${(_overallProgress * 100).toInt()}% mục tiêu tổng thể',
            style: TextStyle(
              color: Colors.white.withOpacity(0.9),
              fontSize: 11,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAddGoalButton() {
    return InkWell(
      onTap: () async {
        final result = await showDialog(
          context: context,
          builder: (context) => const AddGoalDialog(),
        );

        if (result != null && mounted) {
          await showDialog(
            context: context,
            builder: (context) => const SuccessNotificationDialog(
              message: 'Thêm mục tiêu thành công!',
            ),
          );
          _loadGoals(); // Reload goals after adding
        }
      },
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            colors: [Color(0xFFEC4899), Color(0xFFDB2777)],
            begin: Alignment.centerLeft,
            end: Alignment.centerRight,
          ),
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFFEC4899).withOpacity(0.3),
              blurRadius: 8,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: const Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.add, color: Colors.white, size: 20),
            SizedBox(width: 8),
            Text(
              'Tạo mục tiêu mới',
              style: TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w600,
                color: Colors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAISuggestionCard() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFFFFF7E6), Color(0xFFFFECCC)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFFFD89C), width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: const Color(0xFFFF9800),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: SvgPicture.asset(
                  'assets/icons/dilen.svg',
                  color: Colors.white,
                  width: 24,
                  height: 24,
                ),
              ),
              const SizedBox(width: 12),
              const Expanded(
                child: Text(
                  'Gợi ý từ FinPal',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF111827),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          const Text(
            'Nếu bạn giảm chi tiêu "Ăn ngoài" xuống 20%, bạn có thể tiết kiệm thêm 700.000đ/tháng và hoàn thành mục tiêu "Tai nghe Sony" sớm hơn 1 tháng.',
            style: TextStyle(
              fontSize: 14,
              color: Color(0xFF6B5B3D),
              height: 1.5,
            ),
          ),
          const SizedBox(height: 12),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(
              onPressed: () {},
              style: TextButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 0),
              ),
              child: const Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    'Xem chi tiết',
                    style: TextStyle(
                      fontSize: 14,
                      color: Color(0xFFFF9800),
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  SizedBox(width: 4),
                  Icon(Icons.arrow_forward, size: 16, color: Color(0xFFFF9800)),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _formatCurrency(double amount) {
    if (amount >= 1000000) {
      return '${(amount / 1000000).toStringAsFixed(1)}M đ';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toStringAsFixed(0)}K đ';
    }
    return '${amount.toStringAsFixed(0)}đ';
  }

  String _formatCurrencyShort(double amount) {
    if (amount >= 1000000) {
      final millions = amount / 1000000;
      final remaining = (amount % 1000000) ~/ 1000;
      if (remaining == 0) {
        return '${millions.toInt()}.000.000 đ';
      }
      return '${millions.toInt()}.${remaining.toString().padLeft(3, '0')}.000 đ';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toInt()}.000 đ';
    }
    return '${amount.toStringAsFixed(0)} đ';
  }

  Widget _buildGoalCardFromApi(SavingsGoalResponse goal) {
    // Determine icon and color based on goal name or status
    String iconAsset = 'assets/icons/muctieu_icon.svg';
    Color iconBgColor = const Color(0xFF2196F3);

    // You can customize icon based on goal name
    if (goal.name.toLowerCase().contains('điện thoại') ||
        goal.name.toLowerCase().contains('phone') ||
        goal.name.toLowerCase().contains('iphone')) {
      iconAsset = 'assets/icons/tainghe.svg';
      iconBgColor = const Color(0xFF2196F3);
    } else if (goal.name.toLowerCase().contains('du lịch') ||
        goal.name.toLowerCase().contains('travel')) {
      iconAsset = 'assets/icons/maybay.svg';
      iconBgColor = const Color(0xFF4CAF50);
    } else if (goal.name.toLowerCase().contains('laptop') ||
        goal.name.toLowerCase().contains('máy tính')) {
      iconAsset = 'assets/icons/laptop.svg';
      iconBgColor = const Color(0xFF9810FA);
    }

    final monthlyContribution =
        goal.remainingAmount / (goal.daysRemaining / 30).ceil();

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFE5E7EB), width: 1),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header with icon and title
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: iconBgColor,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: SvgPicture.asset(
                  iconAsset,
                  width: 28,
                  height: 28,
                  color: Colors.white,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      goal.name,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF111827),
                      ),
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        const Icon(
                          Icons.access_time,
                          size: 14,
                          color: Color(0xFF6B7280),
                        ),
                        const SizedBox(width: 4),
                        Text(
                          'Còn ${goal.daysRemaining} ngày • ${goal.deadline}',
                          style: const TextStyle(
                            fontSize: 12,
                            color: Color(0xFF6B7280),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),

          const SizedBox(height: 16),

          // Progress section
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Tiến độ',
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w500,
                  color: Color(0xFF6B7280),
                ),
              ),
              Text(
                '${goal.progressPercentage.toInt()}%',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: iconBgColor,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(100),
            child: LinearProgressIndicator(
              value: goal.progressPercentage / 100,
              backgroundColor: const Color(0xFFE5E7EB),
              valueColor: AlwaysStoppedAnimation<Color>(iconBgColor),
              minHeight: 6,
            ),
          ),
          const SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                _formatCurrencyShort(goal.currentAmount),
                style: const TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w500,
                  color: Color(0xFF111827),
                ),
              ),
              Text(
                _formatCurrencyShort(goal.targetAmount),
                style: const TextStyle(fontSize: 13, color: Color(0xFF6B7280)),
              ),
            ],
          ),

          const SizedBox(height: 16),

          // Details section
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Còn lại',
                      style: TextStyle(fontSize: 12, color: Color(0xFF6B7280)),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _formatCurrency(goal.remainingAmount),
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: Color(0xFF111827),
                      ),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    const Text(
                      'Góp mỗi tháng',
                      style: TextStyle(fontSize: 12, color: Color(0xFF6B7280)),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _formatCurrency(monthlyContribution),
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: Color(0xFF111827),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),

          const SizedBox(height: 16),

          // Action buttons
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: () async {
                    final result = await showDialog(
                      context: context,
                      builder: (context) => EditGoalDialog(
                        goalId: goal.id,
                        goalName: goal.name,
                        targetAmount: goal.targetAmount,
                        savedAmount: goal.currentAmount,
                        deadline: DateTime.parse(goal.deadline),
                        monthlyContribution: monthlyContribution,
                      ),
                    );

                    if (result != null && mounted) {
                      await showDialog(
                        context: context,
                        builder: (context) => const SuccessNotificationDialog(
                          message: 'Cập nhật mục tiêu thành công!',
                        ),
                      );
                      _loadGoals();
                    }
                  },
                  style: OutlinedButton.styleFrom(
                    side: const BorderSide(color: Color(0xFFE5E7EB)),
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: const Text(
                    'Chỉnh sửa',
                    style: TextStyle(
                      fontSize: 14,
                      color: Color(0xFF374151),
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: ElevatedButton(
                  onPressed: () async {
                    final result = await showDialog(
                      context: context,
                      builder: (context) => ContributeGoalDialog(
                        goalId: goal.id,
                        goalName: goal.name,
                        currentAmount: goal.currentAmount,
                        targetAmount: goal.targetAmount,
                        monthlyContribution: monthlyContribution,
                      ),
                    );

                    if (result != null && mounted) {
                      await showDialog(
                        context: context,
                        builder: (context) => const SuccessNotificationDialog(
                          message: 'Góp tiền thành công!',
                        ),
                      );
                      _loadGoals();
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: iconBgColor,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                    elevation: 0,
                  ),
                  child: const Text(
                    'Góp tiền',
                    style: TextStyle(
                      fontSize: 14,
                      color: Colors.white,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
