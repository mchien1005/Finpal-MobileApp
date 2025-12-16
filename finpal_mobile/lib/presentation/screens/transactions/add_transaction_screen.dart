import 'package:flutter/material.dart';
import 'package:flutter_sms_inbox/flutter_sms_inbox.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/widgets/custom_bottom_nav_bar.dart';
import '../../../core/utils/app_bar_with_drawer.dart';
import '../../../core/utils/bottom_nav_helper.dart';
import '../../../data/services/transaction_service.dart';
import '../../../data/services/sms_service.dart';
import '../../../data/models/category.dart';
import 'package:flutter_svg/flutter_svg.dart';

class AddTransactionScreen extends StatefulWidget {
  const AddTransactionScreen({super.key});

  @override
  State<AddTransactionScreen> createState() => _AddTransactionScreenState();
}

class _AddTransactionScreenState extends State<AddTransactionScreen> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _transactionService = TransactionService();
  final _smsService = SmsService();
  final SmsQuery _smsQuery = SmsQuery();
  String _transactionType = 'expense'; // 'expense' or 'income'
  String? _selectedSource;
  Category? _selectedCategory;
  DateTime? _selectedDate;
  bool _isLoading = false;
  bool _isSmsScanning = false;
  List<Category> _categories = [];
  bool _isCategoriesLoading = true;

  @override
  void initState() {
    super.initState();
    _loadCategories();
  }

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

  /// Request SMS permission
  Future<bool> _requestSmsPermission() async {
    var status = await Permission.sms.status;
    if (status.isDenied) {
      status = await Permission.sms.request();
    }
    return status.isGranted;
  }

  /// Handle SMS scanning
  Future<void> _handleScanSms() async {
    // Check permission
    final hasPermission = await _requestSmsPermission();
    if (!hasPermission) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Cần quyền đọc SMS để quét tin nhắn ngân hàng'),
            backgroundColor: Colors.orange,
          ),
        );
      }
      return;
    }

    setState(() {
      _isSmsScanning = true;
    });

    try {
      // Get SMS messages from the last 30 days
      final messages = await _smsQuery.querySms(
        kinds: [SmsQueryKind.inbox],
        count: 100, // Limit to last 100 messages
      );

      print('📱 Total SMS found: ${messages.length}');

      // Filter bank SMS messages (check both sender and message body for bank keywords)
      // Use a Set to deduplicate by amount + date pattern
      final Set<String> seenTransactions = {};
      final bankMessages = messages.where((sms) {
        final sender = sms.sender ?? sms.address ?? '';
        final body = sms.body ?? '';

        // Check if this is a bank SMS
        if (!_smsService.isBankSms(sender, messageBody: body)) {
          return false;
        }

        // Extract amount from message using regex (e.g., "+10,000,000 VND" or "-500,000 VND")
        final amountRegex = RegExp(
          r'[+-]?[\d,\.]+\s*(?:VND|đ|dong)',
          caseSensitive: false,
        );
        final amountMatch = amountRegex.firstMatch(body);
        final amount = amountMatch?.group(0) ?? '';

        // Extract date pattern (e.g., "13:11 25/11/2025" or "25/11/2025")
        final dateRegex = RegExp(
          r'\d{1,2}[:/]\d{1,2}(?:[:/]\d{2,4})?(?:\s+\d{1,2}/\d{1,2}/\d{2,4})?',
        );
        final dateMatch = dateRegex.firstMatch(body);
        final dateStr = dateMatch?.group(0) ?? '';

        // Create unique key from amount + date (to catch same transaction with different ref numbers)
        final transactionKey = '${amount}_${dateStr}';

        if (seenTransactions.contains(transactionKey)) {
          print('⏭️ Skipping duplicate transaction: $amount at $dateStr');
          return false;
        }
        seenTransactions.add(transactionKey);
        print('✅ Accepting SMS: $amount at $dateStr');
        return true;
      }).toList();

      print('🏦 Bank SMS found (after dedup): ${bankMessages.length}');

      if (bankMessages.isEmpty) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Không tìm thấy tin nhắn ngân hàng nào'),
              backgroundColor: Colors.orange,
            ),
          );
        }
        return;
      }

      // Show confirmation dialog
      if (mounted) {
        final shouldProceed = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Xác nhận quét SMS'),
            content: Text(
              'Tìm thấy ${bankMessages.length} tin nhắn từ ngân hàng.\n\nBạn có muốn quét và thêm giao dịch tự động?',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('Hủy'),
              ),
              ElevatedButton(
                onPressed: () => Navigator.pop(context, true),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFFD7006E),
                ),
                child: const Text(
                  'Quét ngay',
                  style: TextStyle(color: Colors.white),
                ),
              ),
            ],
          ),
        );

        if (shouldProceed != true) {
          return;
        }
      }

      // Convert to list of maps for processing
      // Use bank name from message body if sender is a phone number
      final smsData = bankMessages.map((sms) {
        final rawSender = sms.sender ?? sms.address ?? '';
        final messageBody = sms.body ?? '';
        // Extract bank name from message if sender is just a phone number
        final sender = _smsService.getBankNameFromMessage(
          rawSender,
          messageBody,
        );
        return {
          'sender': sender,
          'message': messageBody,
          'date': sms.date ?? DateTime.now(),
        };
      }).toList();

      // Process SMS messages
      final result = await _smsService.scanAndProcessSms(smsData);

      // Show result
      if (mounted) {
        _showScanResultDialog(result);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Lỗi quét SMS: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isSmsScanning = false;
        });
      }
    }
  }

  /// Show scan result dialog
  void _showScanResultDialog(SmsScanResult result) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Row(
          children: [
            Icon(Icons.check_circle, color: Colors.green),
            SizedBox(width: 8),
            Text('Kết quả quét SMS'),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildResultRow('Tổng SMS đã quét:', '${result.totalSmsScanned}'),
            _buildResultRow('Giao dịch hợp lệ:', '${result.validTransactions}'),
            _buildResultRow(
              'Đã thêm thành công:',
              '${result.addedTransactions}',
              valueColor: Colors.green,
            ),
            if (result.failedTransactions > 0)
              _buildResultRow(
                'Thất bại:',
                '${result.failedTransactions}',
                valueColor: Colors.red,
              ),
            if (result.errors.isNotEmpty) ...[
              const SizedBox(height: 12),
              const Text(
                'Chi tiết lỗi:',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              ...result.errors
                  .take(3)
                  .map(
                    (error) => Text(
                      '• $error',
                      style: const TextStyle(fontSize: 12, color: Colors.red),
                    ),
                  ),
            ],
          ],
        ),
        actions: [
          ElevatedButton(
            onPressed: () => Navigator.pop(context),
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFD7006E),
            ),
            child: const Text('Đóng', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  Widget _buildResultRow(String label, String value, {Color? valueColor}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label),
          Text(
            value,
            style: TextStyle(fontWeight: FontWeight.bold, color: valueColor),
          ),
        ],
      ),
    );
  }

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
      final type = _transactionType == 'expense' ? 'EXPENSE' : 'INCOME';

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
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Thêm giao dịch thành công!'),
            backgroundColor: Colors.green,
          ),
        );

        // Clear form
        _amountController.clear();
        _descriptionController.clear();
        setState(() {
          _selectedSource = null;
          _selectedCategory = null;
          _selectedDate = null;
        });
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
    return AppBarWithDrawer.scrollable(
      context,
      userName: 'Nguyễn Văn A',
      notificationCount: 3,
      backgroundColor: AppColors.background,
      body: Column(
        children: [
          // SMS Auto-fill Banner
          Container(
            margin: const EdgeInsets.all(16),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              border: Border.all(color: const Color(0xFFD7006E), width: 2),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Tự động quét SMS',
                        style: TextStyle(
                          color: Color(0xFFD7006E),
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Ghi nhận giao dịch từ tin nhắn ngân hàng',
                        style: TextStyle(
                          color: const Color(0xFFD7006E).withOpacity(0.75),
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                _isSmsScanning
                    ? const SizedBox(
                        width: 80,
                        height: 36,
                        child: Center(
                          child: SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Color(0xFFD7006E),
                            ),
                          ),
                        ),
                      )
                    : ElevatedButton(
                        onPressed: _handleScanSms,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFFD7006E),
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 8,
                          ),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                          ),
                        ),
                        child: Row(
                          children: [
                            SvgPicture.asset(
                              'assets/icons/scanner.svg',
                              width: 16,
                              height: 16,
                            ),
                            const SizedBox(width: 4),
                            const Text(
                              'Quét ngay',
                              style: TextStyle(fontSize: 12),
                            ),
                          ],
                        ),
                      ),
              ],
            ),
          ),

          // Form Container
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            padding: const EdgeInsets.all(24),
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
                  const Text(
                    'Loại giao dịch',
                    style: TextStyle(
                      fontSize: 14,
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
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
                  const Text(
                    'Số tiền (VNĐ)',
                    style: TextStyle(
                      fontSize: 14,
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextFormField(
                    controller: _amountController,
                    keyboardType: TextInputType.number,
                    style: const TextStyle(fontSize: 16),
                    decoration: InputDecoration(
                      hintText: '0',
                      suffixText: 'đ',
                      filled: true,
                      fillColor: AppColors.inputBackground,
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 12,
                      ),
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
                  const Text(
                    'Nguồn giao dịch',
                    style: TextStyle(
                      fontSize: 14,
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<String>(
                    value: _selectedSource,
                    hint: const Text('Chọn danh mục'),
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.inputBackground,
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 12,
                      ),
                    ),
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
                  const Text(
                    'Danh mục',
                    style: TextStyle(
                      fontSize: 14,
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 8),
                  _isCategoriesLoading
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
                          value: _selectedCategory,
                          hint: const Text('Chọn danh mục'),
                          decoration: InputDecoration(
                            filled: true,
                            fillColor: AppColors.inputBackground,
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(8),
                              borderSide: BorderSide.none,
                            ),
                            contentPadding: const EdgeInsets.symmetric(
                              horizontal: 16,
                              vertical: 12,
                            ),
                          ),
                          items: _categories
                              .map(
                                (category) => DropdownMenuItem<Category>(
                                  value: category,
                                  child: Text(category.name),
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
                  const Text(
                    'Mô tả',
                    style: TextStyle(
                      fontSize: 14,
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextFormField(
                    controller: _descriptionController,
                    maxLines: 3,
                    decoration: InputDecoration(
                      hintText: 'Ví dụ: Mua bánh mì sáng, đổ xăng...',
                      filled: true,
                      fillColor: AppColors.inputBackground,
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 12,
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Date Field
                  const Text(
                    'Ngày giao dịch',
                    style: TextStyle(
                      fontSize: 14,
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 8),
                  InkWell(
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
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 12,
                      ),
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
                  ),
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
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Colors.white,
              border: Border.all(
                color: Colors.black.withOpacity(0.1),
                width: 1.275,
              ),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header with title and badge
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
                        border: Border.all(
                          color: Colors.transparent,
                          width: 1.275,
                        ),
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

                // SMS List
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
          ),
          const SizedBox(height: 100),
        ],
      ),
      bottomNavigationBar: CustomBottomNavBar(
        currentIndex: 2,
        onTap: (index) {
          // TODO: Navigate to different screens based on index
          if (index == 2) return; // Already on add transaction screen
          // Handle navigation to other screens
        },
      ),
    );
  }

  Widget _buildTypeButton(String label, String type) {
    final isSelected = _transactionType == type;
    return InkWell(
      onTap: () {
        setState(() {
          _transactionType = type;
          _selectedCategory = null; // Reset selected category
        });
        _loadCategories(); // Reload categories for new type
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
          // Bank name and amount
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
          // Description
          Text(
            description,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w400,
              color: Color(0xFF4A5565),
            ),
          ),
          const SizedBox(height: 3.983),
          // Date time
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
