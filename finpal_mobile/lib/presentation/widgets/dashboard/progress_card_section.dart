import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../data/models/dashboard_model.dart';
import '../dashboard_card.dart';

class ProgressCardSection extends StatelessWidget {
  final DashboardSummary? summary;
  final BudgetItem? activeBudget;
  final CashFlow? cashFlow;

  const ProgressCardSection({
    Key? key,
    required this.summary,
    required this.activeBudget,
    required this.cashFlow,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '',
      decimalDigits: 0,
    );

    // Ưu tiên sử dụng cashFlow từ API /dashboard/cash-flow
    if (cashFlow != null) {
      // Nếu có budget limit, hiển thị progress
      if (cashFlow!.budgetLimit > 0) {
        return ProgressCard(
          title: 'Tình hình chi tiêu tháng này',
          progressValue: cashFlow!.budgetProgress,
          progressText: '${cashFlow!.budgetPercentage.toStringAsFixed(1)}%',
          amountText: '${currencyFormat.format(cashFlow!.budgetUsed)} / ${currencyFormat.format(cashFlow!.budgetLimit)} VNĐ',
        );
      }
      // Nếu không có budget nhưng có chi tiêu, hiển thị chi tiêu thực tế
      else if (cashFlow!.totalExpense > 0) {
        return ProgressCard(
          title: 'Chi tiêu tháng này',
          progressValue: 0.0,
          progressText: '${currencyFormat.format(cashFlow!.totalExpense)} VNĐ',
          amountText: 'Chưa thiết lập ngân sách',
        );
      }
    }
    
    // Fallback về activeBudget
    if (activeBudget != null) {
      return ProgressCard(
        title: 'Tình hình chi tiêu tháng này',
        progressValue: activeBudget!.progress.clamp(0.0, 1.0),
        progressText: '${activeBudget!.percentage.toStringAsFixed(1)}%',
        amountText: '${currencyFormat.format(activeBudget!.spent)} / ${currencyFormat.format(activeBudget!.amount)} VNĐ',
      );
    }
    
    // Fallback về summary
    final used = summary?.budgetUsed ?? summary?.totalExpense ?? 0.0;
    final limit = summary?.budgetLimit ?? 0.0;
    
    if (limit > 0) {
      final progress = used / limit;
      final percentage = progress * 100;
      return ProgressCard(
        title: 'Tình hình chi tiêu tháng này',
        progressValue: progress.clamp(0.0, 1.0),
        progressText: '${percentage.toStringAsFixed(1)}%',
        amountText: '${currencyFormat.format(used)} / ${currencyFormat.format(limit)} VNĐ',
      );
    }
    
    // Hiển thị chi tiêu thực tế nếu có
    if (used > 0) {
      return ProgressCard(
        title: 'Chi tiêu tháng này',
        progressValue: 0.0,
        progressText: '${currencyFormat.format(used)} VNĐ',
        amountText: 'Chưa thiết lập ngân sách',
      );
    }
    
    // Hoàn toàn không có dữ liệu
    return ProgressCard(
      title: 'Tình hình chi tiêu tháng này',
      progressValue: 0.0,
      progressText: '0 VNĐ',
      amountText: 'Chưa có dữ liệu chi tiêu',
    );
  }
}
