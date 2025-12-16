import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/confirmation_dialog.dart';
import '../../../core/widgets/success_notification_dialog.dart';
class EditTransactionDialog extends StatefulWidget {
  final String title;
  final String amount;
  final String category;
  final String account;
  final String date;
  final bool isIncome;
  final Function({
    required String amount,
    required String source,
    required String category,
    required String description,
    required DateTime date,
  }) onSave;
  final VoidCallback onCancel;

  const EditTransactionDialog({
    super.key,
    required this.title,
    required this.amount,
    required this.category,
    required this.account,
    required this.date,
    required this.isIncome,
    required this.onSave,
    required this.onCancel,
  });

  @override
  State<EditTransactionDialog> createState() => _EditTransactionDialogState();
}

class _EditTransactionDialogState extends State<EditTransactionDialog> {
  late TextEditingController _amountController;
  late TextEditingController _descriptionController;
  late TextEditingController _dateController;
  String? _selectedSource;
  String? _selectedCategory;
  DateTime? _selectedDate;

  final List<String> _sources = [
    'VCB',
    'Techcombank',
    'ACB',
    'Tiền mặt',
  ];

  final Map<String, Map<String, dynamic>> _categories = {
    'Ăn uống': {
      'icon': Icons.coffee,
      'color': const Color(0xFFFF6B9D),
    },
    'Di chuyển': {
      'icon': Icons.directions_car,
      'color': const Color(0xFFFF4444),
    },
    'Mua sắm': {
      'icon': Icons.shopping_bag,
      'color': const Color(0xFF4D96FF),
    },
    'Giải trí': {
      'icon': Icons.music_note,
      'color': const Color(0xFFB660E0),
    },
    'Hóa đơn': {
      'icon': Icons.receipt_long,
      'color': const Color(0xFF9CA3AF),
    },
    'Sức khỏe': {
      'icon': Icons.favorite,
      'color': const Color(0xFFFF6B9D),
    },
    'Học tập': {
      'icon': Icons.school,
      'color': const Color(0xFF00D9A3),
    },
    'Khác': {
      'icon': Icons.more_horiz,
      'color': const Color(0xFFFF6B9D),
    },
    'Lương': {
      'icon': Icons.account_balance_wallet,
      'color': const Color(0xFF00D9A3),
    },
  };

