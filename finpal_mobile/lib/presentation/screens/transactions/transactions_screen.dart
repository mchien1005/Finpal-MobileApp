import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import '../../../core/widgets/success_notification_dialog.dart';
import '../../../core/widgets/confirmation_dialog.dart';
import 'edit_transaction_dialog.dart';

class TransactionsScreen extends StatefulWidget {
  const TransactionsScreen({super.key});

  @override
  State<TransactionsScreen> createState() => _TransactionsScreenState();
}

class _TransactionsScreenState extends State<TransactionsScreen>
    with SingleTickerProviderStateMixin {
  int _selectedNavIndex = 1;
  late TabController _tabController;

  // Transaction data
  List<Map<String, dynamic>> _transactions = [];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _tabController.addListener(() {
      if (!_tabController.indexIsChanging) {
        setState(() {});
      }
    });

    // Initialize transactions data
    _transactions = [
      {
        'id': '1',
        'icon': Icons.directions_car,
        'iconBg': const Color(0xFFFFE2E2),
        'title': 'GRAB',
        'category': 'Di chuyển',
        'account': 'VCB',
        'amount': '-55.000đ',
        'date': '11-13 09:00',
        'isIncome': false,
        'hasAiBadge': false,
      },
      {
        'id': '2',
        'icon': Icons.coffee,
        'iconBg': const Color(0xFFFFE2E2),
        'title': 'THE COFFEE HOUSE',
        'category': '\u0102n u\u1ed1ng',
        'account': 'Techcombank',
        'amount': '-125.000\u0111',
        'date': '11-13 10:30',
        'isIncome': false,
        'hasAiBadge': false,
      },
      {
        'id': '3',
        'icon': Icons.shopping_bag,
        'iconBg': const Color(0xFFFFE2E2),
        'title': 'SHOPEE',
        'category': 'Mua s\u1eafm',
        'account': 'ACB',
        'amount': '-450.000\u0111',
        'date': '11-12 20:15',
        'isIncome': false,
        'hasAiBadge': false,
      },
      {
        'id': '4',
        'icon': Icons.coffee,
        'iconBg': const Color(0xFFFFE2E2),
        'title': 'B\u00e1nh m\u00ec',
        'category': '\u0102n u\u1ed1ng',
        'account': 'ACB',
        'amount': '-15.000\u0111',
        'date': '11-12 07:00',
        'isIncome': false,
        'hasAiBadge': false,
      },
      {
        'id': '5',
        'icon': Icons.account_balance_wallet,
        'iconBg': const Color(0xFFDCFCE7),
        'title': 'L\u01b0\u01a1ng th\u00e1ng 11',
        'category': 'L\u01b0\u01a1ng',
        'account': 'VCB',
        'amount': '+15.000.000\u0111',
        'date': '11-01 08:00',
        'isIncome': true,
        'hasAiBadge': false,
      },
      {
        'id': '6',
        'icon': Icons.movie,
        'iconBg': const Color(0xFFFFE2E2),
        'title': 'CGV CINEMA',
        'category': 'Gi\u1ea3i tr\u00ed',
        'account': 'Techcombank',
        'amount': '-180.000\u0111',
        'date': '11-10 19:30',
        'isIncome': false,
        'hasAiBadge': true,
        'aiConfidence': '89% AI',
      },
      {
        'id': '7',
        'icon': Icons.bolt,
        'iconBg': const Color(0xFFFFE2E2),
        'title': 'Ti\u1ec1n \u0111i\u1ec7n',
        'category': 'H\u00f3a \u0111\u01a1n',
        'account': 'Techcombank',
        'amount': '-350.000\u0111',
        'date': '11-05 14:00',
        'isIncome': false,
        'hasAiBadge': false,
      },
    ];
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  void _updateTransaction({
    required String id,
    required String amount,
    required String source,
    required String category,
    required String description,
    required DateTime date,
  }) {
    setState(() {
      final index = _transactions.indexWhere((t) => t['id'] == id);
      if (index != -1) {
        _transactions[index]['title'] = description;
        _transactions[index]['category'] = category;
        _transactions[index]['account'] = source;
        _transactions[index]['amount'] =
            amount.contains('-') || amount.contains('+')
            ? amount
            : (_transactions[index]['isIncome']
                  ? '+$amount\u0111'
                  : '-$amount\u0111');
        _transactions[index]['date'] = DateFormat('MM-dd HH:mm').format(date);
      }
    });
  }

  void _deleteTransaction(String id) {
    setState(() {
      _transactions.removeWhere((t) => t['id'] == id);
    });
  }

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
              Container(
                decoration: BoxDecoration(
                  color: AppColors.white,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: AppColors.borderColor, width: 1.12),
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
    );
  }

  Widget _buildCurrentTabContent() {
    // Hiển thị nội dung dựa trên tab được chọn
    List<Widget> transactions = [];

    if (_tabController.index == 0) {
      // Tất cả
      transactions = _getAllTransactions();
    } else if (_tabController.index == 1) {
      // Chi tiêu
      transactions = _getExpenseTransactions();
    } else {
      // Thu nhập
      transactions = _getIncomeTransactions();
    }

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(children: transactions),
    );
  }

  List<Widget> _getAllTransactions() {
    List<Widget> widgets = [];
    for (int i = 0; i < _transactions.length; i++) {
      if (i > 0) widgets.add(const SizedBox(height: 12));
      final t = _transactions[i];
      widgets.add(
        _buildTransactionItem(
          id: t['id'],
          icon: t['icon'],
          iconBg: t['iconBg'],
          title: t['title'],
          category: t['category'],
          account: t['account'],
          amount: t['amount'],
          date: t['date'],
          isIncome: t['isIncome'],
          hasAiBadge: t['hasAiBadge'],
          aiConfidence: t['aiConfidence'],
        ),
      );
    }
    return widgets;
  }

  List<Widget> _getExpenseTransactions() {
    List<Widget> widgets = [];
    final expenses = _transactions.where((t) => !t['isIncome']).toList();
    for (int i = 0; i < expenses.length; i++) {
      if (i > 0) widgets.add(const SizedBox(height: 12));
      final t = expenses[i];
      widgets.add(
        _buildTransactionItem(
          id: t['id'],
          icon: t['icon'],
          iconBg: t['iconBg'],
          title: t['title'],
          category: t['category'],
          account: t['account'],
          amount: t['amount'],
          date: t['date'],
          isIncome: t['isIncome'],
          hasAiBadge: t['hasAiBadge'],
          aiConfidence: t['aiConfidence'],
        ),
      );
    }
    return widgets;
  }

  List<Widget> _getIncomeTransactions() {
    List<Widget> widgets = [];
    final incomes = _transactions.where((t) => t['isIncome']).toList();
    for (int i = 0; i < incomes.length; i++) {
      if (i > 0) widgets.add(const SizedBox(height: 12));
      final t = incomes[i];
      widgets.add(
        _buildTransactionItem(
          id: t['id'],
          icon: t['icon'],
          iconBg: t['iconBg'],
          title: t['title'],
          category: t['category'],
          account: t['account'],
          amount: t['amount'],
          date: t['date'],
          isIncome: t['isIncome'],
          hasAiBadge: t['hasAiBadge'],
          aiConfidence: t['aiConfidence'],
        ),
      );
    }
    return widgets;
  }

  Widget _buildTransactionItem({
    required String id,
    required IconData icon,
    required Color iconBg,
    required String title,
    required String category,
    required String account,
    required String amount,
    required String date,
    required bool isIncome,
    required bool hasAiBadge,
    String? aiConfidence,
  }) {
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
                child: Icon(
                  icon,
                  size: 20,
                  color: isIncome ? Colors.green : Colors.red,
                ),
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
                        if (hasAiBadge && aiConfidence != null) ...[
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 9,
                              vertical: 3,
                            ),
                            decoration: BoxDecoration(
                              border: Border.all(
                                color: const Color(0xFFFFD230),
                                width: 1.12,
                              ),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              aiConfidence,
                              style: const TextStyle(
                                fontSize: 12,
                                color: Color(0xFFBB4D00),
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
                    amount,
                    style: TextStyle(
                      fontSize: 14,
                      color: isIncome
                          ? const Color(0xFF00A63E)
                          : const Color(0xFFE7000B),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    date,
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
                    showDialog(
                      context: context,
                      builder: (context) => EditTransactionDialog(
                        title: title,
                        amount: amount,
                        category: category,
                        account: account,
                        date: date,
                        isIncome: isIncome,
                        onSave:
                            ({
                              required String amount,
                              required String source,
                              required String category,
                              required String description,
                              required DateTime date,
                            }) {
                              Navigator.of(context).pop();

                              // Update transaction in state
                              _updateTransaction(
                                id: id,
                                amount: amount,
                                source: source,
                                category: category,
                                description: description,
                                date: date,
                              );

                              // Show success popup
                              SuccessNotificationDialog.show(
                                context,
                                message: 'Cập nhật giao dịch thành công',
                              );
                            },
                        onCancel: () {
                          Navigator.of(context).pop();
                        },
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
                      message: 'Bạn có chắc chắn muốn xóa  không? \nHành động này không thể hoàn tác.',
                      confirmText: 'Xóa',
                      cancelText: 'Hủy',
                      confirmColor: const Color(0xFFD7006E),
                      onConfirm: () {
                        _deleteTransaction(id);
                        SuccessNotificationDialog.show(
                          context,
                          message: 'Đã xóa giao dịch thành công',
                        );
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
