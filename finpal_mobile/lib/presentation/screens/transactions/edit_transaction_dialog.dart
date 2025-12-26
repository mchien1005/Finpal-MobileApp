import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/models/category.dart';
import '../../../data/services/transaction_service.dart';

class EditTransactionDialog extends StatefulWidget {
  final String title;
  final double amount;
  final String category;
  final int? categoryId;
  final String account;
  final DateTime date;
  final bool isIncome;
  final Function({
    required double amount,
    required String source,
    required String category,
    required int? categoryId,
    required String description,
    required DateTime date,
  }) onSave;
  final VoidCallback onCancel;

  const EditTransactionDialog({
    super.key,
    required this.title,
    required this.amount,
    required this.category,
    this.categoryId,
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
  int? _selectedCategoryId;
  DateTime? _selectedDate;

  final TransactionService _transactionService = TransactionService();
  List<Category> _categories = [];
  bool _isLoadingCategories = false;

  final List<String> _sources = ['VCB', 'Techcombank', 'ACB', 'Tiền mặt', 'Ngân hàng'];

  @override
  void initState() {
    super.initState();
    _amountController = TextEditingController(text: widget.amount.toInt().toString());
    _descriptionController = TextEditingController(text: widget.title);
    
    // Add account to sources list if not exists
    if (widget.account.isNotEmpty && !_sources.contains(widget.account)) {
      _sources.add(widget.account);
    }
    _selectedSource = widget.account.isNotEmpty ? widget.account : null;
    
    _selectedCategory = widget.category;
    _selectedCategoryId = widget.categoryId;
    _selectedDate = widget.date;
    _dateController = TextEditingController(
      text: DateFormat('dd/MM/yyyy').format(_selectedDate!),
    );

    // Load categories from API
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    setState(() {
      _isLoadingCategories = true;
    });

    try {
      final categories = await _transactionService.getCategories(
        type: widget.isIncome ? 'INCOME' : 'EXPENSE',
      );
      if (mounted) {
        setState(() {
          _categories = categories;
          _isLoadingCategories = false;
          
          // Check if selected category exists in loaded categories by ID or Name
          if (_selectedCategoryId != null) {
            final categoryExists = _categories.any((c) => c.id == _selectedCategoryId);
            if (!categoryExists && _selectedCategory != null) {
              // Try to find by name if ID doesn't match
              final matchByName = _categories.cast<Category?>().firstWhere(
                (c) => c?.name.toLowerCase() == _selectedCategory?.toLowerCase(),
                orElse: () => null,
              );
              if (matchByName != null) {
                _selectedCategoryId = matchByName.id;
                _selectedCategory = matchByName.name;
              }
            }
          } else if (_selectedCategory != null && _selectedCategory!.isNotEmpty) {
            final matchByName = _categories.cast<Category?>().firstWhere(
                (c) => c?.name.toLowerCase() == _selectedCategory?.toLowerCase(),
                orElse: () => null,
              );
            if (matchByName != null) {
              _selectedCategoryId = matchByName.id;
              _selectedCategory = matchByName.name;
            }
          }
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoadingCategories = false;
        });
      }
      print('❌ Error loading categories: $e');
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    _dateController.dispose();
    super.dispose();
  }

  IconData _getCategoryIcon(String? iconName, String categoryName) {
    // Priority 1: Use icon name if available
    if (iconName != null && iconName.isNotEmpty) {
      switch (iconName.toLowerCase()) {
        case 'restaurant':
        case 'food':
          return Icons.restaurant;
        case 'car':
        case 'transport':
        case 'directions_car':
          return Icons.directions_car;
        case 'shopping':
        case 'shopping_bag':
          return Icons.shopping_bag;
        case 'movie':
        case 'entertainment':
          return Icons.movie;
        case 'health':
        case 'medical':
          return Icons.medical_services;
        case 'education':
        case 'school':
          return Icons.school;
        case 'bill':
        case 'receipt':
          return Icons.receipt_long;
        case 'salary':
        case 'wallet':
          return Icons.account_balance_wallet;
        case 'gift':
          return Icons.card_giftcard;
        case 'investment':
          return Icons.trending_up;
        case 'business':
          return Icons.business;
      }
    }

    // Priority 2: Fallback to category name matching (Vietnamese)
    switch (categoryName.toLowerCase()) {
      case 'ăn uống': return Icons.restaurant;
      case 'di chuyển': return Icons.directions_car;
      case 'mua sắm': return Icons.shopping_bag;
      case 'giải trí': return Icons.movie;
      case 'y tế': return Icons.medical_services;
      case 'học tập': return Icons.school;
      case 'hóa đơn': return Icons.receipt_long;
      case 'lương': return Icons.account_balance_wallet;
      case 'thưởng': return Icons.card_giftcard;
      case 'đầu tư': return Icons.trending_up;
      case 'kinh doanh': return Icons.business;
      default: return Icons.category;
    }
  }

  Color _getCategoryColor(String? colorHex) {
    if (colorHex == null || colorHex.isEmpty) {
      return const Color(0xFF9CA3AF);
    }

    try {
      String hex = colorHex.replaceAll('#', '');
      if (hex.length == 6) {
        hex = 'FF$hex';
      }
      return Color(int.parse(hex, radix: 16));
    } catch (e) {
      return const Color(0xFF9CA3AF);
    }
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
            colorScheme: const ColorScheme.light(primary: Color(0xFFD91656)),
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
    final amountText = _amountController.text.trim().replaceAll(',', '').replaceAll('.', '');
    if (amountText.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng nhập số tiền'),
          backgroundColor: Color(0xFFE7000B),
        ),
      );
      return;
    }

    final amountValue = double.tryParse(amountText);
    if (amountValue == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Số tiền không hợp lệ'),
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

    // Category logic
    final categoryToSave = _selectedCategory ?? widget.category;
    final categoryIdToSave = _selectedCategoryId ?? widget.categoryId;

    widget.onSave(
      amount: amountValue,
      source: _selectedSource!,
      category: categoryToSave,
      categoryId: categoryIdToSave,
      description: _descriptionController.text.trim(),
      date: _selectedDate ?? DateTime.now(),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: Colors.white,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
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
                  'Cập nhật thông tin giao dịch. Phản hồi của bạn giúp AI học và cải thiện độ chính xác.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 14,
                    color: AppColors.textSecondary,
                  ),
                ),
                const SizedBox(height: 24),

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
                    return DropdownMenuItem(value: source, child: Text(source));
                  }).toList(),
                  onChanged: (value) {
                    setState(() {
                      _selectedSource = value;
                    });
                  },
                ),
                const SizedBox(height: 16),

                const Text(
                  'Danh mục',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 8),
                _isLoadingCategories
                    ? const Center(child: CircularProgressIndicator())
                    : DropdownButtonFormField<String>(
                        value: _categories.any((c) => c.name == _selectedCategory) ? _selectedCategory : null,
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
                        items: _categories.map((category) {
                          return DropdownMenuItem(
                            value: category.name,
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Icon(
                                  _getCategoryIcon(category.icon, category.name),
                                  size: 20,
                                  color: _getCategoryColor(category.color),
                                ),
                                const SizedBox(width: 12),
                                Text(category.name),
                              ],
                            ),
                          );
                        }).toList(),
                        onChanged: (value) {
                          if (value != null) {
                            final category = _categories.firstWhere(
                              (c) => c.name == value,
                              orElse: () => _categories.first,
                            );
                            setState(() {
                              _selectedCategory = value;
                              _selectedCategoryId = category.id;
                            });
                          }
                        },
                      ),
                const SizedBox(height: 16),

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
                    hintText: 'Mô tả giao dịch',
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
                    hintText: 'dd/mm/yyyy',
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
