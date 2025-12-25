import 'package:flutter/material.dart';
import 'package:flutter_sms_inbox/flutter_sms_inbox.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../data/services/sms_service.dart';
import '../../data/services/transaction_deduplicator.dart';

/// A reusable widget that provides SMS scanning functionality
/// to auto-import bank transactions from SMS messages.
/// Includes deduplication to prevent duplicate transactions.
class SmsScannerBanner extends StatefulWidget {
  const SmsScannerBanner({super.key});

  @override
  State<SmsScannerBanner> createState() => _SmsScannerBannerState();
}

class _SmsScannerBannerState extends State<SmsScannerBanner> {
  final SmsService _smsService = SmsService();
  final TransactionDeduplicator _deduplicator = TransactionDeduplicator();
  final SmsQuery _smsQuery = SmsQuery();
  bool _isSmsScanning = false;

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
              'Tìm thấy ${bankMessages.length} tin nhắn từ ngân hàng.\n\n'
              'Bạn có muốn quét và thêm giao dịch tự động?\n\n'
              '(Giao dịch trùng lặp sẽ tự động được bỏ qua)',
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

      // Process with deduplication
      int addedCount = 0;
      int duplicateCount = 0;
      int failedCount = 0;
      List<String> errors = [];

      for (final sms in bankMessages) {
        final rawSender = sms.sender ?? sms.address ?? '';
        final messageBody = sms.body ?? '';
        final sender = _smsService.getBankNameFromMessage(
          rawSender,
          messageBody,
        );
        final date = sms.date ?? DateTime.now();

        // Check for duplicate using deduplicator
        final amount = _deduplicator.parseAmountFromMessage(messageBody);
        if (amount != null) {
          final isNew = await _deduplicator.checkAndMark(
            amount: amount,
            date: date,
            bankName: sender,
          );

          if (!isNew) {
            duplicateCount++;
            continue; // Skip duplicate
          }
        }

        // Process the transaction
        try {
          final result = await _smsService.scanAndProcessSms([
            {'sender': sender, 'message': messageBody, 'date': date},
          ]);
          addedCount += result.addedTransactions;
          failedCount += result.failedTransactions;
          errors.addAll(result.errors);
        } catch (e) {
          failedCount++;
          errors.add('Lỗi: ${e.toString()}');
        }
      }

      // Show result
      if (mounted) {
        _showScanResultDialog(
          totalScanned: bankMessages.length,
          added: addedCount,
          duplicates: duplicateCount,
          failed: failedCount,
          errors: errors,
        );
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

  /// Show scan result dialog with deduplication info
  void _showScanResultDialog({
    required int totalScanned,
    required int added,
    required int duplicates,
    required int failed,
    required List<String> errors,
  }) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Row(
          children: [
            Icon(Icons.check_circle, color: Colors.green),
            SizedBox(width: 8),
            Expanded(child: Text('Kết quả quét SMS')),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildResultRow('Tổng SMS đã quét:', '$totalScanned'),
            _buildResultRow(
              'Đã thêm thành công:',
              '$added',
              valueColor: Colors.green,
            ),
            if (duplicates > 0)
              _buildResultRow(
                'Bỏ qua (trùng lặp):',
                '$duplicates',
                valueColor: Colors.orange,
                icon: Icons.content_copy,
              ),
            if (failed > 0)
              _buildResultRow('Thất bại:', '$failed', valueColor: Colors.red),
            if (errors.isNotEmpty) ...[
              const SizedBox(height: 12),
              const Text(
                'Chi tiết lỗi:',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              ...errors
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

  Widget _buildResultRow(
    String label,
    String value, {
    Color? valueColor,
    IconData? icon,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              if (icon != null) ...[
                Icon(icon, size: 16, color: valueColor),
                const SizedBox(width: 4),
              ],
              Text(label),
            ],
          ),
          Text(
            value,
            style: TextStyle(fontWeight: FontWeight.bold, color: valueColor),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
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
                    color: const Color(0xFFD7006E).withValues(alpha: 0.75),
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
                      const Text('Quét ngay', style: TextStyle(fontSize: 12)),
                    ],
                  ),
                ),
        ],
      ),
    );
  }
}

// Alias for backwards compatibility
typedef TransactionScannerBanner = SmsScannerBanner;
