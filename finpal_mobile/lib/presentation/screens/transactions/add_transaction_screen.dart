import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../data/services/transaction_service.dart';
import '../../../data/models/category.dart';
import 'widgets/add_transaction_tab.dart';
import 'widgets/add_budget_tab.dart';

/// Màn hình thêm giao dịch với 2 tab: Thêm giao dịch và Thêm ngân sách
class AddTransactionScreen extends StatefulWidget {
  const AddTransactionScreen({super.key});

  @override
  State<AddTransactionScreen> createState() => _AddTransactionScreenState();
}

class _AddTransactionScreenState extends State<AddTransactionScreen>
    with SingleTickerProviderStateMixin {
  // Tab Controller
  late TabController _tabController;

  // Transaction Service
  final _transactionService = TransactionService();

  // Categories
  List<Category> _categories = [];
  bool _isCategoriesLoading = true;

  // Transaction type cho tab giao dịch
  String _transactionType = 'expense';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadCategories();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  /// Tải danh mục từ API
  Future<void> _loadCategories() async {
    setState(() {
      _isCategoriesLoading = true;
    });

    try {
      final type = _transactionType == 'expense' ? 'EXPENSE' : 'INCOME';
      final categories = await _transactionService.getCategories(type: type);
      setState(() {
        _categories = categories;
        _isCategoriesLoading = false;
      });
    } catch (e) {
      setState(() {
        _isCategoriesLoading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Không thể tải danh mục: ${e.toString()}'),
            backgroundColor: Colors.orange,
          ),
        );
      }
    }
  }

  /// Xử lý thay đổi loại giao dịch
  void _onTransactionTypeChanged(String type) {
    setState(() {
      _transactionType = type;
    });
    _loadCategories();
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
        userName: 'Nguyễn Văn A',
        notificationCount: 3,
        backgroundColor: AppColors.background,
        customTitle: 'Thêm giao dịch, ngân sách',
        body: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Tab Bar
            _buildTabBar(),

            // Tab Content - Sử dụng SizedBox với chiều cao cố định hoặc LayoutBuilder
            SizedBox(
              height:
                  MediaQuery.of(context).size.height -
                  250, // Trừ đi AppBar và BottomNav
              child: TabBarView(
                controller: _tabController,
                children: [
                  // Tab 1: Thêm giao dịch
                  AddTransactionTab(
                    categories: _categories,
                    isCategoriesLoading: _isCategoriesLoading,
                    transactionType: _transactionType,
                    onTransactionTypeChanged: _onTransactionTypeChanged,
                  ),
                  // Tab 2: Thêm ngân sách
                  const AddBudgetTab(),
                ],
              ),
            ),
          ],
        ),
        bottomNavigationBar: CustomBottomNavBar(
          currentIndex: 2,
          onTap: (index) {
            BottomNavHelper.navigateToIndex(context, index, 2);
          },
        ),
      ),
    );
  }

  /// Build Tab Bar với 2 tab
  Widget _buildTabBar() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: const Color(0xFFD7006E).withValues(alpha: 0.3),
          width: 1.5,
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: TabBar(
        controller: _tabController,
        indicator: BoxDecoration(
          borderRadius: BorderRadius.circular(10),
          color: const Color(0xFFD7006E),
        ),
        indicatorSize: TabBarIndicatorSize.tab,
        labelColor: Colors.white,
        unselectedLabelColor: const Color(0xFFD7006E),
        labelStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
        unselectedLabelStyle: const TextStyle(
          fontWeight: FontWeight.w500,
          fontSize: 14,
        ),
        dividerColor: Colors.transparent,
        padding: const EdgeInsets.all(4),
        tabs: const [
          Tab(text: 'Giao dịch'),
          Tab(text: 'Ngân sách'),
        ],
      ),
    );
  }
}
