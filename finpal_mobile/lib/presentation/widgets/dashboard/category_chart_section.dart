import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/models/dashboard_model.dart';

class CategoryChartSection extends StatelessWidget {
  final List<CategorySpending> categories;

  const CategoryChartSection({
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

  Color _getCategoryColor(String category) {
    switch (category.toUpperCase()) {
      case 'FOOD':
      case 'ĂN UỐNG':
        return AppColors.categoryFood;
      case 'TRANSPORT':
      case 'DI CHUYỂN':
        return AppColors.categoryTransport;
      case 'SHOPPING':
      case 'MUA SẮM':
        return AppColors.categoryShopping;
      case 'ENTERTAINMENT':
      case 'GIẢI TRÍ':
        return AppColors.categoryEntertainment;
      case 'BILLS':
      case 'HÓA ĐƠN':
        return AppColors.categoryBills;
      default:
        return AppColors.primary;
    }
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '',
      decimalDigits: 0,
    );

    // Kiểm tra nếu không có data thực sự
    final hasRealData = categories.isNotEmpty && 
                        categories.any((c) => c.amount > 0);
    
    if (!hasRealData) {
      return Container(
        padding: const EdgeInsets.all(32),
        decoration: BoxDecoration(
          color: AppColors.white,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: const Color(0x1A000000), width: 1.12),
        ),
        child: Column(
          children: [
            Icon(
              Icons.pie_chart_outline_rounded,
              size: 80,
              color: Colors.grey[300],
            ),
            const SizedBox(height: 16),
            const Text(
              'Chưa có dữ liệu chi tiêu',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Hãy thêm giao dịch đầu tiên của bạn!',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 14,
                color: AppColors.textSecondary,
              ),
            ),
          ],
        ),
      );
    }

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0x1A000000), width: 1.12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Phân loại chi tiêu',
            style: TextStyle(fontSize: 16, color: AppColors.textPrimary),
          ),
          const SizedBox(height: 24),
          SizedBox(
            height: 300,
            child: PieChart(
              PieChartData(
                sectionsSpace: 2,
                centerSpaceRadius: 0,
                startDegreeOffset: -90,
                borderData: FlBorderData(show: false),
                sections: categories
                    .where((c) => c.amount > 0)
                    .map((category) {
                  final color = _getCategoryColor(category.category);
                  final isLargeCategory = category.percentage >= 10;
                  
                  return PieChartSectionData(
                    value: category.percentage,
                    title: '',
                    color: color,
                    radius: 110,
                    titlePositionPercentageOffset: 0.7,
                    badgeWidget: _buildBadge(
                      '${category.categoryName} ${category.percentage.toStringAsFixed(0)}%',
                      color,
                      isLargeCategory,
                    ),
                    badgePositionPercentageOffset: isLargeCategory ? 1.3 : 1.7,
                  );
                }).toList(),
              ),
            ),
          ),
          const SizedBox(height: 16),
          ...categories
              .where((c) => c.amount > 0)
              .map((category) {
            return Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Row(
                children: [
                  Container(
                    width: 16,
                    height: 16,
                    decoration: BoxDecoration(
                      color: _getCategoryColor(category.category),
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      category.categoryName,
                      style: const TextStyle(
                        fontSize: 14,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ),
                  Text(
                    '${currencyFormat.format(category.amount)} đ',
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildBadge(String text, Color color, bool isLarge) {
    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: isLarge ? 8 : 6,
        vertical: isLarge ? 4 : 3,
      ),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(4),
        border: Border.all(color: color, width: 1.5),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Text(
        text,
        style: TextStyle(
          fontSize: isLarge ? 11 : 9,
          color: color,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
