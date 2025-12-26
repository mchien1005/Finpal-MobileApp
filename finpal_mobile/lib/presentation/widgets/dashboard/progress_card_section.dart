import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/constants/app_colors.dart';
import '../../../data/models/dashboard_model.dart';

/// Section hiển thị thanh progress chi tiêu
class ProgressCardSection extends StatelessWidget {
  final DashboardSummary? summary;
  final BudgetItem? activeBudget;
  final CashFlow? cashFlow;

  const ProgressCardSection({
    super.key,
    required this.summary,
    required this.activeBudget,
    required this.cashFlow,
  });

  String _formatCurrency(double amount) {
    return NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '',
      decimalDigits: 0,
    ).format(amount);
  }

  @override
  Widget build(BuildContext context) {
    // Lấy dữ liệu - ưu tiên theo thứ tự: summary > cashFlow > activeBudget
    double totalExpense = 0;
    double totalIncome = 0;

    // Debug log
    print('📊 ProgressCardSection: Building...');
    print('   - summary: ${summary != null}');
    print('   - cashFlow: ${cashFlow != null}');
    print('   - activeBudget: ${activeBudget != null}');

    // Lấy dữ liệu từ summary (đây là nguồn chính)
    if (summary != null) {
      totalExpense = summary!.totalExpense;
      totalIncome = summary!.totalIncome;
      print('   - From summary: Income=$totalIncome, Expense=$totalExpense');
    }

    // Nếu summary không có dữ liệu, fallback về cashFlow
    if (totalIncome == 0 && totalExpense == 0 && cashFlow != null) {
      totalExpense = cashFlow!.totalExpense;
      totalIncome = cashFlow!.totalIncome;
      print('   - From cashFlow: Income=$totalIncome, Expense=$totalExpense');
    }

    // Tính phần trăm (chi tiêu / thu nhập)
    // Nếu thu nhập = 0, dùng chi tiêu làm mẫu số (100%)
    double percentage;
    double progress;

    if (totalIncome > 0) {
      percentage = (totalExpense / totalIncome * 100);
      progress = (totalExpense / totalIncome).clamp(0.0, 1.0);
    } else if (totalExpense > 0) {
      percentage = 100.0;
      progress = 1.0;
    } else {
      percentage = 0.0;
      progress = 0.0;
    }

    print('   - Calculated: Progress=$progress, Percentage=$percentage%');

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
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Tiêu đề
            const Text(
              'Tình hình chi tiêu tháng này',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 20),

            // Label và phần trăm
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Đã chi',
                  style: TextStyle(
                    fontSize: 14,
                    color: AppColors.textSecondary,
                  ),
                ),
                Text(
                  '${percentage.toStringAsFixed(1)}%',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),

            // Thanh progress
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: LinearProgressIndicator(
                value: progress,
                backgroundColor: Colors.grey.shade200,
                valueColor: const AlwaysStoppedAnimation<Color>(
                  Color(0xFF6C63FF), // Màu tím như trong hình
                ),
                minHeight: 12,
              ),
            ),
            const SizedBox(height: 12),

            // Số tiền - hiển thị chi tiêu / thu nhập
            Center(
              child: Text(
                totalIncome > 0 || totalExpense > 0
                    ? '${_formatCurrency(totalExpense)} / ${_formatCurrency(totalIncome)} VNĐ'
                    : 'Chưa có dữ liệu',
                style: TextStyle(fontSize: 14, color: AppColors.textSecondary),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