  @override
  void initState() {
    super.initState();
    // Parse amount (remove đ and commas)
    String cleanAmount = widget.amount
        .replaceAll('đ', '')
        .replaceAll('.', '')
        .replaceAll('+', '')
        .replaceAll('-', '')
        .trim();
    _amountController = TextEditingController(text: cleanAmount);
    _descriptionController = TextEditingController(text: widget.title);
    _selectedSource = widget.account;
    _selectedCategory = widget.category;
    
    // Parse date from "11-13 09:00" format
    try {
      final parts = widget.date.split(' ');
      final dateParts = parts[0].split('-');
      final month = int.parse(dateParts[0]);
      final day = int.parse(dateParts[1]);
      _selectedDate = DateTime(2025, month, day);
      _dateController = TextEditingController(
        text: DateFormat('dd/MM/yyyy').format(_selectedDate!),
      );
    } catch (e) {
      _selectedDate = DateTime.now();
      _dateController = TextEditingController(
        text: DateFormat('dd/MM/yyyy').format(_selectedDate!),
      );
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    _dateController.dispose();
    super.dispose();
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate ?? DateTime.now(),
      firstDate: DateTime(2020),
      lastDate: DateTime(2030),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.light(
              primary: Color(0xFFD91656),
            ),
          ),
          child: child!,
        );
      },
    );
    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
        _dateController.text = DateFormat('dd/MM/yyyy').format(picked);
      });
    }
  }

  void _handleSave() {
    // Validate required fields
    if (_amountController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng nhập số tiền'),
          backgroundColor: Color(0xFFE7000B),
        ),
      );
      return;
    }

    if (_selectedSource == null || _selectedSource!.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng chọn nguồn giao dịch'),
          backgroundColor: Color(0xFFE7000B),
        ),
      );
      return;
    }

    if (_selectedCategory == null || _selectedCategory!.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng chọn danh mục'),
          backgroundColor: Color(0xFFE7000B),
        ),
      );
      return;
    }

    // Call onSave with updated data
    widget.onSave(
      amount: _amountController.text.trim(),
      source: _selectedSource!,
      category: _selectedCategory!,
      description: _descriptionController.text.trim(),
      date: _selectedDate ?? DateTime.now(),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
      ),
      insetPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 24),
      child: Container(
        constraints: const BoxConstraints(maxWidth: 400),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
        ),
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header with title and close button
                Row(
                  children: [
                    const Expanded(
                      child: Text(
                        'Chỉnh sửa giao dịch',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w600,
                          color: AppColors.textPrimary,
                        ),
                      ),
                    ),
                    InkWell(
                      onTap: widget.onCancel,
                      borderRadius: BorderRadius.circular(20),
                      child: Container(
                        padding: const EdgeInsets.all(4),
                        child: const Icon(
                          Icons.close,
                          size: 24,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                const Text(
                  'Cập nhật thông tin giao dịch. Phần hồi của bạn giúp \nAI học và cải thiện độ chính xác.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 14,
                    color: AppColors.textSecondary,
                  ),
                ),
                const SizedBox(height: 24),
            
            // Số tiền field
            const Text(
              'Số tiền (VNĐ)',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _amountController,
              keyboardType: TextInputType.number,
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontSize: 14,
              ),
              decoration: InputDecoration(
                hintText: '55000',
                hintStyle: const TextStyle(
                  color: Color(0xFF9CA3AF),
                  fontSize: 14,
                ),
                suffixText: 'đ',
                suffixStyle: const TextStyle(
                  color: AppColors.textPrimary,
                  fontSize: 14,
                ),
                filled: true,
                fillColor: const Color(0xFFF3F3F5),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD91656),
                    width: 2,
                  ),
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 14,
                  vertical: 10,
                ),
              ),
            ),
            const SizedBox(height: 16),
            
            // Nguồn giao dịch dropdown
            const Text(
              'Nguồn giao dịch',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              value: _selectedSource,
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontSize: 14,
              ),
              decoration: InputDecoration(
                hintText: 'Chọn nguồn',
                hintStyle: const TextStyle(
                  color: Color(0xFF9CA3AF),
                  fontSize: 14,
                ),
                filled: true,
                fillColor: const Color(0xFFF3F3F5),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD91656),
                    width: 2,
                  ),
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 14,
                  vertical: 10,
                ),
              ),
              items: _sources.map((source) {
                return DropdownMenuItem(
                  value: source,
                  child: Text(source),
                );
              }).toList(),
              onChanged: (value) {
                setState(() {
                  _selectedSource = value;
                });
              },
            ),
            const SizedBox(height: 16),
            
            // Danh mục dropdown
            const Text(
              'Danh mục',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              value: _selectedCategory,
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontSize: 14,
              ),
              decoration: InputDecoration(
                hintText: 'Chọn danh mục',
                hintStyle: const TextStyle(
                  color: Color(0xFF9CA3AF),
                  fontSize: 14,
                ),
                filled: true,
                fillColor: const Color(0xFFF3F3F5),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD91656),
                    width: 2,
                  ),
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 14,
                  vertical: 10,
                ),
              ),
              items: _categories.keys.map((category) {
                final categoryData = _categories[category]!;
                return DropdownMenuItem(
                  value: category,
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        categoryData['icon'] as IconData,
                        size: 20,
                        color: categoryData['color'] as Color,
                      ),
                      const SizedBox(width: 12),
                      Text(category),
                    ],
                  ),
                );
              }).toList(),
              onChanged: (value) {
                setState(() {
                  _selectedCategory = value;
                });
              },
            ),
            const SizedBox(height: 16),
            
            // Mô tả field
            const Text(
              'Mô tả',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _descriptionController,
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontSize: 14,
              ),
              decoration: InputDecoration(
                hintText: 'GRAB',
                hintStyle: const TextStyle(
                  color: Color(0xFF9CA3AF),
                  fontSize: 14,
                ),
                filled: true,
                fillColor: const Color(0xFFF3F3F5),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD91656),
                    width: 2,
                  ),
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 14,
                  vertical: 10,
                ),
              ),
            ),
            const SizedBox(height: 16),
            
            // Ngày giao dịch field
            const Text(
              'Ngày giao dịch',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _dateController,
              readOnly: true,
              onTap: _selectDate,
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontSize: 14,
              ),
              decoration: InputDecoration(
                hintText: '13/12/2025',
                hintStyle: const TextStyle(
                  color: Color(0xFF9CA3AF),
                  fontSize: 14,
                ),
                suffixIcon: const Icon(
                  Icons.calendar_today,
                  size: 20,
                  color: Color(0xFF6B7280),
                ),
                filled: true,
                fillColor: const Color(0xFFF3F3F5),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD1D5DB),
                    width: 1,
                  ),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(
                    color: Color(0xFFD91656),
                    width: 2,
                  ),
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 14,
                  vertical: 10,
                ),
              ),
            ),
            const SizedBox(height: 24),
            
            // Action buttons
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: widget.onCancel,
                    style: OutlinedButton.styleFrom(
                      backgroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      side: const BorderSide(
                        color: Color(0xFFD1D5DB),
                        width: 1,
                      ),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                    child: const Text(
                      'Hủy',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: _handleSave,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFD91656),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                      elevation: 0,
                    ),
                    child: const Text(
                      'Lưu thay đổi',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                        color: Colors.white,
                      ),
                    ),
                  ),
                ),
              ],
            ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
