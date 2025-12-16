import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ContributeGoalDialog extends StatefulWidget {
  final String goalName;
  final double currentAmount;
  final double targetAmount;
  final double monthlyContribution;

  const ContributeGoalDialog({
    super.key,
    required this.goalName,
    required this.currentAmount,
    required this.targetAmount,
    required this.monthlyContribution,
  });

  @override
  State<ContributeGoalDialog> createState() => _ContributeGoalDialogState();
}

class _ContributeGoalDialogState extends State<ContributeGoalDialog> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _noteController = TextEditingController();
  String _selectedQuickOption = '';

  @override
  void dispose() {
    _amountController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  double get _contributionAmount {
    final text = _amountController.text.replaceAll(',', '').replaceAll('.', '');
    return double.tryParse(text) ?? 0;
  }

  double get _newAmount => widget.currentAmount + _contributionAmount;
  double get _remainingAmount => widget.targetAmount - widget.currentAmount;
  double get _progress =>
      (_newAmount / widget.targetAmount * 100).clamp(0, 100);

  void _selectQuickOption(String option) {
    setState(() {
      _selectedQuickOption = option;
      if (option == 'monthly') {
        _amountController.text = widget.monthlyContribution.toInt().toString();
      } else if (option == 'remaining') {
        _amountController.text = _remainingAmount.toInt().toString();
      }
    });
  }

  String _formatCurrency(double amount) {
    if (amount >= 1000000) {
      final millions = amount / 1000000;
      final remaining = (amount % 1000000) ~/ 1000;
      if (remaining == 0) {
        return '${millions.toInt()}.000.000đ';
      }
      return '${millions.toInt()}.${remaining.toString().padLeft(3, '0')}.000đ';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toInt()}.000đ';
    }
    return '${amount.toInt()}đ';
  }

  @override
  Widget build(BuildContext context) {
    final showSummary = _contributionAmount > 0;

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
                      'Góp tiền vào mục tiêu',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      widget.goalName,
                      style: const TextStyle(
                        fontSize: 14,
                        color: Color(0xFF717182),
                        fontFamily: 'Arimo',
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 24),

                    // Quick Select
                    const Text(
                      'Chọn nhanh',
                      style: TextStyle(
                        fontSize: 14,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        Expanded(
                          child: InkWell(
                            onTap: () => _selectQuickOption('monthly'),
                            child: Container(
                              padding: const EdgeInsets.symmetric(vertical: 12),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(8),
                                border: Border.all(
                                  color: _selectedQuickOption == 'monthly'
                                      ? const Color(0xFF155DFC)
                                      : Colors.black.withOpacity(0.1),
                                  width: 1.145,
                                ),
                              ),
                              child: Column(
                                children: [
                                  const Text(
                                    'Góp theo tháng',
                                    style: TextStyle(
                                      fontSize: 14,
                                      color: Color(0xFF0A0A0A),
                                      fontFamily: 'Arimo',
                                    ),
                                    textAlign: TextAlign.center,
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    _formatCurrency(widget.monthlyContribution),
                                    style: const TextStyle(
                                      fontSize: 12,
                                      color: Color(0xFF155DFC),
                                      fontFamily: 'Arimo',
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: InkWell(
                            onTap: () => _selectQuickOption('remaining'),
                            child: Container(
                              padding: const EdgeInsets.symmetric(vertical: 12),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(8),
                                border: Border.all(
                                  color: _selectedQuickOption == 'remaining'
                                      ? const Color(0xFF00A63E)
                                      : Colors.black.withOpacity(0.1),
                                  width: 1.145,
                                ),
                              ),
                              child: Column(
                                children: [
                                  const Text(
                                    'Góp hết còn thiếu',
                                    style: TextStyle(
                                      fontSize: 14,
                                      color: Color(0xFF0A0A0A),
                                      fontFamily: 'Arimo',
                                    ),
                                    textAlign: TextAlign.center,
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    _formatCurrency(_remainingAmount),
                                    style: const TextStyle(
                                      fontSize: 12,
                                      color: Color(0xFF00A63E),
                                      fontFamily: 'Arimo',
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),

                    // Amount Input
                    const Text(
                      'Số tiền góp (VNĐ)',
                      style: TextStyle(
                        fontSize: 14,
                        color: Color(0xFF0A0A0A),
                        fontFamily: 'Arimo',
                      ),
                    ),
                    const SizedBox(height: 8),
                    TextFormField(
                      controller: _amountController,
                      keyboardType: TextInputType.number,
                      inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                      onChanged: (value) {
                        setState(() {
                          // Clear quick option when manually typing
                          if (_selectedQuickOption.isNotEmpty) {
                            _selectedQuickOption = '';
                          }
                        });
                      },
                      decoration: InputDecoration(
                        hintText: '0',
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
                        if (value == null || value.isEmpty || value == '0') {
                          return 'Vui lòng nhập số tiền góp';
                        }
                        return null;
                      },
                    ),

                    if (showSummary) ...[
                      const SizedBox(height: 16),
                      Container(
                        padding: const EdgeInsets.all(13),
                        decoration: BoxDecoration(
                          color: const Color(0xFFF0FDF4),
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(
                            color: const Color(0xFFB9F8CF),
                            width: 1.322,
                          ),
                        ),
                        child: Column(
                          children: [
                            _buildSummaryRow(
                              'Hiện tại:',
                              _formatCurrency(widget.currentAmount),
                              const Color(0xFF4A5565),
                              const Color(0xFF0A0A0A),
                            ),
                            const SizedBox(height: 8),
                            _buildSummaryRow(
                              'Góp thêm:',
                              '+${_formatCurrency(_contributionAmount)}',
                              const Color(0xFF4A5565),
                              const Color(0xFF00A63E),
                            ),
                            const SizedBox(height: 8),
                            const Divider(
                              color: Color(0xFF7BF1A8),
                              thickness: 1.322,
                            ),
                            const SizedBox(height: 8),
                            _buildSummaryRow(
                              'Sau khi góp:',
                              _formatCurrency(_newAmount),
                              const Color(0xFF4A5565),
                              const Color(0xFF008236),
                              boldValue: true,
                            ),
                            const SizedBox(height: 8),
                            _buildSummaryRow(
                              'Tiến độ:',
                              '${_progress.toStringAsFixed(1)}%',
                              const Color(0xFF4A5565),
                              const Color(0xFF0A0A0A),
                            ),
                          ],
                        ),
                      ),
                    ],

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
                          child: Container(
                            decoration: BoxDecoration(
                              gradient: const LinearGradient(
                                colors: [Color(0xFF00A63E), Color(0xFF009966)],
                                begin: Alignment.centerLeft,
                                end: Alignment.centerRight,
                              ),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: ElevatedButton(
                              onPressed: () {
                                if (_formKey.currentState!.validate()) {
                                  // Handle contribution
                                  Navigator.of(context).pop({
                                    'amount': _contributionAmount,
                                    'note': _noteController.text,
                                  });
                                }
                              },
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.transparent,
                                shadowColor: Colors.transparent,
                                padding: const EdgeInsets.symmetric(
                                  vertical: 8,
                                ),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(8),
                                ),
                              ),
                              child: const Text(
                                'Xác nhận góp tiền',
                                style: TextStyle(
                                  fontSize: 14,
                                  color: Colors.white,
                                  fontFamily: 'Arimo',
                                ),
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

  Widget _buildSummaryRow(
    String label,
    String value,
    Color labelColor,
    Color valueColor, {
    bool boldValue = false,
  }) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: TextStyle(
            fontSize: boldValue ? 16 : 14,
            color: labelColor,
            fontFamily: 'Arimo',
          ),
        ),
        Text(
          value,
          style: TextStyle(
            fontSize: boldValue ? 16 : 14,
            color: valueColor,
            fontFamily: 'Arimo',
            fontWeight: boldValue ? FontWeight.w500 : FontWeight.normal,
          ),
        ),
      ],
    );
  }
}
