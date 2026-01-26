import 'package:flutter/material.dart';
import '../../../../core/constants/app_colors.dart';
import '../../../../core/utils/category_icon_helper.dart';
import '../../../../data/models/category.dart';
import '../../../../data/models/budget_model.dart';
import '../../../../data/services/budget_service.dart';
import '../../../../data/services/category_cache_service.dart';
import '../../../../core/widgets/success_notification_dialog.dart';

/// Widget form tạo/sửa ngân sách
/// Hỗ trợ cả 2 chế độ: thêm mới (với selectedCategory) và sửa (với existingBudget)
class BudgetForm extends StatefulWidget {
  /// Danh mục được chọn (dùng khi thêm mới)
  final Category? selectedCategory;

  /// Ngân sách cần sửa (dùng khi edit mode)
  final BudgetResponse? existingBudget;

  /// Callback khi nhấn nút quay lại
  final VoidCallback onBack;

  /// Callback khi thêm/sửa thành công
  final VoidCallback onSuccess;

  const BudgetForm({
    super.key,
    this.selectedCategory,
    this.existingBudget,
    required this.onBack,
    required this.onSuccess,
  }) : assert(
         selectedCategory != null || existingBudget != null,
         'Phải có selectedCategory (thêm mới) hoặc existingBudget (sửa)',
       );

  /// Kiểm tra xem đang ở chế độ sửa hay không
  bool get isEditMode => existingBudget != null;

  @override
  State<BudgetForm> createState() => _BudgetFormState();
}

