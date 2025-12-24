import 'package:flutter/material.dart';
import '../../../../core/constants/app_colors.dart';
import '../../../../core/utils/category_icon_helper.dart';
import '../../../../data/models/category.dart';
import '../../../../data/services/transaction_service.dart';
import '../../../../core/widgets/success_notification_dialog.dart';
import '../../../widgets/sms_scanner_banner.dart';

/// Widget tab thêm giao dịch
class AddTransactionTab extends StatefulWidget {
  final List<Category> categories;
  final bool isCategoriesLoading;
  final String transactionType;
  final Function(String) onTransactionTypeChanged;

  const AddTransactionTab({
    super.key,
    required this.categories,
    required this.isCategoriesLoading,
    required this.transactionType,
    required this.onTransactionTypeChanged,
  });

  @override
  State<AddTransactionTab> createState() => _AddTransactionTabState();
}

class _AddTransactionTabState extends State<AddTransactionTab> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _transactionService = TransactionService();

  String? _selectedSource;
  Category? _selectedCategory;
  DateTime? _selectedDate;
  bool _isLoading = false;

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _handleAddTransaction() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    if (_selectedSource == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng chọn nguồn giao dịch'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    if (_selectedDate == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng chọn ngày giao dịch'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      final amount = double.parse(_amountController.text);
      final type = widget.transactionType == 'expense' ? 'EXPENSE' : 'INCOME';

      await _transactionService.addTransaction(
        type: type,
        amount: amount,
        transactionSource: _selectedSource!,
        categoryId: _selectedCategory?.id,
        description: _descriptionController.text,
        transactionDate: _selectedDate!,
        isAuto: false,
      );

      if (mounted) {
        await SuccessNotificationDialog.show(
          context,
          message: 'Thêm giao dịch thành công!',
          onConfirm: () {
            _amountController.clear();
            _descriptionController.clear();
            setState(() {
              _selectedSource = null;
              _selectedCategory = null;
              _selectedDate = null;
            });
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
    return SingleChildScrollView(
      child: Column(
        children: [
          // SMS Auto-fill Banner
          const SmsScannerBanner(),

          // Form Container
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.05),
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
                  const Text(
                    'Thêm giao dịch thủ công',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 24),

                  // Transaction Type Toggle
                  _buildLabel('Loại giao dịch'),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Expanded(child: _buildTypeButton('Chi tiêu', 'expense')),
                      const SizedBox(width: 12),
                      Expanded(child: _buildTypeButton('Thu nhập', 'income')),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // Amount Field
                  _buildLabel('Số tiền (VNĐ)'),
                  const SizedBox(height: 8),
                  TextFormField(
                    controller: _amountController,
                    keyboardType: TextInputType.number,
                    style: const TextStyle(fontSize: 16),
                    decoration: _buildInputDecoration(
                      hintText: '0',
                      suffixText: 'đ',
                    ),
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return 'Vui lòng nhập số tiền';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 20),

                  // Source Dropdown
                  _buildLabel('Nguồn giao dịch'),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<String>(
                    value: _selectedSource,
                    hint: const Text('Chọn danh mục'),
                    decoration: _buildInputDecoration(),
                    items: ['Tiền mặt', 'Ngân hàng', 'Ví điện tử']
                        .map(
                          (item) =>
                              DropdownMenuItem(value: item, child: Text(item)),
                        )
                        .toList(),
                    onChanged: (value) {
                      setState(() {
                        _selectedSource = value;
                      });
                    },
                  ),
                  const SizedBox(height: 20),

                  // Category Dropdown
                  _buildLabel('Danh mục'),
                  const SizedBox(height: 8),
                  widget.isCategoriesLoading
                      ? Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 12,
                          ),
                          decoration: BoxDecoration(
                            color: AppColors.inputBackground,
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: const Row(
                            children: [
                              SizedBox(
                                width: 16,
                                height: 16,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                ),
                              ),
                              SizedBox(width: 12),
                              Text('Đang tải danh mục...'),
                            ],
                          ),
                        )
                      : DropdownButtonFormField<Category>(
                          // ignore: deprecated_member_use
                          value: _selectedCategory,
                          hint: const Text('Chọn danh mục'),
                          decoration: _buildInputDecoration(),
                          menuMaxHeight: 300, // Giới hạn chiều cao dropdown
                          isExpanded: true,
                          items: widget.categories
                              .map(
                                (category) => DropdownMenuItem<Category>(
                                  value: category,
                                  child: Row(
                                    children: [
                                      Container(
                                        width: 24,
                                        height: 24,
                                        decoration: BoxDecoration(
                                          color:
                                              CategoryIconHelper.getBackgroundColor(
                                                CategoryIconHelper.getColor(
                                                  category.icon,
                                                  category.name,
                                                ),
                                              ),
                                          borderRadius: BorderRadius.circular(
                                            5,
                                          ),
                                        ),
                                        child: Icon(
                                          CategoryIconHelper.getIconWithFallback(
                                            category.icon,
                                            category.name,
                                          ),
                                          size: 14,
                                          color: CategoryIconHelper.getColor(
                                            category.icon,
                                            category.name,
                                          ),
                                        ),
                                      ),
                                      const SizedBox(width: 10),
                                      Expanded(
                                        child: Text(
                                          category.name,
                                          style: const TextStyle(fontSize: 14),
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              )
                              .toList(),
                          onChanged: (value) {
                            setState(() {
                              _selectedCategory = value;
                            });
                          },
                        ),
                  const SizedBox(height: 20),

                  // Description Field
                  _buildLabel('Mô tả'),
                  const SizedBox(height: 8),
                  TextFormField(
                    controller: _descriptionController,
                    maxLines: 3,
                    decoration: _buildInputDecoration(
                      hintText: 'Ví dụ: Mua bánh mì sáng, đổ xăng...',
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Date Field
                  _buildLabel('Ngày giao dịch'),
                  const SizedBox(height: 8),
                  _buildDatePicker(),
                  const SizedBox(height: 32),

                  // Add Transaction Button
                  SizedBox(
                    width: double.infinity,
                    child: _isLoading
                        ? const Center(
                            child: CircularProgressIndicator(
                              color: Color(0xFFD7006E),
                            ),
                          )
                        : ElevatedButton(
                            onPressed: _handleAddTransaction,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFFD7006E),
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(vertical: 12),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8),
                              ),
                            ),
                            child: const Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(Icons.add, size: 16),
                                SizedBox(width: 8),
                                Text(
                                  'Thêm giao dịch',
                                  style: TextStyle(fontSize: 14),
                                ),
                              ],
                            ),
                          ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // Recent SMS Card
          _buildRecentSmsCard(),
          const SizedBox(height: 100),
        ],
      ),
    );
  }

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

  Widget _buildTypeButton(String label, String type) {
    final isSelected = widget.transactionType == type;
    return InkWell(
      onTap: () {
        widget.onTransactionTypeChanged(type);
        setState(() {
          _selectedCategory = null;
        });
      },
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: isSelected
              ? (type == 'expense'
                    ? const Color(0xFFFB2C36)
                    : const Color(0xFF00C950))
              : Colors.white,
          border: Border.all(
            color: isSelected ? Colors.transparent : Colors.grey.shade300,
            width: 1.3,
          ),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Text(
          label,
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
            color: isSelected ? Colors.white : AppColors.textPrimary,
          ),
        ),
      ),
    );
  }

  Widget _buildDatePicker() {
    return InkWell(
      onTap: () async {
        final date = await showDatePicker(
          context: context,
          initialDate: _selectedDate ?? DateTime.now(),
          firstDate: DateTime(2000),
          lastDate: DateTime(2100),
        );
        if (date != null) {
          setState(() {
            _selectedDate = date;
          });
        }
      },
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
              _selectedDate != null
                  ? '${_selectedDate!.day}/${_selectedDate!.month}/${_selectedDate!.year}'
                  : 'Chọn ngày',
              style: TextStyle(
                fontSize: 16,
                color: _selectedDate != null
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

  Widget _buildRecentSmsCard() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: Colors.black.withOpacity(0.1), width: 1.275),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'SMS gần đây',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w400,
                  color: Colors.black,
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 9.275,
                  vertical: 3.275,
                ),
                decoration: BoxDecoration(
                  color: const Color(0xFFECEEF2),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Text(
                  '3 tin mới',
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w400,
                    color: Color(0xFF030213),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 28),
          _buildSmsItem(
            bankName: 'VCB',
            amount: '+15,000,000đ',
            description: 'ND: Chuyen tien luong',
            dateTime: '01/11/2025 08:00',
            isIncome: true,
          ),
          const SizedBox(height: 8),
          _buildSmsItem(
            bankName: 'Techcombank',
            amount: '-125,000đ',
            description: 'ND: THE COFFEE HOUSE',
            dateTime: '13/11/2025 10:30',
            isIncome: false,
          ),
          const SizedBox(height: 8),
          _buildSmsItem(
            bankName: 'ACB',
            amount: '-450,000đ',
            description: 'ND: SHOPEE',
            dateTime: '12/11/2025 20:15',
            isIncome: false,
          ),
        ],
      ),
    );
  }

  Widget _buildSmsItem({
    required String bankName,
    required String amount,
    required String description,
    required String dateTime,
    required bool isIncome,
  }) {
    return Container(
      padding: const EdgeInsets.only(
        left: 13.264,
        right: 13.265,
        top: 13.265,
        bottom: 1.275,
      ),
      decoration: BoxDecoration(
        color: isIncome ? const Color(0xFFF0FDF4) : const Color(0xFFEFF6FF),
        border: Border.all(
          color: isIncome ? const Color(0xFFB9F8CF) : const Color(0xFFBEDBFF),
          width: 1.275,
        ),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                bankName,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w400,
                  color: isIncome
                      ? const Color(0xFF016630)
                      : const Color(0xFF193CB8),
                ),
              ),
              Text(
                amount,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w400,
                  color: isIncome
                      ? const Color(0xFF00A63E)
                      : const Color(0xFFE7000B),
                ),
              ),
            ],
          ),
          const SizedBox(height: 3.983),
          Text(
            description,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w400,
              color: Color(0xFF4A5565),
            ),
          ),
          const SizedBox(height: 3.983),
          Text(
            dateTime,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w400,
              color: Color(0xFF99A1AF),
            ),
          ),
        ],
      ),
    );
  }
}
