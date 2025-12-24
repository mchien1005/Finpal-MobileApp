import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../../core/constants/app_colors.dart';
import '../../../../core/utils/category_icon_helper.dart';
import '../../../../core/widgets/confirmation_dialog.dart';
import '../../../../core/widgets/success_notification_dialog.dart';
import '../../../../data/models/category.dart';
import '../../../../data/models/budget_model.dart';
import '../../../../data/services/budget_service.dart';
import 'budget_category_grid.dart';
import 'budget_form.dart';

class BudgetListCard extends StatefulWidget {
  final VoidCallback? onBudgetChanged;

  const BudgetListCard({super.key, this.onBudgetChanged});

  @override
  State<BudgetListCard> createState() => _BudgetListCardState();
}

class _BudgetListCardState extends State<BudgetListCard> {
  List<BudgetResponse> _budgets = [];
  bool _isLoading = true;
  final _currencyFormat = NumberFormat.currency(locale: 'vi_VN', symbol: 'đ');

  // Trạng thái hiển thị form thêm ngân sách
  bool _showAddBudget = false;
  Category? _selectedCategory;

  // Trạng thái hiển thị form sửa ngân sách
  BudgetResponse? _editingBudget;

  @override
  void initState() {
    super.initState();
    _loadBudgets();
  }