class _BudgetFormState extends State<BudgetForm> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _amountController = TextEditingController();
  final _categoryCacheService = CategoryCacheService.instance;

  String? _selectedPeriod;
  DateTime? _startDate;
  DateTime? _endDate;
  bool _isLoading = false;

  // Danh mục hiện tại (có thể thay đổi khi sửa)
  Category? _currentCategory;
  List<Category> _categories = [];
  bool _isLoadingCategories = false;
  bool _showCategorySelector = false;

  // Danh sách các kỳ hạn
  final List<Map<String, String>> _periods = [
    {'value': 'WEEKLY', 'name': 'Hàng tuần'},
    {'value': 'MONTHLY', 'name': 'Hàng tháng'},
    {'value': 'QUATERLY', 'name': 'Hàng quý'},
    {'value': 'YEARLY', 'name': 'Hàng năm'},
  ];

  @override
  void initState() {
    super.initState();
    _initializeForm();
  }

  /// Khởi tạo form với data ban đầu
  void _initializeForm() {
    if (widget.isEditMode) {
      // Chế độ sửa: điền sẵn data từ ngân sách hiện có
      final budget = widget.existingBudget!;
      _nameController.text = budget.name;
      _amountController.text = budget.amount.toStringAsFixed(0);
      _selectedPeriod = budget.period;
      _startDate = budget.startDate;
      _endDate = budget.endDate;
      // Tạo Category từ thông tin budget
      _currentCategory = Category(
        id: budget.categoryId ?? 0,
        name: budget.categoryName ?? budget.name,
        type: 'EXPENSE',
        icon: budget.categoryIcon,
        color: budget.categoryColor, // Sử dụng màu từ CSDL
        isSystem: true,
        displayOrder: 0,
      );
    } else {
      // Chế độ thêm mới: sử dụng danh mục được chọn
      _currentCategory = widget.selectedCategory;
      _startDate = DateTime.now();
    }
  }

  /// Tải danh sách danh mục chi tiêu
  Future<void> _loadCategories() async {
    setState(() {
      _isLoadingCategories = true;
    });

    try {
      // Sử dụng cache service - lấy từ cache nếu đã có
      final categories = await _categoryCacheService.getCategories('EXPENSE');
      setState(() {
        _categories = categories;
        _isLoadingCategories = false;
      });
    } catch (e) {
      setState(() {
        _isLoadingCategories = false;
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

  /// Mở bộ chọn danh mục
  void _openCategorySelector() {
    if (_categories.isEmpty) {
      _loadCategories();
    }
    setState(() {
      _showCategorySelector = true;
    });
  }

  /// Chọn danh mục mới
  void _selectCategory(Category category) {
    setState(() {
      _currentCategory = category;
      _showCategorySelector = false;
    });
  }

  /// Đóng bộ chọn danh mục
  void _closeCategorySelector() {
    setState(() {
      _showCategorySelector = false;
    });
  }

  @override
  void dispose() {
    _nameController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  /// Lấy tên danh mục
  String get _categoryName {
    return _currentCategory?.name ?? 'Chưa chọn danh mục';
  }

  /// Lấy icon danh mục
  String? get _categoryIcon {
    return _currentCategory?.icon;
  }

  /// Lấy category ID
  int? get _categoryId {
    return _currentCategory?.id;
  }

  /// Xử lý submit form (tạo mới hoặc cập nhật)
  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    if (_currentCategory == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng chọn danh mục'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    if (_selectedPeriod == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng chọn kỳ hạn'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    if (_startDate == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng chọn ngày bắt đầu'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    if (_endDate == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng chọn ngày kết thúc'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      final request = BudgetRequest(
        name: _nameController.text.trim(),
        amount: double.parse(_amountController.text),
        period: _selectedPeriod!,
        startDate: _startDate!,
        endDate: _endDate,
        categoryId: _categoryId,
      );

      if (widget.isEditMode) {
        // Cập nhật ngân sách
        await BudgetService.updateBudget(widget.existingBudget!.id, request);
        if (mounted) {
          await SuccessNotificationDialog.show(
            context,
            message: 'Cập nhật ngân sách thành công!',
            onConfirm: () {
              widget.onSuccess();
            },
          );
        }
      } else {
        // Tạo ngân sách mới
        await BudgetService.createBudget(request);
        if (mounted) {
          await SuccessNotificationDialog.show(
            context,
            message: 'Tạo ngân sách thành công!',
            onConfirm: () {
              widget.onSuccess();
            },
          );
        }
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
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    // Nếu đang hiển thị bộ chọn danh mục
    if (_showCategorySelector) {
      return _buildCategorySelector();
    }

    // Lấy emoji và màu từ CategoryIconHelper (sử dụng màu từ API)
    final emoji = CategoryIconHelper.getEmoji(_categoryIcon);
    final iconColor = CategoryIconHelper.getColor(
      _categoryIcon,
      _categoryName,
      colorFromApi: _currentCategory?.color,
    );

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header với nút đóng
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  widget.isEditMode ? 'Sửa ngân sách' : 'Tạo ngân sách mới',
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textPrimary,
                  ),
                ),
                GestureDetector(
                  onTap: widget.onBack,
                  child: Container(
                    padding: const EdgeInsets.all(6),
                    decoration: BoxDecoration(
                      color: Colors.grey.shade100,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Icon(
                      Icons.close,
                      size: 20,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Hiển thị danh mục (emoji + tên) - có thể click để đổi
            InkWell(
              onTap: _openCategorySelector,
              borderRadius: BorderRadius.circular(12),
              child: Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: CategoryIconHelper.getBackgroundColor(iconColor),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: iconColor.withOpacity(0.3),
                    width: 1,
                  ),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 44,
                      height: 44,
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(10),
                        boxShadow: [
                          BoxShadow(
                            color: iconColor.withOpacity(0.2),
                            blurRadius: 4,
                            offset: const Offset(0, 2),
                          ),
                        ],
                      ),
                      child: Center(
                        child: Text(
                          emoji,
                          style: const TextStyle(fontSize: 24),
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Danh mục',
                            style: TextStyle(
                              fontSize: 12,
                              color: AppColors.textSecondary,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            _categoryName,
                            style: const TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w600,
                              color: AppColors.textPrimary,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                    Icon(Icons.chevron_right, color: iconColor, size: 24),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Tên ngân sách
            _buildLabel('Tên ngân sách'),
            const SizedBox(height: 8),
            TextFormField(
              controller: _nameController,
              decoration: _buildInputDecoration(
                hintText: 'Ví dụ: Ăn uống tháng 12',
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Vui lòng nhập tên ngân sách';
                }
                return null;
              },
            ),
            const SizedBox(height: 20),

            // Số tiền
            _buildLabel('Số tiền (VNĐ)'),
            const SizedBox(height: 8),
            TextFormField(
              controller: _amountController,
              keyboardType: TextInputType.number,
              decoration: _buildInputDecoration(hintText: '0', suffixText: 'đ'),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Vui lòng nhập số tiền';
                }
                if (double.tryParse(value) == null) {
                  return 'Số tiền không hợp lệ';
                }
                return null;
              },
            ),
            const SizedBox(height: 20),

            // Kỳ hạn
            _buildLabel('Kỳ hạn'),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              value: _selectedPeriod,
              hint: const Text('Chọn kỳ hạn'),
              decoration: _buildInputDecoration(),
              items: _periods
                  .map(
                    (period) => DropdownMenuItem(
                      value: period['value'],
                      child: Text(period['name']!),
                    ),
                  )
                  .toList(),
              onChanged: (value) {
                setState(() {
                  _selectedPeriod = value;
                });
              },
            ),
            const SizedBox(height: 20),

            // Ngày bắt đầu
            _buildLabel('Ngày bắt đầu'),
            const SizedBox(height: 8),
            _buildDatePicker(
              date: _startDate,
              onTap: () => _selectDate(isStartDate: true),
            ),
            const SizedBox(height: 20),

            // Ngày kết thúc
            _buildLabel('Ngày kết thúc'),
            const SizedBox(height: 8),
            _buildDatePicker(
              date: _endDate,
              onTap: () => _selectDate(isStartDate: false),
            ),
            const SizedBox(height: 32),

            // Nút submit
            SizedBox(
              width: double.infinity,
              child: _isLoading
                  ? const Center(
                      child: CircularProgressIndicator(
                        color: Color(0xFFD7006E),
                      ),
                    )
                  : ElevatedButton(
                      onPressed: _handleSubmit,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFFD7006E),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            widget.isEditMode ? Icons.save : Icons.add,
                            size: 18,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            widget.isEditMode
                                ? 'Lưu thay đổi'
                                : 'Tạo ngân sách',
                            style: const TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ),
            ),

            // Nút hủy (chỉ hiện ở chế độ sửa)
            if (widget.isEditMode) ...[
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: OutlinedButton(
                  onPressed: widget.onBack,
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.textSecondary,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    side: BorderSide(color: Colors.grey.shade300),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: const Text(
                    'Hủy',
                    style: TextStyle(fontSize: 15, fontWeight: FontWeight.w500),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  /// Build bộ chọn danh mục
  Widget _buildCategorySelector() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            children: [
              GestureDetector(
                onTap: _closeCategorySelector,
                child: Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.grey.shade100,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.arrow_back,
                    size: 20,
                    color: AppColors.textPrimary,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              const Text(
                'Chọn danh mục',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: AppColors.textPrimary,
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),

          // Grid danh mục
          _isLoadingCategories
              ? const Center(
                  child: Padding(
                    padding: EdgeInsets.all(40),
                    child: CircularProgressIndicator(color: Color(0xFFD7006E)),
                  ),
                )
              : _categories.isEmpty
              ? const Center(
                  child: Padding(
                    padding: EdgeInsets.all(40),
                    child: Text(
                      'Không có danh mục nào',
                      style: TextStyle(color: AppColors.textSecondary),
                    ),
                  ),
                )
              : GridView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 3,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 0.9,
                  ),
                  itemCount: _categories.length,
                  itemBuilder: (context, index) {
                    final category = _categories[index];
                    final isSelected = _currentCategory?.id == category.id;
                    return _buildCategoryItem(category, isSelected);
                  },
                ),
        ],
      ),
    );
  }

  /// Build item danh mục
  Widget _buildCategoryItem(Category category, bool isSelected) {
    final emoji = CategoryIconHelper.getEmoji(category.icon);
    final iconColor = CategoryIconHelper.getColor(
      category.icon,
      category.name,
      colorFromApi: category.color,
    );
    final bgColor = CategoryIconHelper.getBackgroundColor(iconColor);

    return InkWell(
      onTap: () => _selectCategory(category),
      borderRadius: BorderRadius.circular(10),
      child: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: isSelected ? iconColor.withOpacity(0.2) : bgColor,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: isSelected ? iconColor : iconColor.withOpacity(0.2),
            width: isSelected ? 2 : 1,
          ),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(10),
                boxShadow: [
                  BoxShadow(
                    color: iconColor.withOpacity(0.2),
                    blurRadius: 4,
                    offset: const Offset(0, 2),
                  ),
                ],
              ),
              child: Center(
                child: Text(emoji, style: const TextStyle(fontSize: 22)),
              ),
            ),
            const SizedBox(height: 6),
            Text(
              category.name,
              style: TextStyle(
                fontSize: 11,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
                color: isSelected ? iconColor : iconColor.withOpacity(0.9),
              ),
              textAlign: TextAlign.center,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }

  /// Build label cho các trường
  Widget _buildLabel(String text) {
    return Text(
      text,
      style: const TextStyle(
        fontSize: 14,
        color: AppColors.textPrimary,
        fontWeight: FontWeight.w500,
      ),
    );
  }

  /// Build decoration cho input
  InputDecoration _buildInputDecoration({
    String? hintText,
    String? suffixText,
  }) {
    return InputDecoration(
      hintText: hintText,
      suffixText: suffixText,
      filled: true,
      fillColor: AppColors.inputBackground,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(8),
        borderSide: BorderSide.none,
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
    );
  }

  /// Build date picker
  Widget _buildDatePicker({
    DateTime? date,
    required VoidCallback onTap,
    bool showClearButton = false,
    VoidCallback? onClear,
  }) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: AppColors.inputBackground,
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              date != null
                  ? '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}'
                  : 'dd/mm/yyyy',
              style: TextStyle(
                fontSize: 16,
                color: date != null
                    ? AppColors.textPrimary
                    : AppColors.textPlaceholder,
              ),
            ),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (showClearButton && onClear != null)
                  GestureDetector(
                    onTap: onClear,
                    child: const Padding(
                      padding: EdgeInsets.only(right: 8),
                      child: Icon(Icons.close, size: 18, color: Colors.grey),
                    ),
                  ),
                const Icon(
                  Icons.calendar_today,
                  size: 20,
                  color: AppColors.textPlaceholder,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  /// Chọn ngày
  Future<void> _selectDate({required bool isStartDate}) async {
    final initialDate = isStartDate
        ? (_startDate ?? DateTime.now())
        : (_endDate ?? DateTime.now().add(const Duration(days: 30)));

    final firstDate = isStartDate
        ? DateTime(2000)
        : (_startDate ?? DateTime.now());

    final date = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: firstDate,
      lastDate: DateTime(2100),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.light(primary: Color(0xFFD7006E)),
            datePickerTheme: const DatePickerThemeData(
              backgroundColor: Colors.white,
              headerBackgroundColor: Color(0xFFD7006E),
              headerForegroundColor: Colors.white,
              surfaceTintColor: Colors.transparent,
              dayStyle: TextStyle(color: Colors.black),
              yearStyle: TextStyle(color: Colors.black),
            ),
          ),
          child: child!,
        );
      },
    );

    if (date != null) {
      setState(() {
        if (isStartDate) {
          _startDate = date;
        } else {
          _endDate = date;
        }
      });
    }
  }
}
