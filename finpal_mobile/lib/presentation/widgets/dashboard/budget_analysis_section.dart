import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/utils/category_icon_helper.dart';
import '../../../data/models/dashboard_model.dart';

class BudgetAnalysisSection extends StatelessWidget {
  final List<CategoryBudget> categoryBudgets;

  const BudgetAnalysisSection({
    Key? key,
    required this.categoryBudgets,
  }) : super(key: key);

  String _formatCurrency(double amount) {
    final currencyFormat = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '',
      decimalDigits: 0,
    );
    
    if (amount >= 1000000) {
      return '${(amount / 1000000).toStringAsFixed(1)}M';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toStringAsFixed(1)}K';
    }
    return currencyFormat.format(amount);
  }

  @override
  Widget build(BuildContext context) {
    if (categoryBudgets.isEmpty) {
      return const SizedBox.shrink();
    }

    // Sắp xếp theo phần trăm giảm dần (EXCEEDED lên đầu)
    final sortedBudgets = categoryBudgets.toList()
      ..sort((a, b) => b.percentage.compareTo(a.percentage));

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0x1A000000), width: 1.12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Phân tích theo Ngân sách',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 16),
          ...sortedBudgets.map((budget) => _buildBudgetItem(budget)),
        ],
      ),
    );
  }

  Widget _buildBudgetItem(CategoryBudget budget) {
    // Xác định màu dựa trên status và percentage
    Color statusColor;
    Color statusBgColor;
    Color iconBgColor;
    
    if (budget.isExceeded) {
      statusColor = const Color(0xFFE53935); // Red
      statusBgColor = const Color(0xFFFFEBEE);
      iconBgColor = const Color(0xFFFFCDD2);
    } else if (budget.isWarning) {
      statusColor = const Color(0xFFFB8C00); // Orange
      statusBgColor = const Color(0xFFFFF3E0);
      iconBgColor = const Color(0xFFFFE0B2);
    } else {
      statusColor = const Color(0xFF43A047); // Green
      statusBgColor = const Color(0xFFE8F5E9);
      iconBgColor = const Color(0xFFC8E6C9);
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: statusBgColor,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        children: [
          // Icon với background tròn
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: iconBgColor,
              shape: BoxShape.circle,
            ),
            child: Icon(
              CategoryIconHelper.getIcon(budget.categoryName),
              color: statusColor,
              size: 22,
            ),
          ),
          const SizedBox(width: 12),
          // Content
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  budget.categoryName,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '${_formatCurrency(budget.spentAmount)} / ${_formatCurrency(budget.budgetAmount)} đ',
                  style: TextStyle(
                    fontSize: 11,
                    color: AppColors.textSecondary.withOpacity(0.8),
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 8),
                // Progress bar
                ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: LinearProgressIndicator(
                    value: budget.progress,
                    backgroundColor: Colors.white.withOpacity(0.5),
                    valueColor: AlwaysStoppedAnimation<Color>(statusColor),
                    minHeight: 8,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 12),
          // Status badge
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
            decoration: BoxDecoration(
              color: statusColor,
              borderRadius: BorderRadius.circular(16),
            ),
            child: Text(
              '${budget.percentage.toStringAsFixed(0)}%',
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