  /// Tải danh sách ngân sách từ API
  Future<void> _loadBudgets() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final budgets = await BudgetService.getAllBudgets();
      // Lọc chỉ những ngân sách đang active (soft delete)
      final activeBudgets = budgets.where((b) => b.isActive).toList();
      setState(() {
        _budgets = activeBudgets;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Không thể tải ngân sách: ${e.toString()}'),
            backgroundColor: Colors.orange,
          ),
        );
      }
    }
  }

  /// Hiển thị form thêm ngân sách
  void _showAddBudgetForm() {
    setState(() {
      _showAddBudget = true;
      _selectedCategory = null;
    });
  }

  /// Đóng form thêm ngân sách
  void _hideAddBudgetForm() {
    setState(() {
      _showAddBudget = false;
      _selectedCategory = null;
    });
  }

  /// Khi chọn danh mục
  void _onCategorySelected(Category category) {
    setState(() {
      _selectedCategory = category;
    });
  }

  /// Khi tạo ngân sách thành công
  void _onBudgetCreated() {
    setState(() {
      _showAddBudget = false;
      _selectedCategory = null;
    });
    _loadBudgets();
    widget.onBudgetChanged?.call();
  }

  /// Xóa ngân sách
  Future<void> _deleteBudget(BudgetResponse budget) async {
    // Hiển thị dialog xác nhận sử dụng ConfirmationDialog chung
    final confirmed = await ConfirmationDialog.show(
      context,
      title: 'Xác nhận xóa',
      message: 'Bạn có chắc chắn muốn xóa ngân sách "${budget.name}"?',
      confirmText: 'Xóa',
      cancelText: 'Hủy',
      confirmColor: Colors.red,
      // icon: Icons.warning_amber_rounded,
      iconColor: Colors.red,
    );

    if (confirmed == true) {
      try {
        debugPrint('🗑️ Đang xóa ngân sách ID: ${budget.id}');
        await BudgetService.deleteBudget(budget.id);
        debugPrint('✅ Xóa ngân sách thành công');

        if (mounted) {
          // Hiển thị thông báo thành công sử dụng SuccessNotificationDialog chung
          await SuccessNotificationDialog.show(
            context,
            message: 'Đã xóa ngân sách "${budget.name}" thành công!',
          );
          _loadBudgets();
          widget.onBudgetChanged?.call();
        }
      } catch (e) {
        debugPrint('❌ Lỗi xóa ngân sách: $e');
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Lỗi khi xóa: ${e.toString()}'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }

  /// Sửa ngân sách - chuyển sang chế độ edit với BudgetForm
  void _editBudget(BudgetResponse budget) {
    setState(() {
      _editingBudget = budget;
      _showAddBudget = false;
      _selectedCategory = null;
    });
  }

  /// Hủy chế độ edit
  void _cancelEdit() {
    setState(() {
      _editingBudget = null;
    });
  }

  /// Callback khi sửa ngân sách thành công
  void _onEditSuccess() {
    setState(() {
      _editingBudget = null;
    });
    _loadBudgets();
    widget.onBudgetChanged?.call();
  }

  /// Lấy màu dựa trên phần trăm tiến độ
  Color _getProgressColor(double percentage) {
    if (percentage >= 100) return Colors.red;
    if (percentage >= 70) return Colors.orange;
    if (percentage >= 50) return Colors.amber;
    return const Color(0xFFD7006E);
  }

  /// Lấy tên chu kỳ hiển thị
  String _getPeriodDisplay(String period) {
    switch (period) {
      case 'WEEKLY':
        return 'Hàng tuần';
      case 'MONTHLY':
        return 'Hàng tháng';
      case 'QUATERLY':
        return 'Hàng quý';
      case 'YEARLY':
        return 'Hàng năm';
      default:
        return period;
    }
  }

  @override
  Widget build(BuildContext context) {
    // Nếu đang ở chế độ edit, hiển thị BudgetForm
    if (_editingBudget != null) {
      return BudgetForm(
        existingBudget: _editingBudget,
        onBack: _cancelEdit,
        onSuccess: _onEditSuccess,
      );
    }

    return Column(
      children: [
        // Hiển thị card danh sách ngân sách HOẶC card thêm ngân sách
        if (!_showAddBudget) ...[
          // Card danh sách ngân sách
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.08),
                  blurRadius: 16,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header
                Row(
                  children: [
                    // Phần icon và tiêu đề - có thể co giãn
                    Expanded(
                      child: Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              gradient: const LinearGradient(
                                colors: [Color(0xFFD7006E), Color(0xFFFF6B9D)],
                                begin: Alignment.topLeft,
                                end: Alignment.bottomRight,
                              ),
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: const Icon(
                              Icons.account_balance_wallet,
                              color: Colors.white,
                              size: 20,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: const [
                                Text(
                                  'Ngân sách của bạn',
                                  style: TextStyle(
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold,
                                    color: AppColors.textPrimary,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                                Text(
                                  'Quản lý chi tiêu thông minh',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: AppColors.textSecondary,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    // Các nút action
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        // Nút thêm ngân sách
                        InkWell(
                          onTap: _showAddBudgetForm,
                          borderRadius: BorderRadius.circular(10),
                          child: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: Colors.green,
                              borderRadius: BorderRadius.circular(10),
                              boxShadow: [
                                BoxShadow(
                                  color: const Color.fromARGB(
                                    255,
                                    44,
                                    251,
                                    75,
                                  ).withValues(alpha: 0.3),
                                  offset: const Offset(3, 3),
                                ),
                              ],
                            ),
                            child: const Icon(
                              Icons.add,
                              color: Colors.white,
                              size: 20,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        // Nút refresh
                        IconButton(
                          onPressed: _loadBudgets,
                          icon: _isLoading
                              ? const SizedBox(
                                  width: 20,
                                  height: 20,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: Color(0xFFD7006E),
                                  ),
                                )
                              : const Icon(
                                  Icons.refresh,
                                  color: AppColors.textSecondary,
                                ),
                          tooltip: 'Làm mới',
                        ),
                      ],
                    ),
                  ],
                ),
                const SizedBox(height: 20),

                // Danh sách ngân sách
                _isLoading
                    ? const Center(
                        child: Padding(
                          padding: EdgeInsets.all(40),
                          child: CircularProgressIndicator(
                            color: Color(0xFFD7006E),
                          ),
                        ),
                      )
                    : _budgets.isEmpty
                    ? Center(
                        child: Padding(
                          padding: const EdgeInsets.all(40),
                          child: Column(
                            children: [
                              Container(
                                padding: const EdgeInsets.all(16),
                                decoration: BoxDecoration(
                                  color: Colors.grey.withValues(alpha: 0.1),
                                  borderRadius: BorderRadius.circular(50),
                                ),
                                child: const Icon(
                                  Icons.savings_outlined,
                                  size: 48,
                                  color: AppColors.textSecondary,
                                ),
                              ),
                              const SizedBox(height: 16),
                              const Text(
                                'Chưa có ngân sách nào',
                                style: TextStyle(
                                  color: AppColors.textSecondary,
                                  fontSize: 14,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                              const SizedBox(height: 4),
                              const Text(
                                'Nhấn nút + để tạo ngân sách mới',
                                style: TextStyle(
                                  color: AppColors.textSecondary,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      )
                    : ListView.separated(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: _budgets.length,
                        separatorBuilder: (context, index) =>
                            const SizedBox(height: 12),
                        itemBuilder: (context, index) {
                          final budget = _budgets[index];
                          return _BudgetItemCard(
                            budget: budget,
                            currencyFormat: _currencyFormat,
                            getProgressColor: _getProgressColor,
                            getPeriodDisplay: _getPeriodDisplay,
                            onEdit: () => _editBudget(budget),
                            onDelete: () => _deleteBudget(budget),
                          );
                        },
                      ),
              ],
            ),
          ),
        ] else ...[
          // Card thêm ngân sách (hiển thị thay cho danh sách)
          _selectedCategory != null
              ? BudgetForm(
                  selectedCategory: _selectedCategory!,
                  onBack: () {
                    setState(() {
                      _selectedCategory = null;
                    });
                  },
                  onSuccess: _onBudgetCreated,
                )
              : _buildAddBudgetCard(),
        ],
      ],
    );
  }

  /// Widget card chọn danh mục với header và nút quay lại
  Widget _buildAddBudgetCard() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.08),
            blurRadius: 16,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header với nút quay lại
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  const Color(0xFFD7006E).withValues(alpha: 0.05),
                  const Color(0xFFFF4081).withValues(alpha: 0.02),
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(16),
                topRight: Radius.circular(16),
              ),
            ),
            child: Row(
              children: [
                // Nút quay lại
                InkWell(
                  onTap: _hideAddBudgetForm,
                  borderRadius: BorderRadius.circular(10),
                  child: Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(10),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withValues(alpha: 0.08),
                          blurRadius: 4,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: const Icon(
                      Icons.arrow_back,
                      color: Color(0xFFD7006E),
                      size: 20,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                // Tiêu đề
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Thêm ngân sách mới',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimary,
                        ),
                      ),
                      Text(
                        'Chọn danh mục để thiết lập ngân sách',
                        style: TextStyle(
                          fontSize: 12,
                          color: AppColors.textSecondary.withValues(alpha: 0.8),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          // Nội dung - Grid danh mục (không có container wrapper)
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 20),
            child: BudgetCategoryGrid(
              onCategorySelected: _onCategorySelected,
              showContainer: false,
              showTitle: false,
            ),
          ),
        ],
      ),
    );
  }
}

/// Widget card hiển thị một ngân sách trong danh sách
class _BudgetItemCard extends StatelessWidget {
  final BudgetResponse budget;
  final NumberFormat currencyFormat;
  final Color Function(double) getProgressColor;
  final String Function(String) getPeriodDisplay;
  final VoidCallback onEdit;
  final VoidCallback onDelete;

  const _BudgetItemCard({
    required this.budget,
    required this.currencyFormat,
    required this.getProgressColor,
    required this.getPeriodDisplay,
    required this.onEdit,
    required this.onDelete,
  });

  /// Lấy màu theo trạng thái ngân sách
  Color _getStatusColor(String status) {
    switch (status) {
      case 'EXCEEDED':
        return Colors.red;
      case 'WARNING':
        return Colors.orange;
      case 'OK':
        return Colors.green;
      case 'ACTIVE':
        return const Color(0xFFD7006E);
      default:
        return Colors.grey;
    }
  }

  /// Lấy icon theo trạng thái ngân sách
  IconData _getStatusIcon(String status) {
    switch (status) {
      case 'EXCEEDED':
        return Icons.warning_amber_rounded;
      case 'WARNING':
        return Icons.error_outline;
      case 'OK':
        return Icons.check_circle_outline;
      case 'ACTIVE':
        return Icons.trending_up;
      default:
        return Icons.info_outline;
    }
  }

  @override
  Widget build(BuildContext context) {
    // Lấy emoji và màu từ API
    final emoji = CategoryIconHelper.getEmoji(budget.categoryIcon);
    final iconColor = CategoryIconHelper.getColor(
      budget.categoryIcon,
      budget.categoryName ?? budget.name,
    );
    final progressColor = getProgressColor(budget.progressPercentage);
    final progressPercentage = budget.progressPercentage.clamp(0, 100);

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.grey.withValues(alpha: 0.03),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.grey.withValues(alpha: 0.1), width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header với icon, tên và actions
          Row(
            children: [
              // Emoji danh mục
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: iconColor.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Center(
                  child: Text(emoji, style: const TextStyle(fontSize: 26)),
                ),
              ),
              const SizedBox(width: 12),
              // Tên và chu kỳ
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      budget.name,
                      style: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textPrimary,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 2),
                    Row(
                      children: [
                        Flexible(
                          child: Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 8,
                              vertical: 2,
                            ),
                            decoration: BoxDecoration(
                              color: iconColor.withValues(alpha: 0.1),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              getPeriodDisplay(budget.period),
                              style: TextStyle(
                                fontSize: 10,
                                fontWeight: FontWeight.w500,
                                color: iconColor,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ),
                        if (budget.categoryName != null) ...[
                          const SizedBox(width: 6),
                          Flexible(
                            child: Text(
                              budget.categoryName!,
                              style: const TextStyle(
                                fontSize: 11,
                                color: AppColors.textSecondary,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ],
                ),
              ),
              // Các nút action
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Nút sửa
                  InkWell(
                    onTap: onEdit,
                    borderRadius: BorderRadius.circular(8),
                    child: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.blue.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Icon(
                        Icons.edit_outlined,
                        color: Colors.blue,
                        size: 18,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  // Nút xóa
                  InkWell(
                    onTap: onDelete,
                    borderRadius: BorderRadius.circular(8),
                    child: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.red.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Icon(
                        Icons.delete_outline,
                        color: Colors.red,
                        size: 18,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 16),
          // Progress bar
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Đã chi: ${currencyFormat.format(budget.spentAmount)}',
                    style: TextStyle(
                      fontSize: 12,
                      color: progressColor,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  Text(
                    '${progressPercentage.toStringAsFixed(0)}%',
                    style: TextStyle(
                      fontSize: 12,
                      color: progressColor,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              // Progress bar
              Stack(
                children: [
                  Container(
                    height: 8,
                    decoration: BoxDecoration(
                      color: Colors.grey.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                  AnimatedContainer(
                    duration: const Duration(milliseconds: 500),
                    height: 8,
                    width:
                        (MediaQuery.of(context).size.width - 104) *
                        (progressPercentage / 100),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          progressColor,
                          progressColor.withValues(alpha: 0.7),
                        ],
                        begin: Alignment.centerLeft,
                        end: Alignment.centerRight,
                      ),
                      borderRadius: BorderRadius.circular(4),
                      boxShadow: [
                        BoxShadow(
                          color: progressColor.withValues(alpha: 0.3),
                          blurRadius: 4,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Flexible(
                    child: Text(
                      'Còn lại: ${currencyFormat.format(budget.remainingAmount)}',
                      style: const TextStyle(
                        fontSize: 12,
                        color: AppColors.textSecondary,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  Flexible(
                    child: Text(
                      'Ngân sách: ${currencyFormat.format(budget.amount)}',
                      style: const TextStyle(
                        fontSize: 12,
                        color: AppColors.textSecondary,
                        fontWeight: FontWeight.w500,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              // Trạng thái và số ngày còn lại
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // Trạng thái
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 10,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: _getStatusColor(
                        budget.status,
                      ).withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                        color: _getStatusColor(
                          budget.status,
                        ).withValues(alpha: 0.3),
                        width: 1,
                      ),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          _getStatusIcon(budget.status),
                          color: _getStatusColor(budget.status),
                          size: 14,
                        ),
                        const SizedBox(width: 4),
                        Text(
                          budget.statusDisplay,
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                            color: _getStatusColor(budget.status),
                          ),
                        ),
                      ],
                    ),
                  ),
                  // Số ngày còn lại
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        Icons.access_time,
                        color: budget.daysRemaining <= 7
                            ? Colors.orange
                            : AppColors.textSecondary,
                        size: 14,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        budget.daysRemaining > 0
                            ? 'Còn ${budget.daysRemaining} ngày'
                            : 'Đã hết hạn',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w500,
                          color: budget.daysRemaining <= 7
                              ? Colors.orange
                              : AppColors.textSecondary,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }
}
