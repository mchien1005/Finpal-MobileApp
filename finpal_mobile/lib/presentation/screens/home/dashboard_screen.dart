import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import '../../widgets/dashboard_card.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  Widget build(BuildContext context) {
    return AppBarWithDrawer.scrollable(
      context,
      userName: 'Nguyễn Văn A',
      notificationCount: 3,
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              AppColors.backgroundGradientStart,
              AppColors.backgroundGradientMid,
              AppColors.backgroundGradientEnd,
            ],
            stops: [0.0, 0.045, 1.0],
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildSummaryCards(),
              const SizedBox(height: 16),
              _buildProgressCard(),
              const SizedBox(height: 16),
              _buildCategoryCard(),
              const SizedBox(height: 16),
              _buildWarningCard(),
              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
      bottomNavigationBar: CustomBottomNavBar(
        currentIndex: 0,
        onTap: (index) {
          BottomNavHelper.navigateToIndex(context, index, 0);
        },
      ),
    );
  }

  Widget _buildSummaryCards() {
    return Row(
      children: [
        Expanded(
          child: SummaryCard(
            icon: Icons.trending_up,
            label: 'Thu nhập',
            amount: '15.0M',
            backgroundColor: AppColors.cardIncome,
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: SummaryCard(
            icon: Icons.trending_down,
            label: 'Chi tiêu',
            amount: '8.8M',
            backgroundColor: AppColors.cardExpense,
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: SummaryCard(
            icon: Icons.account_balance_wallet,
            label: 'Còn lại',
            amount: '6.3M',
            backgroundColor: AppColors.cardBalance,
          ),
        ),
      ],
    );
  }

  Widget _buildProgressCard() {
    return const ProgressCard(
      title: 'Tình hình chi tiêu tháng này',
      progressValue: 0.583,
      progressText: '58.3%',
      amountText: '8.750.000 / 15.000.000 VNĐ',
    );
  }

  Widget _buildCategoryCard() {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.borderColor, width: 1.12),
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
                sections: [
                  PieChartSectionData(
                    value: 40,
                    title: 'Ăn uống\n40%',
                    color: AppColors.categoryFood,
                    radius: 110,
                    titleStyle: const TextStyle(
                      fontSize: 11,
                      color: AppColors.categoryFood,
                      fontWeight: FontWeight.w600,
                    ),
                    titlePositionPercentageOffset: 1.4,
                  ),
                  PieChartSectionData(
                    value: 17,
                    title: 'Di chuyển\n17%',
                    color: AppColors.categoryTransport,
                    radius: 110,
                    titleStyle: const TextStyle(
                      fontSize: 11,
                      color: AppColors.categoryTransport,
                      fontWeight: FontWeight.w600,
                    ),
                    titlePositionPercentageOffset: 1.4,
                  ),
                  PieChartSectionData(
                    value: 23,
                    title: 'Mua sắm\n23%',
                    color: AppColors.categoryShopping,
                    radius: 110,
                    titleStyle: const TextStyle(
                      fontSize: 11,
                      color: AppColors.categoryShopping,
                      fontWeight: FontWeight.w600,
                    ),
                    titlePositionPercentageOffset: 1.4,
                  ),
                  PieChartSectionData(
                    value: 9,
                    title: 'Giải trí\n9%',
                    color: AppColors.categoryEntertainment,
                    radius: 110,
                    titleStyle: const TextStyle(
                      fontSize: 11,
                      color: AppColors.categoryEntertainment,
                      fontWeight: FontWeight.w600,
                    ),
                    titlePositionPercentageOffset: 1.4,
                  ),
                  PieChartSectionData(
                    value: 11,
                    title: 'Hóa đơn\n11%',
                    color: AppColors.categoryBills,
                    radius: 110,
                    titleStyle: const TextStyle(
                      fontSize: 11,
                      color: AppColors.categoryBills,
                      fontWeight: FontWeight.w600,
                    ),
                    titlePositionPercentageOffset: 1.4,
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          const CategoryLegendItem(
            color: AppColors.categoryFood,
            label: 'Ăn uống',
            amount: '3.500.000 đ',
          ),
          const CategoryLegendItem(
            color: AppColors.categoryTransport,
            label: 'Di chuyển',
            amount: '1.500.000 đ',
          ),
          const CategoryLegendItem(
            color: AppColors.categoryShopping,
            label: 'Mua sắm',
            amount: '2.000.000 đ',
          ),
          const CategoryLegendItem(
            color: AppColors.categoryEntertainment,
            label: 'Giải trí',
            amount: '800.000 đ',
          ),
          const CategoryLegendItem(
            color: AppColors.categoryBills,
            label: 'Hóa đơn',
            amount: '950.000 đ',
          ),
        ],
      ),
    );
  }

  Widget _buildWarningCard() {
    return const WarningCard(
      title: 'Hạng mục chi nhiều nhất',
      subtitle: 'Ăn uống - 3.5M đ',
      description: 'Chiếm 40% tổng chi tiêu',
    );
  }
}
