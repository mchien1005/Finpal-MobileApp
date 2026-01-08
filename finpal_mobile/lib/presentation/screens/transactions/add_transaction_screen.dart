import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../data/services/category_cache_service.dart';
import '../../../data/services/notification_service.dart';
import '../../../data/models/category.dart';
import 'widgets/add_transaction_tab.dart';
import 'widgets/add_budget_tab.dart';

/// Màn hình thêm giao dịch với 2 tab: Thêm giao dịch và Thêm ngân sách
class AddTransactionScreen extends StatefulWidget {
  /// Tab index ban đầu (0: Giao dịch, 1: Ngân sách)
  final int initialTabIndex;

  const AddTransactionScreen({super.key, this.initialTabIndex = 0});

  @override
  State<AddTransactionScreen> createState() => _AddTransactionScreenState();
}

class _AddTransactionScreenState extends State<AddTransactionScreen>
    with SingleTickerProviderStateMixin {
  // Tab Controller
  late TabController _tabController;

  // Services
  final _categoryCacheService = CategoryCacheService.instance;
  final _notificationService = NotificationService();

  // Categories
  List<Category> _categories = [];
  bool _isCategoriesLoading = true;
  int _unreadCount = 0;

  // Transaction type cho tab giao dịch
  String _transactionType = 'expense';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(
      length: 2,
      vsync: this,
      initialIndex: widget.initialTabIndex,
    );
    // Preload tất cả danh mục (EXPENSE và INCOME) khi vào màn hình
    _preloadAndLoadCategories();
    _loadUnreadCount();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  /// Preload tất cả danh mục và load danh mục hiện tại
  Future<void> _preloadAndLoadCategories() async {
    setState(() {
      _isCategoriesLoading = true;
    });

    try {
      // Preload cả EXPENSE và INCOME song song để cache sẵn
      await _categoryCacheService.preloadAllCategories();

      // Load danh mục cho loại hiện tại
      await _loadCategories();
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

  /// Tải danh mục từ cache (đã được preload)
  Future<void> _loadCategories() async {
    final type = _transactionType == 'expense' ? 'EXPENSE' : 'INCOME';

    setState(() {
      _isCategoriesLoading = true;
    });

    try {
      // Lấy từ cache service (sẽ dùng cache nếu đã có)
      final categories = await _categoryCacheService.getCategories(type);

      setState(() {
        _categories = categories;
        _isCategoriesLoading = false;
      });
    } catch (e) {
      setState(() {
        _isCategoriesLoading = false;
      });
    }
  }

  Future<void> _loadUnreadCount() async {
    try {
      final count = await _notificationService.getUnreadCount();
      if (mounted) {
        setState(() {
          _unreadCount = count;
        });
      }
    } catch (e) {
      // Ignore errors for notification count
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
        notificationCount: _unreadCount,
        backgroundColor: AppColors.background,
        customTitle: 'Thêm giao dịch, ngân sách',
        body: Builder(
          builder: (context) {
            // Tính chiều cao một lần dựa trên screen size
            final screenHeight = MediaQuery.sizeOf(context).height;
            final contentHeight =
                screenHeight - 250; // Trừ AppBar, TabBar, BottomNav

            return Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Tab Bar
                _buildTabBar(),

                // Tab Content
                SizedBox(
                  height: contentHeight > 300 ? contentHeight : 300,
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
            );
          },
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
            color: Colors.black.withValues(alpha: 0.05),
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
