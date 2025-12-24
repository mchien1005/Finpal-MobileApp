import 'package:flutter/material.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/custom_app_bar.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import '../../../data/services/dashboard_service.dart';
import '../../../data/services/user_service.dart';
import '../../../data/models/dashboard_model.dart';
import '../../../data/models/user.dart' show UserProfile;
import '../../widgets/dashboard/summary_cards_section.dart';
import '../../widgets/dashboard/progress_card_section.dart';
import '../../widgets/dashboard/trend_chart_section.dart';
import '../../widgets/dashboard/category_chart_section.dart';
import '../../widgets/dashboard/warning_card_section.dart';
import '../../widgets/dashboard/budget_analysis_section.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({Key? key}) : super(key: key);

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  final DashboardService _dashboardService = DashboardService();
  final UserService _userService = UserService();
  
  DashboardSummary? _summary;
  List<CategorySpending> _categories = [];
  List<BudgetItem> _budgets = [];
  List<CategoryBudget> _categoryBudgets = [];
  List<MonthlyTrend> _monthlyTrends = [];
  int _selectedMonths = 6;
  BudgetItem? _activeBudget;
  CashFlow? _cashFlow;
  UserProfile? _user;
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadDashboardData();
  }

  Future<void> _loadDashboardData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      print('📡 Dashboard: Bắt đầu tải dữ liệu...');
      
      final results = await Future.wait([
        _dashboardService.getSummary(),
        _dashboardService.getSpendingByCategory(),
        _dashboardService.getBudgets(),
        _dashboardService.getActiveBudget(),
        _dashboardService.getCashFlow(),
        _userService.getProfile(),
        _dashboardService.getCategoryBudgets(),
        _dashboardService.getMonthlyTrend(months: _selectedMonths),
      ]);

      final summary = results[0] as DashboardSummary;
      final categories = results[1] as List<CategorySpending>;
      final budgets = results[2] as List<BudgetItem>;
      final activeBudget = results[3] as BudgetItem?;
      final cashFlow = results[4] as CashFlow;
      final user = results[5] as UserProfile;
      final categoryBudgets = results[6] as List<CategoryBudget>;
      final monthlyTrends = results[7] as List<MonthlyTrend>;

      print('✅ Dashboard Summary:');
      print('   - Thu nhập: ${summary.totalIncome}');
      print('   - Chi tiêu: ${summary.totalExpense}');
      print('   - Số dư: ${summary.balance}');
      print('   - Budget used: ${summary.budgetUsed}/${summary.budgetLimit}');
      print('✅ Dashboard Categories: ${categories.length} categories');
      if (categories.isNotEmpty) {
        for (var cat in categories) {
          print('   - ${cat.categoryName}: ${cat.amount}đ (${cat.percentage}%)');
        }
      } else {
        print('   ⚠️  Không có categories nào!');
      }
      print('✅ User: ${user.fullName} (${user.email})');
      print('✅ Active Budget: ${activeBudget != null ? "${activeBudget.name} - ${activeBudget.spent}/${activeBudget.amount}" : "Không có"}');
      print('✅ Cash Flow - Budget: ${cashFlow.budgetUsed}/${cashFlow.budgetLimit} (${cashFlow.budgetPercentage.toStringAsFixed(1)}%)');
      print('✅ Category Budgets: ${categoryBudgets.length} budgets');
      if (categoryBudgets.isNotEmpty) {
        for (var budget in categoryBudgets) {
          print('   - ${budget.categoryName}: ${budget.spentAmount}/${budget.budgetAmount} (${budget.percentage.toStringAsFixed(1)}%) - ${budget.status}');
        }
      }
      print('✅ Monthly Trends: ${monthlyTrends.length} months');
      if (monthlyTrends.isNotEmpty) {
        for (var trend in monthlyTrends) {
          print('   - ${trend.month}: Income ${trend.income}, Expense ${trend.expense}');
        }
      }

      setState(() {
        _summary = summary;
        _categories = categories;
        _budgets = budgets;
        _categoryBudgets = categoryBudgets;
        _monthlyTrends = monthlyTrends;
        _activeBudget = activeBudget;
        _cashFlow = cashFlow;
        _user = user;
        _isLoading = false;
      });
    } catch (e, stackTrace) {
      print('❌ Lỗi tải dashboard: $e');
      print('📍 Stack: $stackTrace');
      
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop) return;
        await BottomNavHelper.handleBackButton(context);
      },
      child: AppBarWithDrawer.scrollable(
        context,
        userName: _user?.fullName ?? 'User',
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
          child: _isLoading
              ? const Center(
                  child: CircularProgressIndicator(
                    valueColor: AlwaysStoppedAnimation<Color>(AppColors.primary),
                  ),
                )
              : _error != null
                  ? _buildErrorWidget()
                  : Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          SummaryCardsSection(summary: _summary),
                          const SizedBox(height: 16),
                          ProgressCardSection(
                            summary: _summary,
                            activeBudget: _activeBudget,
                            cashFlow: _cashFlow,
                          ),
                          const SizedBox(height: 16),
                          TrendChartSection(
                            monthlyTrends: _monthlyTrends,
                            selectedMonths: _selectedMonths,
                            onMonthsChanged: (value) {
                              setState(() {
                                _selectedMonths = value;
                              });
                              _loadDashboardData();
                            },
                          ),
                          const SizedBox(height: 16),
                          CategoryChartSection(categories: _categories),
                          const SizedBox(height: 16),
                          WarningCardSection(categories: _categories),
                          const SizedBox(height: 16),
                          BudgetAnalysisSection(categoryBudgets: _categoryBudgets),
                          const SizedBox(height: 16),
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
      ),
    );
  }

  Widget _buildErrorWidget() {
    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline,
              size: 64,
              color: Colors.red,
            ),
            const SizedBox(height: 16),
            const Text(
              'Không thể tải dữ liệu',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              _error ?? 'Đã xảy ra lỗi',
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: _loadDashboardData,
              icon: const Icon(Icons.refresh),
              label: const Text('Thử lại'),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primary,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(
                  horizontal: 32,
                  vertical: 12,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
