import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/models/dashboard_model.dart';
import '../dashboard_card.dart';

class SummaryCardsSection extends StatelessWidget {
  final DashboardSummary? summary;

  const SummaryCardsSection({
    Key? key,
    required this.summary,
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
    final hasData = (summary?.totalIncome ?? 0) > 0 || (summary?.totalExpense ?? 0) > 0;
    
    return Column(
      children: [
        Row(
          children: [
            Expanded(
              child: SummaryCard(
                icon: Icons.trending_up,
                label: 'Thu nhập',
                amount: _formatCurrency(summary?.totalIncome ?? 0),
                backgroundColor: AppColors.cardIncome,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: SummaryCard(
                icon: Icons.trending_down,
                label: 'Chi tiêu',
                amount: _formatCurrency(summary?.totalExpense ?? 0),
                backgroundColor: AppColors.cardExpense,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: SummaryCard(
                icon: Icons.account_balance_wallet,
                label: 'Còn lại',
                amount: _formatCurrency(summary?.balance ?? 0),
                backgroundColor: AppColors.cardBalance,
              ),
            ),
          ],
        ),
        if (!hasData)
          Padding(
            padding: const EdgeInsets.only(top: 12),
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.blue.shade200),
              ),
              child: Row(
                children: [
                  Icon(Icons.info_outline, size: 16, color: Colors.blue.shade700),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Thêm giao dịch để theo dõi tài chính của bạn',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.blue.shade700,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
      ],
    );
  }
}
