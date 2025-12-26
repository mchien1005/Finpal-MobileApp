import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../data/models/dashboard_model.dart';
import '../dashboard_card.dart';

class WarningCardSection extends StatelessWidget {
  final List<CategorySpending> categories;

  const WarningCardSection({
    Key? key,
    required this.categories,
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
    if (categories.isEmpty) {
      return const SizedBox.shrink();
    }
    
    if (!categories.any((c) => c.amount > 0)) {
      return const SizedBox.shrink();
    }
    
    // Tìm category chi nhiều nhất
    final validCategories = categories.where((c) => c.amount > 0).toList();
    
    if (validCategories.isEmpty) {
      return const SizedBox.shrink();
    }
    
    final topCategory = validCategories.reduce((a, b) => a.amount > b.amount ? a : b);
    
    final formattedAmount = _formatCurrency(topCategory.amount);
    final subtitle = '${topCategory.categoryName} - $formattedAmount đ';
    final description = 'Chiếm ${topCategory.percentage.toStringAsFixed(1)}% tổng chi tiêu';
    
    return WarningCard(
      title: 'Hạng mục chi nhiều nhất',
      subtitle: subtitle,
      description: description,
    );
  }
}
