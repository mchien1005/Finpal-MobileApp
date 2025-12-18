import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../../data/models/savings_goal_model.dart';
import '../../../../data/services/savings_goal_service.dart';

class EditGoalDialog extends StatefulWidget {
  final int goalId;
  final String goalName;
  final double targetAmount;
  final double savedAmount;
  final DateTime deadline;
  final double monthlyContribution;

  const EditGoalDialog({
    super.key,
    required this.goalId,
    required this.goalName,
    required this.targetAmount,
    required this.savedAmount,
    required this.deadline,
    required this.monthlyContribution,
  });

  @override
  State<EditGoalDialog> createState() => _EditGoalDialogState();
}

class _EditGoalDialogState extends State<EditGoalDialog> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _targetAmountController;
  late TextEditingController _savedAmountController;
  late TextEditingController _monthlyContributionController;
  late TextEditingController _dateController;
  late DateTime _selectedDate;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.goalName);
    _targetAmountController = TextEditingController(
      text: widget.targetAmount.toInt().toString(),
    );
    _savedAmountController = TextEditingController(
      text: widget.savedAmount.toInt().toString(),
    );
    _monthlyContributionController = TextEditingController(
      text: widget.monthlyContribution.toInt().toString(),
    );
    _selectedDate = widget.deadline;
    _dateController = TextEditingController(text: _formatDate(_selectedDate));
  }

  @override
  void dispose() {
    _nameController.dispose();
    _targetAmountController.dispose();
    _savedAmountController.dispose();
    _monthlyContributionController.dispose();
    _dateController.dispose();
    super.dispose();
  }

  String _formatDate(DateTime date) {
    return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
  }

  Future<void> _selectDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime.now(),
      lastDate: DateTime(2100),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.light(
              primary: Color(0xFFD7006E),
              onPrimary: Colors.white,
              surface: Colors.white,
              onSurface: Color(0xFF0A0A0A),
            ),
          ),
          child: child!,
        );
      },
    );
    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
        _dateController.text = _formatDate(picked);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      child: Stack(
        clipBehavior: Clip.none,
        children: [
          Container(
            width: 396,
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(10),
              border: Border.all(
                color: Colors.black.withOpacity(0.1),
                width: 1.145,
              ),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 15,
                  offset: const Offset(0, 10),
                ),
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 6,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Form(
              key: _formKey,
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Header
                    const Text(
                      'Chỉnh sửa mục tiêu',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Cập nhật thông tin mục tiêu tiết kiệm của bạn',
                      style: TextStyle(
                        fontSize: 14,
                        color: Color(0xFF717182),
                        fontFamily: 'Arimo',
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 24),

                    // Goal Name
                    const Text(
                      'Tên mục tiêu',
                      style: TextStyle(
                        fontSize: 14,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 8),
                    TextFormField(
                      controller: _nameController,
                      decoration: InputDecoration(
                        hintText: 'Nhập tên mục tiêu',
                        hintStyle: const TextStyle(
                          color: Color(0xFF717182),
                          fontSize: 16,
                          fontFamily: 'Arimo',
                        ),
                        filled: true,
                        fillColor: const Color(0xFFF3F3F5),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide.none,
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 8,
                        ),
                      ),
                      style: const TextStyle(
                        fontSize: 16,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Vui lòng nhập tên mục tiêu';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // Target Amount
                    const Text(
                      'Số tiền mục tiêu (VNĐ)',
                      style: TextStyle(
                        fontSize: 14,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 8),
                    TextFormField(
                      controller: _targetAmountController,
                      keyboardType: TextInputType.number,
                      inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                      decoration: InputDecoration(
                        hintText: 'Nhập số tiền',
                        hintStyle: const TextStyle(
                          color: Color(0xFF717182),
                          fontSize: 16,
                          fontFamily: 'Arimo',
                        ),
                        filled: true,
                        fillColor: const Color(0xFFF3F3F5),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide.none,
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 8,
                        ),
                      ),
                      style: const TextStyle(
                        fontSize: 16,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Vui lòng nhập số tiền mục tiêu';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // Saved Amount - Display only
                    const Text(
                      'Số tiền đã tiết kiệm (VNĐ)',
                      style: TextStyle(
                        fontSize: 14,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 8),
                    TextFormField(
                      controller: _savedAmountController,
                      enabled: false, // Read-only - cannot edit
                      decoration: InputDecoration(
                        hintText: 'Dùng nút "Góp tiền" để thêm tiền',
                        hintStyle: const TextStyle(
                          color: Color(0xFF717182),
                          fontSize: 14,
                          fontFamily: 'Arimo',
                        ),
                        filled: true,
                        fillColor: const Color(0xFFE5E7EB), // Gray background
                        disabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide.none,
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 8,
                        ),
                        suffixIcon: const Tooltip(
                          message:
                              'Không thể sửa trực tiếp.\nDùng nút "Góp tiền" bên dưới để thêm tiền.',
                          child: Icon(
                            Icons.info_outline,
                            size: 20,
                            color: Color(0xFF6B7280),
                          ),
                        ),
                      ),
                      style: const TextStyle(
                        fontSize: 16,
                        color: Color(0xFF6B7280), // Lighter color for disabled
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Deadline
                    const Text(
                      'Thời hạn',
                      style: TextStyle(
                        fontSize: 14,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 8),
                    TextFormField(
                      controller: _dateController,
                      readOnly: true,
                      onTap: () => _selectDate(context),
                      decoration: InputDecoration(
                        hintText: 'Chọn thời hạn',
                        hintStyle: const TextStyle(
                          color: Color(0xFF717182),
                          fontSize: 16,
                          fontFamily: 'Arimo',
                        ),
                        filled: true,
                        fillColor: const Color(0xFFF3F3F5),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide.none,
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 8,
                        ),
                        suffixIcon: const Icon(
                          Icons.calendar_today,
                          size: 20,
                          color: Color(0xFF0A0A0A),
                        ),
                      ),
                      style: const TextStyle(
                        fontSize: 16,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Vui lòng chọn thời hạn';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // Monthly Contribution - Auto-calculated, display only
                    const Text(
                      'Góp mỗi tháng (VNĐ)',
                      style: TextStyle(
                        fontSize: 14,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 8),
                    TextFormField(
                      controller: _monthlyContributionController,
                      enabled: false, // Read-only - auto calculated
                      decoration: InputDecoration(
                        hintText: 'Tự động tính dựa trên thời hạn',
                        hintStyle: const TextStyle(
                          color: Color(0xFF717182),
                          fontSize: 14,
                          fontFamily: 'Arimo',
                        ),
                        filled: true,
                        fillColor: const Color(0xFFE5E7EB), // Gray for disabled
                        disabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide.none,
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 8,
                        ),
                        suffixIcon: const Tooltip(
                          message:
                              'Số tiền gợi ý mỗi tháng để đạt mục tiêu.\nTự động tính từ (Số tiền còn lại ÷ Số tháng còn lại)',
                          child: Icon(
                            Icons.info_outline,
                            size: 20,
                            color: Color(0xFF6B7280),
                          ),
                        ),
                      ),
                      style: const TextStyle(
                        fontSize: 16,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 24),

                    // Action Buttons
                    Row(
                      children: [
                        Expanded(
                          child: OutlinedButton(
                            onPressed: () => Navigator.of(context).pop(),
                            style: OutlinedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 9),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8),
                              ),
                              side: BorderSide(
                                color: Colors.black.withOpacity(0.1),
                                width: 1.145,
                              ),
                            ),
                            child: const Text(
                              'Hủy',
                              style: TextStyle(
                                fontSize: 14,
                                color: Color(0xFF0A0A0A),
                                fontFamily: 'Arimo',
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: ElevatedButton(
                            onPressed: _isLoading ? null : _updateGoal,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFFD7006E),
                              padding: const EdgeInsets.symmetric(vertical: 8),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8),
                              ),
                            ),
                            child: _isLoading
                                ? const SizedBox(
                                    height: 20,
                                    width: 20,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      valueColor: AlwaysStoppedAnimation<Color>(
                                        Colors.white,
                                      ),
                                    ),
                                  )
                                : const Text(
                                    'Cập nhật',
                                    style: TextStyle(
                                      fontSize: 14,
                                      color: Colors.white,
                                      fontFamily: 'Arimo',
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
          Positioned(
            right: 8,
            top: 8,
            child: IconButton(
              icon: const Icon(Icons.close, size: 20),
              onPressed: () => Navigator.of(context).pop(),
              padding: const EdgeInsets.all(8),
              constraints: const BoxConstraints(minWidth: 36, minHeight: 36),
              color: Colors.black.withOpacity(0.7),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _updateGoal() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      final request = SavingsGoalRequest(
        name: _nameController.text,
        targetAmount: double.parse(_targetAmountController.text),
        deadline:
            '${_selectedDate.year}-${_selectedDate.month.toString().padLeft(2, '0')}-${_selectedDate.day.toString().padLeft(2, '0')}',
      );

      final response = await SavingsGoalService.updateSavingsGoal(
        widget.goalId,
        request,
      );

      if (mounted) {
        Navigator.of(context).pop(response);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Lỗi cập nhật mục tiêu: ${e.toString()}'),
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
}
