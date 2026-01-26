import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/models/dashboard_model.dart';

class TrendChartSection extends StatelessWidget {
  final List<MonthlyTrend> monthlyTrends;
  final int selectedMonths;
  final ValueChanged<int> onMonthsChanged;

  const TrendChartSection({
    Key? key,
    required this.monthlyTrends,
    required this.selectedMonths,
    required this.onMonthsChanged,
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
    if (monthlyTrends.isEmpty) {
      return const SizedBox.shrink();
    }

    final currencyFormat = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '',
      decimalDigits: 0,
    );

    // Calculate max value for Y axis
    double maxValue = 0;
    for (var trend in monthlyTrends) {
      if (trend.income > maxValue) maxValue = trend.income;
      if (trend.expense > maxValue) maxValue = trend.expense;
    }

    // Add 10% padding to max value
    maxValue = maxValue * 1.1;

    // Ensure minimum range
    if (maxValue < 1000000) maxValue = 1000000;

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
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Xu hướng Thu Chi',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: AppColors.textPrimary,
                ),
              ),
              // Month filter
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.backgroundGradientMid,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: DropdownButton<int>(
                  value: selectedMonths,
                  underline: const SizedBox(),
                  icon: const Icon(Icons.arrow_drop_down, size: 18),
                  style: const TextStyle(
                    fontSize: 12,
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w600,
                  ),
                  items: [3, 6, 12].map((months) {
                    return DropdownMenuItem(
                      value: months,
                      child: Text('$months tháng'),
                    );
                  }).toList(),
                  onChanged: (value) {
                    if (value != null) {
                      onMonthsChanged(value);
                    }
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
          SizedBox(
            height: 200,
            child: LineChart(
              LineChartData(
                minY: 0,
                maxY: maxValue,
                gridData: FlGridData(
                  show: true,
                  drawVerticalLine: false,
                  getDrawingHorizontalLine: (value) {
                    return FlLine(color: Colors.grey.shade200, strokeWidth: 1);
                  },
                ),
                titlesData: FlTitlesData(
                  leftTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      reservedSize: 50,
                      interval: maxValue / 4,
                      getTitlesWidget: (value, meta) {
                        if (value < 0 || value > maxValue)
                          return const SizedBox();
                        return Text(
                          _formatCurrency(value),
                          style: const TextStyle(
                            fontSize: 10,
                            color: AppColors.textSecondary,
                          ),
                        );
                      },
                    ),
                  ),
                  rightTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  topTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  bottomTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      reservedSize: 30,
                      getTitlesWidget: (value, meta) {
                        final index = value.toInt();
                        if (index < 0 || index >= monthlyTrends.length) {
                          return const SizedBox();
                        }
                        final month = monthlyTrends[index].month;
                        return Padding(
                          padding: const EdgeInsets.only(top: 8),
                          child: Text(
                            month,
                            style: const TextStyle(
                              fontSize: 10,
                              color: AppColors.textSecondary,
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                ),
                borderData: FlBorderData(show: false),
                lineBarsData: [
                  // Income line
                  LineChartBarData(
                    spots: monthlyTrends.asMap().entries.map((e) {
                      return FlSpot(e.key.toDouble(), e.value.income);
                    }).toList(),
                    isCurved: true,
                    preventCurveOverShooting: true,
                    color: AppColors.cardIncome,
                    barWidth: 3,
                    dotData: FlDotData(
                      show: true,
                      getDotPainter: (spot, percent, barData, index) {
                        return FlDotCirclePainter(
                          radius: 4,
                          color: AppColors.cardIncome,
                          strokeWidth: 2,
                          strokeColor: Colors.white,
                        );
                      },
                    ),
                    belowBarData: BarAreaData(
                      show: true,
                      color: AppColors.cardIncome.withOpacity(0.1),
                    ),
                  ),
                  // Expense line
                  LineChartBarData(
                    spots: monthlyTrends.asMap().entries.map((e) {
                      return FlSpot(e.key.toDouble(), e.value.expense);
                    }).toList(),
                    isCurved: true,
                    preventCurveOverShooting: true,
                    color: AppColors.cardExpense,
                    barWidth: 3,
                    dotData: FlDotData(
                      show: true,
                      getDotPainter: (spot, percent, barData, index) {
                        return FlDotCirclePainter(
                          radius: 4,
                          color: AppColors.cardExpense,
                          strokeWidth: 2,
                          strokeColor: Colors.white,
                        );
                      },
                    ),
                    belowBarData: BarAreaData(
                      show: true,
                      color: AppColors.cardExpense.withOpacity(0.1),
                    ),
                  ),
                ],
                lineTouchData: LineTouchData(
                  enabled: true,
                  touchTooltipData: LineTouchTooltipData(
                    getTooltipItems: (touchedSpots) {
                      return touchedSpots.map((spot) {
                        final isIncome = spot.barIndex == 0;
                        return LineTooltipItem(
                          '${isIncome ? "Thu" : "Chi"}: ${currencyFormat.format(spot.y)} đ',
                          const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                        );
                      }).toList();
                    },
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
          // Legend
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _buildLegendItem('Thu nhập', AppColors.cardIncome),
              const SizedBox(width: 24),
              _buildLegendItem('Chi tiêu', AppColors.cardExpense),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildLegendItem(String label, Color color) {
    return Row(
      children: [
        Container(
          width: 16,
          height: 3,
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(2),
          ),
        ),
        const SizedBox(width: 6),
        Text(
          label,
          style: const TextStyle(fontSize: 12, color: AppColors.textSecondary),
        ),
      ],
    );
  }
}
