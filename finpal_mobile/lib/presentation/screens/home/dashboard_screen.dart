import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../../core/theme/app_theme.dart';
import '../../widgets/dashboard_card.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
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
        child: SafeArea(
          child: Column(
            children: [
              _buildHeader(),
              Expanded(
                child: SingleChildScrollView(
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
            ],
          ),
        ),
      ),
      bottomNavigationBar: _buildBottomNavigationBar(),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.primary,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'FinPal',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: AppColors.white,
                ),
              ),
              SizedBox(height: 2),
              Text(
                'Xin chào, Nguyễn Văn A',
                style: TextStyle(
                  fontSize: 14,
                  color: AppColors.white,
                ),
              ),
            ],
          ),
          Row(
            children: [
              Stack(
                children: [
                  IconButton(
                    icon: const Icon(
                      Icons.notifications_outlined,
                      color: AppColors.white,
                    ),
                    onPressed: () {},
                  ),
                  Positioned(
                    right: 8,
                    top: 8,
                    child: Container(
                      padding: const EdgeInsets.all(2),
                      decoration: const BoxDecoration(
                        color: AppColors.white,
                        shape: BoxShape.circle,
                      ),
                      constraints: const BoxConstraints(
                        minWidth: 16,
                        minHeight: 16,
                      ),
                      child: const Center(
                        child: Text(
                          '3',
                          style: TextStyle(
                            fontSize: 10,
                            color: AppColors.primary,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              IconButton(
                icon: const Icon(
                  Icons.menu,
                  color: AppColors.white,
                ),
                onPressed: () {},
              ),
            ],
          ),
        ],
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
      border: Border.all(
        color: AppColors.borderColor,
        width: 1.12,
      ),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Phân loại chi tiêu',
          style: TextStyle(
            fontSize: 16,
            color: AppColors.textPrimary,
          ),
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
      subtitle: 'Ăn uống',
      description: '- 3.5M đ',
    );
  }

  Widget _buildBottomNavigationBar() {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.white,
        border: Border(
          top: BorderSide(
            color: AppColors.gray200,
            width: 1.15,
          ),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: SafeArea(
        child: SizedBox(
          height: 72,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildNavItem(0, Icons.dashboard, 'Tổng quan'),
              _buildNavItem(1, Icons.history, 'Lịch sử'),
              _buildAddButton(),
              _buildNavItem(3, Icons.lightbulb_outline, 'AI Gợi ý'),
              _buildNavItem(4, Icons.flag_outlined, 'Mục tiêu'),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(int index, IconData icon, String label) {
    final isSelected = _selectedIndex == index;
    return GestureDetector(
      onTap: () {
        setState(() {
          _selectedIndex = index;
        });
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.primaryLightBg : Colors.transparent,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 20,
              color: isSelected ? AppColors.primary : AppColors.textSecondary,
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                color: isSelected ? AppColors.primary : AppColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAddButton() {
    return Container(
      width: 62,
      height: 64,
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: AppColors.primary,
        borderRadius: BorderRadius.circular(10),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(
            Icons.add,
            size: 28,
            color: AppColors.white,
          ),
          const SizedBox(height: 2),
          const Text(
            'Thêm',
            style: TextStyle(
              fontSize: 12,
              color: AppColors.white,
            ),
          ),
        ],
      ),
    );
  }
}
