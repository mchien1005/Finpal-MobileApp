import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/category_icon_helper.dart';
import '../../../data/models/budget_model.dart';
import '../../../data/services/budget_service.dart';

/// Card hiển thị tổng quan ngân sách trên dashboard
/// Hiển thị: Tổng ngân sách, đã chi, còn lại, và top 3 ngân sách nổi bật
class BudgetAnalysisSection extends StatefulWidget {
  /// Callback khi người dùng nhấn "Xem tất cả"
  final VoidCallback? onViewAll;

  const BudgetAnalysisSection({super.key, this.onViewAll});

  @override
  State<BudgetAnalysisSection> createState() => _BudgetAnalysisSectionState();
}

class _BudgetAnalysisSectionState extends State<BudgetAnalysisSection> {
  List<BudgetResponse> _budgets = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadBudgets();
  }

  Future<void> _loadBudgets() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final budgets = await BudgetService.getAllBudgets();

      // Lấy tháng hiện tại
      final now = DateTime.now();
      final currentMonthStart = DateTime(now.year, now.month, 1);
      final currentMonthEnd = DateTime(
        now.year,
        now.month + 1,
        0,
      ); // Ngày cuối tháng

      // Lọc ngân sách:
      // 1. Active
      // 2. Nằm trong tháng hiện tại (startDate <= currentMonthEnd && (endDate == null || endDate >= currentMonthStart))
      final currentMonthBudgets = budgets.where((b) {
        if (!b.isActive) return false;

        // Kiểm tra ngân sách có overlap với tháng hiện tại không
        final budgetStart = b.startDate;
        final budgetEnd = b.endDate;

        // Ngân sách bắt đầu trước hoặc trong tháng hiện tại
        final startsBeforeOrInMonth =
            budgetStart.isBefore(currentMonthEnd) ||
            budgetStart.isAtSameMomentAs(currentMonthEnd) ||
            (budgetStart.year == currentMonthEnd.year &&
                budgetStart.month == currentMonthEnd.month);

        // Ngân sách kết thúc sau hoặc trong tháng hiện tại (hoặc không có ngày kết thúc)
        final endsAfterOrInMonth =
            budgetEnd == null ||
            budgetEnd.isAfter(currentMonthStart) ||
            budgetEnd.isAtSameMomentAs(currentMonthStart) ||
            (budgetEnd.year == currentMonthStart.year &&
                budgetEnd.month == currentMonthStart.month);

        return startsBeforeOrInMonth && endsAfterOrInMonth;
      }).toList();

      setState(() {
        _budgets = currentMonthBudgets;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  String _formatCurrency(double amount) {
    if (amount >= 1000000000) {
      return '${(amount / 1000000000).toStringAsFixed(1)}B';
    } else if (amount >= 1000000) {
      return '${(amount / 1000000).toStringAsFixed(1)}M';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toStringAsFixed(0)}K';
    }
    return NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '',
      decimalDigits: 0,
    ).format(amount);
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return _buildLoadingCard();
    }

    if (_error != null) {
      return _buildErrorCard();
    }

    if (_budgets.isEmpty) {
      return _buildEmptyCard();
    }

    // Tính tổng các giá trị
    final totalBudget = _budgets.fold<double>(0, (sum, b) => sum + b.amount);
    final totalSpent = _budgets.fold<double>(
      0,
      (sum, b) => sum + b.spentAmount,
    );
    final totalRemaining = totalBudget - totalSpent;
    final overallProgress = totalBudget > 0 ? (totalSpent / totalBudget) : 0.0;

    // Lấy top 3 ngân sách có phần trăm tiêu dùng cao nhất
    final sortedBudgets = _budgets.toList()
      ..sort((a, b) => b.progressPercentage.compareTo(a.progressPercentage));
    final topBudgets = sortedBudgets.take(3).toList();

    // Đếm số ngân sách vượt và sắp vượt
    final exceededCount = _budgets.where((b) => b.status == 'EXCEEDED').length;
    final warningCount = _budgets.where((b) => b.status == 'WARNING').length;

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.08),
            blurRadius: 16,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Padding(
            padding: const EdgeInsets.all(20),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          gradient: const LinearGradient(
                            colors: [Color(0xFFD7006E), Color(0xFFFF6B9D)],
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                          ),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Icon(
                          Icons.account_balance_wallet,
                          color: Colors.white,
                          size: 20,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Ngân sách tháng này',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                                color: AppColors.textPrimary,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            Text(
                              'Tháng ${DateTime.now().month}/${DateTime.now().year}',
                              style: TextStyle(
                                fontSize: 12,
                                color: AppColors.textSecondary,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                if (widget.onViewAll != null)
                  GestureDetector(
                    onTap: widget.onViewAll,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: const Color(0xFFD7006E).withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: const Text(
                        'Xem tất cả',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                          color: Color(0xFFD7006E),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),

          // Tổng quan số liệu
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    const Color(0xFFD7006E).withValues(alpha: 0.05),
                    const Color(0xFFFF6B9D).withValues(alpha: 0.05),
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(
                  color: const Color(0xFFD7006E).withValues(alpha: 0.1),
                ),
              ),
              child: Column(
                children: [
                  // Progress bar tổng hợp
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'Đã chi: ${_formatCurrency(totalSpent)}đ',
                        style: const TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: AppColors.textPrimary,
                        ),
                      ),
                      Text(
                        'Tổng: ${_formatCurrency(totalBudget)}đ',
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w500,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  // Progress bar
                  ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: LinearProgressIndicator(
                      value: overallProgress.clamp(0.0, 1.0),
                      backgroundColor: Colors.grey.shade200,
                      valueColor: AlwaysStoppedAnimation<Color>(
                        _getProgressColor(overallProgress * 100),
                      ),
                      minHeight: 10,
                    ),
                  ),
                  const SizedBox(height: 12),
                  // Thống kê nhanh
                  Row(
                    children: [
                      Expanded(
                        child: _buildStatItem(
                          '💰',
                          'Còn lại',
                          '${_formatCurrency(totalRemaining)}đ',
                          totalRemaining >= 0 ? Colors.green : Colors.red,
                        ),
                      ),
                      Container(
                        width: 1,
                        height: 40,
                        color: Colors.grey.shade200,
                      ),
                      Expanded(
                        child: _buildStatItem(
                          '📊',
                          'Đã dùng',
                          '${(overallProgress * 100).toStringAsFixed(0)}%',
                          _getProgressColor(overallProgress * 100),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),

          // Cảnh báo nếu có ngân sách vượt hoặc sắp vượt
          if (exceededCount > 0 || warningCount > 0)
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
              child: Row(
                children: [
                  if (exceededCount > 0) ...[
                    _buildAlertBadge('🚨', '$exceededCount vượt', Colors.red),
                    const SizedBox(width: 8),
                  ],
                  if (warningCount > 0)
                    _buildAlertBadge(
                      '⚠️',
                      '$warningCount sắp vượt',
                      Colors.orange,
                    ),
                ],
              ),
            ),

          // Top ngân sách
          if (topBudgets.isNotEmpty) ...[
            const Padding(
              padding: EdgeInsets.fromLTRB(20, 16, 20, 8),
              child: Text(
                'Ngân sách cần chú ý',
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: AppColors.textSecondary,
                ),
              ),
            ),
            ...topBudgets.map((budget) => _buildBudgetItem(budget)),
          ],

          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildStatItem(String emoji, String label, String value, Color color) {
    return Column(
      children: [
        Text(emoji, style: const TextStyle(fontSize: 18)),
        const SizedBox(height: 4),
        Text(
          label,
          style: TextStyle(fontSize: 11, color: AppColors.textSecondary),
        ),
        const SizedBox(height: 2),
        Text(
          value,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
      ],
    );
  }

  Widget _buildAlertBadge(String emoji, String text, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(emoji, style: const TextStyle(fontSize: 12)),
          const SizedBox(width: 4),
          Text(
            text,
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBudgetItem(BudgetResponse budget) {
    final emoji = CategoryIconHelper.getEmoji(budget.categoryIcon);
    final progressColor = _getProgressColor(budget.progressPercentage);
    final progress = (budget.progressPercentage / 100).clamp(0.0, 1.0);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: progressColor.withValues(alpha: 0.08),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: progressColor.withValues(alpha: 0.2)),
        ),
        child: Row(
          children: [
            // Emoji
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(10),
                boxShadow: [
                  BoxShadow(
                    color: progressColor.withValues(alpha: 0.2),
                    blurRadius: 4,
                    offset: const Offset(0, 2),
                  ),
                ],
              ),
              child: Center(
                child: Text(emoji, style: const TextStyle(fontSize: 20)),
              ),
            ),
            const SizedBox(width: 12),
            // Content
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    budget.categoryName ?? budget.name,
                    style: const TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: AppColors.textPrimary,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6),
                  // Mini progress bar
                  ClipRRect(
                    borderRadius: BorderRadius.circular(4),
                    child: LinearProgressIndicator(
                      value: progress,
                      backgroundColor: Colors.white,
                      valueColor: AlwaysStoppedAnimation<Color>(progressColor),
                      minHeight: 6,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            // Percentage
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: progressColor,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                '${budget.progressPercentage.toStringAsFixed(0)}%',
                style: const TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getProgressColor(double percentage) {
    if (percentage >= 100) return Colors.red;
    if (percentage >= 80) return Colors.orange;
    if (percentage >= 60) return Colors.amber.shade700;
    return const Color(0xFF43A047);
  }

  Widget _buildLoadingCard() {
    return Container(
      padding: const EdgeInsets.all(40),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.08),
            blurRadius: 16,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: const Center(
        child: CircularProgressIndicator(color: Color(0xFFD7006E)),
      ),
    );
  }

  Widget _buildErrorCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.08),
            blurRadius: 16,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          const Text('❌', style: TextStyle(fontSize: 32)),
          const SizedBox(height: 8),
          const Text(
            'Không thể tải ngân sách',
            style: TextStyle(color: AppColors.textSecondary),
          ),
          const SizedBox(height: 12),
          TextButton(onPressed: _loadBudgets, child: const Text('Thử lại')),
        ],
      ),
    );
  }

  Widget _buildEmptyCard() {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.08),
            blurRadius: 16,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFFD7006E).withValues(alpha: 0.1),
              shape: BoxShape.circle,
            ),
            child: const Text('💰', style: TextStyle(fontSize: 32)),
          ),
          const SizedBox(height: 16),
          const Text(
            'Chưa có ngân sách',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Tạo ngân sách để quản lý chi tiêu tốt hơn',
            style: TextStyle(fontSize: 13, color: AppColors.textSecondary),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}
