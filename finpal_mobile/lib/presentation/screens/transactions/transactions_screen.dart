import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import '../../../core/widgets/success_notification_dialog.dart';
import '../../../core/widgets/confirmation_dialog.dart';
import '../../../core/utils/category_icon_helper.dart';
import 'dart:async';
import '../../../core/services/transaction_event_bus.dart';
import '../../../data/models/transaction.dart';
import '../../../data/services/transaction_service.dart';
import '../../../data/services/notification_service.dart';
import 'edit_transaction_dialog.dart';

class TransactionsScreen extends StatefulWidget {
  const TransactionsScreen({super.key});

  @override
  State<TransactionsScreen> createState() => _TransactionsScreenState();
}

class _TransactionsScreenState extends State<TransactionsScreen>
    with SingleTickerProviderStateMixin {
  final int _selectedNavIndex = 1;
  late TabController _tabController;
  final TransactionService _transactionService = TransactionService();
  final NotificationService _notificationService = NotificationService();
  late final StreamSubscription<void> _eventSub;

  // Transaction data
  List<Transaction> _transactions = [];
  bool _isLoading = false;
  String? _errorMessage;

  // Số lượng thông báo chưa đọc
  int _unreadCount = 0;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _tabController.addListener(() {
      if (!_tabController.indexIsChanging) {
        setState(() {});
      }
    });
    _loadTransactions();
    _loadUnreadNotificationCount();
    _eventSub = TransactionEventBus.instance.onUpdated.listen((transactionId) async {
      if (!mounted) return;

      // Reload transactions from server
      await _loadTransactions();

      if (!mounted) return;

      // If an updated transaction id was provided, ensure it appears at the top
      if (transactionId != null) {
        final idx = _transactions.indexWhere((t) => t.id == transactionId);
        if (idx > 0) {
          setState(() {
            final updatedTx = _transactions.removeAt(idx);
            _transactions.insert(0, updatedTx);
          });
        } else if (idx == -1) {
          // Item not found in the loaded page — fetch it individually and insert
          try {
            final resp = await _transactionService.getTransactionById(transactionId);
            final tx = Transaction.fromJson(resp);
            if (mounted) {
              setState(() {
                _transactions.insert(0, tx);
              });
            }
          } catch (_) {
            // Ignore fetch errors — best effort to show updated item
          }
        }
      }
    });
  }

  /// Load số lượng thông báo chưa đọc
  Future<void> _loadUnreadNotificationCount() async {
    try {
      final count = await _notificationService.getUnreadCount();
      if (mounted) {
        setState(() {
          _unreadCount = count;
        });
      }
    } catch (e) {
      // Bỏ qua lỗi khi load notification count
      debugPrint('Lỗi khi load notification count: $e');
    }
  }

  /// Load transactions from API
  Future<void> _loadTransactions() async {
    if (!mounted) return;
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      // Load categories first to map categoryId -> category name
      final categories = await _transactionService.getCategories();

      final result = await _transactionService.getTransactions(
        page: 0,
        size: 100, // Load more transactions
      );

      final transactionsList = result['content'] as List;
      final transactions = transactionsList
          .map((t) => Transaction.fromJson(t))
          .toList();

      // Map categoryId to category for each transaction if missing
      for (int i = 0; i < transactions.length; i++) {
        var transaction = transactions[i];
        if (transaction.category == null && transaction.categoryId != null) {
          try {
            final category = categories.firstWhere(
              (c) => c.id == transaction.categoryId,
              orElse: () => categories.first,
            );

            transactions[i] = Transaction(
              id: transaction.id,
              type: transaction.type,
              amount: transaction.amount,
              transactionSource: transaction.transactionSource,
              categoryId: transaction.categoryId,
              description: transaction.description,
              merchant: transaction.merchant,
              transactionDate: transaction.transactionDate,
              isAuto: transaction.isAuto,
              category: TransactionCategory(
                id: category.id,
                name: category.name,
                icon: category.icon,
              ),
            );
          } catch (_) {}
        }
      }

      if (mounted) {
        setState(() {
          _transactions = transactions;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = e.toString();
          _isLoading = false;
        });
      }
    }
  }

  @override
  void dispose() {
    _eventSub.cancel();
    _tabController.dispose();
    super.dispose();
  }

  /// Update transaction via API
  Future<void> _updateTransaction({
    required int id,
    required String type,
    required double amount,
    required String source,
    required int? categoryId,
    required String description,
    required DateTime date,
  }) async {
    await _transactionService.updateTransaction(
      id: id,
      type: type,
      amount: amount,
      transactionSource: source,
      categoryId: categoryId,
      description: description,
      transactionDate: date,
    );
  }

  /// Delete transaction via API
  Future<void> _deleteTransaction(int id) async {
    try {
      await _transactionService.deleteTransaction(id);
      await _loadTransactions();
      TransactionEventBus.instance.notifyUpdated(id);
    } catch (e) {
      rethrow;
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop) return;
        Navigator.of(context).pop();
      },
      child: AppBarWithDrawer.scrollable(
        context,
        notificationCount: _unreadCount,
        showSearchAction: true,
        customTitle: 'Giao dịch của bạn',
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
                Container(
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
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Padding(
                        padding: EdgeInsets.fromLTRB(24, 24, 24, 16),
                        child: Text(
                          'Lịch sử giao dịch',
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w400,
                            color: AppColors.textPrimary,
                          ),
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 24),
                        child: Container(
                          height: 36,
                          padding: const EdgeInsets.all(3),
                          decoration: BoxDecoration(
                            color: const Color(0xFFECECF0),
                            borderRadius: BorderRadius.circular(14),
                          ),
                          child: TabBar(
                            controller: _tabController,
                            indicator: BoxDecoration(
                              color: AppColors.white,
                              borderRadius: BorderRadius.circular(9),
                              border: Border.all(
                                color: AppColors.borderColor,
                                width: 1.12,
                              ),
                            ),
                            indicatorSize: TabBarIndicatorSize.tab,
                            indicatorPadding: const EdgeInsets.symmetric(
                              horizontal: 2,
                            ),
                            dividerColor: Colors.transparent,
                            labelColor: AppColors.textPrimary,
                            unselectedLabelColor: AppColors.textPrimary,
                            labelStyle: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w400,
                            ),
                            tabs: const [
                              Tab(text: 'Tất cả'),
                              Tab(text: 'Chi tiêu'),
                              Tab(text: 'Thu nhập'),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 24),
                      _buildCurrentTabContent(),
                      const SizedBox(height: 16),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        bottomNavigationBar: CustomBottomNavBar(
          currentIndex: _selectedNavIndex,
          onTap: (index) {
            BottomNavHelper.navigateToIndex(context, index, _selectedNavIndex);
          },
        ),
      ),
    );
  }

  Widget _buildCurrentTabContent() {
    if (_isLoading) {
      return const Padding(
        padding: EdgeInsets.symmetric(horizontal: 24, vertical: 40),
        child: Center(child: CircularProgressIndicator()),
      );
    }

    if (_errorMessage != null) {
      return Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 40),
        child: Center(
          child: Column(
            children: [
              const Icon(Icons.error_outline, size: 48, color: Colors.red),
              const SizedBox(height: 16),
              const Text(
                'Không thể tải dữ liệu',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
              ),
              const SizedBox(height: 8),
              Text(
                _errorMessage!,
                style: const TextStyle(fontSize: 14, color: Colors.grey),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: _loadTransactions,
                child: const Text('Thử lại'),
              ),
            ],
          ),
        ),
      );
    }

    if (_transactions.isEmpty) {
      return const Padding(
        padding: EdgeInsets.symmetric(horizontal: 24, vertical: 40),
        child: Center(
          child: Text(
            'Chưa có giao dịch nào',
            style: TextStyle(fontSize: 16, color: Colors.grey),
          ),
        ),
      );
    }

    List<Widget> transactionWidgets = [];

    if (_tabController.index == 0) {
      transactionWidgets = _getAllTransactions();
    } else if (_tabController.index == 1) {
      transactionWidgets = _getExpenseTransactions();
    } else {
      transactionWidgets = _getIncomeTransactions();
    }

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(children: transactionWidgets),
    );
  }

  List<Widget> _getAllTransactions() {
    List<Widget> widgets = [];
    for (int i = 0; i < _transactions.length; i++) {
      if (i > 0) widgets.add(const SizedBox(height: 12));
      widgets.add(_buildTransactionItem(_transactions[i]));
    }
    return widgets;
  }

  List<Widget> _getExpenseTransactions() {
    List<Widget> widgets = [];
    final expenses = _transactions.where((t) => t.isExpense).toList();
    for (int i = 0; i < expenses.length; i++) {
      if (i > 0) widgets.add(const SizedBox(height: 12));
      widgets.add(_buildTransactionItem(expenses[i]));
    }
    return widgets;
  }

  List<Widget> _getIncomeTransactions() {
    List<Widget> widgets = [];
    final incomes = _transactions.where((t) => t.isIncome).toList();
    for (int i = 0; i < incomes.length; i++) {
      if (i > 0) widgets.add(const SizedBox(height: 12));
      widgets.add(_buildTransactionItem(incomes[i]));
    }
    return widgets;
  }

  

  Widget _buildTransactionItem(Transaction transaction) {
    final Widget iconWidget = CategoryIconHelper.emojiWidgetWithFallback(
      transaction.category?.icon,
      transaction.category?.name,
      size: 20,
    );
    final iconBg = transaction.isIncome
        ? const Color(0xFFDCFCE7)
        : const Color(0xFFFFE2E2);
    final title =
        transaction.merchant ?? transaction.description ?? 'Giao dịch';
    final category = transaction.category?.name ?? 'Không phân loại';
    final account = transaction.transactionSource;
    final amountFormatted = transaction.isIncome
        ? '+${NumberFormat('#,###', 'vi_VN').format(transaction.amount)}đ'
        : '-${NumberFormat('#,###', 'vi_VN').format(transaction.amount)}đ';
    final dateFormatted = DateFormat(
      'dd/MM/yyyy HH:mm',
    ).format(transaction.transactionDate);
    final isIncome = transaction.isIncome;

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFFF9FAFB),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  color: iconBg,
                  shape: BoxShape.circle,
                ),
                child: Center(child: iconWidget),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Flexible(
                          child: Text(
                            title,
                            style: const TextStyle(
                              fontSize: 14,
                              color: AppColors.textPrimary,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        if (transaction.isAuto) ...[
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 9,
                              vertical: 3,
                            ),
                            decoration: BoxDecoration(
                              color: const Color(0xFFECEEF2),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: const Text(
                              'Auto',
                              style: TextStyle(
                                fontSize: 12,
                                color: Color(0xFF030213),
                              ),
                            ),
                          ),
                        ],
                      ],
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Text(
                          category,
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.textSecondary,
                          ),
                        ),
                        const SizedBox(width: 8),
                        const Text(
                          '•',
                          style: TextStyle(
                            fontSize: 12,
                            color: Color(0xFFD1D5DC),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Text(
                          account,
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    amountFormatted,
                    style: TextStyle(
                      fontSize: 14,
                      color: isIncome
                          ? const Color(0xFF00A63E)
                          : const Color(0xFFE7000B),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    dateFormatted,
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () {
                    final parentContext = context;
                    showDialog(
                      context: parentContext,
                      builder: (dialogContext) => EditTransactionDialog(
                        title: title,
                        amount: transaction.amount,
                        category: category,
                        categoryId: transaction.categoryId,
                        account: account,
                        date: transaction.transactionDate,
                        isIncome: isIncome,
                        onSave:
                            ({
                              required double amount,
                              required String source,
                              required String category,
                              required int? categoryId,
                              required String description,
                              required DateTime date,
                            }) async {
                              // Close the edit dialog using the dialog's context
                              Navigator.of(dialogContext).pop();

                              // Show a loading indicator using the parent screen context
                              showDialog(
                                context: parentContext,
                                barrierDismissible: false,
                                builder: (_) => const Center(
                                  child: CircularProgressIndicator(),
                                ),
                              );

                              try {
                                await _updateTransaction(
                                  id: transaction.id,
                                  type: transaction.type,
                                  amount: amount,
                                  source: source,
                                  categoryId: categoryId,
                                  description: description,
                                  date: date,
                                );

                                await _loadTransactions();

                                if (!mounted) return;

                                // Move the updated transaction to the top of the list
                                setState(() {
                                  final idx = _transactions.indexWhere(
                                    (t) => t.id == transaction.id,
                                  );
                                  if (idx > 0) {
                                    final updatedTx = _transactions.removeAt(
                                      idx,
                                    );
                                    _transactions.insert(0, updatedTx);
                                  }
                                });

                                // Close the loading dialog (use parentContext)
                                Navigator.of(parentContext).pop();

                                SuccessNotificationDialog.show(
                                  parentContext,
                                  message: 'Đã cập nhật giao dịch thành công',
                                );
                                TransactionEventBus.instance.notifyUpdated(transaction.id);
                              } catch (e) {
                                if (!mounted) return;

                                // Close the loading dialog (use parentContext)
                                Navigator.of(parentContext).pop();

                                ScaffoldMessenger.of(
                                  parentContext,
                                ).showSnackBar(
                                  SnackBar(
                                    content: Text('Lỗi: ${e.toString()}'),
                                    backgroundColor: Colors.red,
                                  ),
                                );
                              }
                            },
                        onCancel: () => Navigator.of(dialogContext).pop(),
                      ),
                    );
                  },
                  icon: const Icon(Icons.edit, size: 16),
                  label: const Text('Sửa'),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.textPrimary,
                    side: const BorderSide(
                      color: Color(0xFFFFD230),
                      width: 1.12,
                    ),
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () {
                    ConfirmationDialog.show(
                      context,
                      title: 'Xóa giao dịch',
                      message:
                          'Bạn có chắc chắn muốn xóa không? \nHành động này không thể hoàn tác.',
                      confirmText: 'Xóa',
                      cancelText: 'Hủy',
                      confirmColor: const Color(0xFFD7006E),
                      onConfirm: () async {
                        try {
                          await _deleteTransaction(transaction.id);
                          if (mounted) {
                            SuccessNotificationDialog.show(
                              context,
                              message: 'Đã xóa giao dịch thành công',
                            );
                          }
                        } catch (e) {
                          if (mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text('Lỗi: ${e.toString()}'),
                                backgroundColor: Colors.red,
                              ),
                            );
                          }
                        }
                      },
                    );
                  },
                  icon: const Icon(Icons.delete, size: 16),
                  label: const Text('Xóa'),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.textPrimary,
                    backgroundColor: const Color(0xFFFFFAFA),
                    side: const BorderSide(
                      color: Color(0xFFE7000B),
                      width: 1.12,
                    ),
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
