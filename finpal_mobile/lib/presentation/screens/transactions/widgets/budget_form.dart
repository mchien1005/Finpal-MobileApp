import 'package:flutter/material.dart';
import '../../../../core/constants/app_colors.dart';
import '../../../../core/utils/category_icon_helper.dart';
import '../../../../data/models/category.dart';
import '../../../../data/models/budget_model.dart';
import '../../../../data/services/budget_service.dart';
import '../../../../core/widgets/success_notification_dialog.dart';

/// Widget form tạo ngân sách mới
class BudgetForm extends StatefulWidget {
  final Category selectedCategory;
  final VoidCallback onBack;
  final VoidCallback onSuccess;

  const BudgetForm({
    super.key,
    required this.selectedCategory,
    required this.onBack,
    required this.onSuccess,
  });

  @override
  State<BudgetForm> createState() => _BudgetFormState();
}

class _BudgetFormState extends State<BudgetForm> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _amountController = TextEditingController();

  String? _selectedPeriod;
  DateTime? _startDate;
  DateTime? _endDate;
  bool _isLoading = false;

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
    _startDate = DateTime.now();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  /// Xử lý tạo ngân sách
  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) {
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
        categoryId: widget.selectedCategory.id,
      );

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
    // Lấy icon và màu từ API thông qua CategoryIconHelper
    final icon = CategoryIconHelper.getIconWithFallback(
      widget.selectedCategory.icon,
      widget.selectedCategory.name,
    );
    final iconColor = CategoryIconHelper.getColor(
      widget.selectedCategory.icon,
      widget.selectedCategory.name,
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
            // Header với nút đổi danh mục
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Tạo ngân sách mới',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textPrimary,
                  ),
                ),
                TextButton(
                  onPressed: widget.onBack,
                  child: const Text(
                    'Đổi danh mục',
                    style: TextStyle(
                      color: Color(0xFFD7006E),
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Hiển thị danh mục đã chọn
            Row(
              children: [
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: CategoryIconHelper.getBackgroundColor(iconColor),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Icon(icon, color: iconColor, size: 20),
                ),
                const SizedBox(width: 12),
                Text(
                  widget.selectedCategory.name,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w500,
                    color: AppColors.textPrimary,
                  ),
                ),
              ],
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

            // Nút tạo ngân sách
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
                      child: const Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.add, size: 18),
                          SizedBox(width: 8),
                          Text(
                            'Tạo ngân sách',
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ),
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
  Widget _buildDatePicker({DateTime? date, required VoidCallback onTap}) {
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
            const Icon(
              Icons.calendar_today,
              size: 20,
              color: AppColors.textPlaceholder,
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
