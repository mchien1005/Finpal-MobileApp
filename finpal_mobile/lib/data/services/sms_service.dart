import 'dart:convert';
import 'package:http/http.dart' as http;
import 'auth_service.dart';

/// Model for parsed SMS transaction data
class ParsedSmsTransaction {
  final String? type;
  final double? amount;
  final String? bankName;
  final String? accountNumber;
  final String? transactionDate;
  final String? description;
  final String? rawMessage;
  final bool isValid;

  ParsedSmsTransaction({
    this.type,
    this.amount,
    this.bankName,
    this.accountNumber,
    this.transactionDate,
    this.description,
    this.rawMessage,
    this.isValid = false,
  });

  factory ParsedSmsTransaction.fromJson(Map<String, dynamic> json) {
    // Parse amount from various possible formats
    double? parsedAmount;
    if (json['amount'] != null) {
      if (json['amount'] is int) {
        parsedAmount = (json['amount'] as int).toDouble();
      } else if (json['amount'] is double) {
        parsedAmount = json['amount'] as double;
      } else if (json['amount'] is String) {
        parsedAmount = double.tryParse(
          json['amount'].toString().replaceAll(',', ''),
        );
      }
    }

    // Consider valid if we have amount and type
    final hasValidData = parsedAmount != null && parsedAmount > 0;

    return ParsedSmsTransaction(
      type: json['type'] ?? json['transactionType'],
      amount: parsedAmount,
      bankName: json['bankName'] ?? json['bank'],
      accountNumber: json['accountNumber'],
      transactionDate: json['transactionDate'] ?? json['date'],
      description: json['description'] ?? json['content'] ?? json['note'],
      rawMessage: json['rawMessage'] ?? json['smsContent'],
      isValid: json['isValid'] ?? hasValidData,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'type': type,
      'amount': amount,
      'bankName': bankName,
      'accountNumber': accountNumber,
      'transactionDate': transactionDate,
      'description': description,
      'rawMessage': rawMessage,
      'isValid': isValid,
    };
  }
}

/// Result of SMS scan operation
class SmsScanResult {
  final int totalSmsScanned;
  final int validTransactions;
  final int addedTransactions;
  final int failedTransactions;
  final List<ParsedSmsTransaction> transactions;
  final List<String> errors;

  SmsScanResult({
    required this.totalSmsScanned,
    required this.validTransactions,
    required this.addedTransactions,
    required this.failedTransactions,
    required this.transactions,
    required this.errors,
  });
}

class SmsService {
  final String _baseUrl = 'http://175.41.150.228:8080/api';
  final AuthService _authService = AuthService();

  /// List of known bank sender addresses
  static const List<String> bankSenders = [
    'Vietcombank',
    'VCB',
    'Techcombank',
    'TCB',
    'ACB',
    'BIDV',
    'Agribank',
    'VPBank',
    'MBBank',
    'MB',
    'Sacombank',
    'TPBank',
    'OCB',
    'VIB',
    'SHB',
    'HDBank',
    'SeABank',
    'MSB',
    'Eximbank',
    'LienVietPostBank',
    'NCB',
    'BacABank',
    'HSBC',
    'Citibank',
    'StandardChartered',
    'ANZ',
    'DBS',
    'UOB',
    'CIMB',
    'PublicBank',
    'VietinBank',
    'VietABank',
    'NamABank',
    'KienLongBank',
    'ABBank',
    'PVcomBank',
    'BaoVietBank',
    'VietCapitalBank',
    'Momo',
    'ZaloPay',
    'VNPay',
    'ShopeePay',
  ];

  /// Check if an SMS sender is from a bank
  /// Also checks message body for bank keywords (useful for testing)
  bool isBankSms(String sender, {String? messageBody}) {
    final senderLower = sender.toLowerCase();
    final bodyLower = (messageBody ?? '').toLowerCase();

    // Check sender name/address
    final isSenderBank = bankSenders.any(
      (bank) => senderLower.contains(bank.toLowerCase()),
    );

    // Also check if message body contains bank name pattern (for testing with regular phone numbers)
    // Look for patterns like "BIDV:", "VCB:", or message starting with bank name
    final isBodyFromBank = bankSenders.any((bank) {
      final bankLower = bank.toLowerCase();
      return bodyLower.startsWith(bankLower) ||
          bodyLower.contains('$bankLower:') ||
          bodyLower.contains('$bankLower ') ||
          bodyLower.contains('$bankLower\n');
    });

    print(
      '🔍 isBankSms check - Sender: $sender, isSenderBank: $isSenderBank, isBodyFromBank: $isBodyFromBank',
    );
    return isSenderBank || isBodyFromBank;
  }

  /// Extract bank name from message body or sender
  /// Returns the bank name if found, otherwise returns the original sender
  String getBankNameFromMessage(String sender, String messageBody) {
    final bodyLower = messageBody.toLowerCase();

    // First check if sender is already a bank name
    for (final bank in bankSenders) {
      if (sender.toLowerCase().contains(bank.toLowerCase())) {
        return bank;
      }
    }

    // Check message body for bank name
    for (final bank in bankSenders) {
      final bankLower = bank.toLowerCase();
      if (bodyLower.startsWith(bankLower) ||
          bodyLower.contains('$bankLower:') ||
          bodyLower.contains('$bankLower ') ||
          bodyLower.contains('$bankLower\n')) {
        return bank;
      }
    }

    // Return original sender if no bank name found
    return sender;
  }

  /// Send SMS to API for parsing
  Future<ParsedSmsTransaction?> parseSms({
    required String sender,
    required String message,
    required DateTime dateReceived,
  }) async {
    try {
      final url = Uri.parse('$_baseUrl/sms/receive');
      final token = await _authService.getToken();

      // Use correct field names as expected by the API
      final requestBody = {
        'senderPhone': sender,
        'smsContent': message,
        'receivedAt': dateReceived.toIso8601String(),
      };

      final headers = {'Content-Type': 'application/json'};

      if (token != null && token.isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }

      print('📱 Parsing SMS from: $sender');
      print(
        '📝 Message: ${message.length > 100 ? '${message.substring(0, 100)}...' : message}',
      );

      final response = await http.post(
        url,
        headers: headers,
        body: jsonEncode(requestBody),
      );

      print('📊 Parse SMS API Status: ${response.statusCode}');
      print('📄 Response body: ${response.body}');

      if (response.statusCode == 200 || response.statusCode == 201) {
        final jsonResponse = jsonDecode(response.body);
        print('📋 Parsed JSON: $jsonResponse');
        final transaction = ParsedSmsTransaction.fromJson(jsonResponse);
        print(
          '✅ Transaction parsed - Type: ${transaction.type}, Amount: ${transaction.amount}, isValid: ${transaction.isValid}',
        );
        return transaction;
      } else {
        print('❌ Failed to parse SMS: ${response.body}');
        return null;
      }
    } catch (e) {
      print('❌ Error parsing SMS: $e');
      return null;
    }
  }

  /// Add parsed transaction to the system
  Future<bool> addParsedTransaction(ParsedSmsTransaction transaction) async {
    try {
      if (!transaction.isValid || transaction.amount == null) {
        print('⚠️ Invalid transaction, skipping');
        return false;
      }

      final url = Uri.parse('$_baseUrl/transactions');
      final token = await _authService.getToken();

      // Determine transaction source based on bank name
      String transactionSource = 'Ngân hàng';
      if (transaction.bankName != null) {
        final bankLower = transaction.bankName!.toLowerCase();
        if (bankLower.contains('momo') ||
            bankLower.contains('zalo') ||
            bankLower.contains('shopee') ||
            bankLower.contains('vnpay')) {
          transactionSource = 'Ví điện tử';
        }
      }

      final requestBody = {
        'type': transaction.type?.toUpperCase() ?? 'EXPENSE',
        'amount': transaction.amount,
        'transactionSource': transactionSource,
        'description': transaction.description ?? transaction.rawMessage ?? '',
        'transactionDate':
            transaction.transactionDate ?? DateTime.now().toIso8601String(),
        'isAuto': true,
        'notes': 'Tự động từ SMS ${transaction.bankName ?? ''}',
      };

      final headers = {'Content-Type': 'application/json'};

      if (token != null && token.isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }

      print(
        '💾 Adding transaction: ${transaction.type} - ${transaction.amount}',
      );

      final response = await http.post(
        url,
        headers: headers,
        body: jsonEncode(requestBody),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        print('✅ Transaction added successfully');
        return true;
      } else {
        print('❌ Failed to add transaction: ${response.body}');
        return false;
      }
    } catch (e) {
      print('❌ Error adding transaction: $e');
      return false;
    }
  }

  /// Scan and process multiple SMS messages
  Future<SmsScanResult> scanAndProcessSms(
    List<Map<String, dynamic>> smsMessages,
  ) async {
    int totalScanned = smsMessages.length;
    int validCount = 0;
    int addedCount = 0;
    int failedCount = 0;
    List<ParsedSmsTransaction> transactions = [];
    List<String> errors = [];

    // Track processed messages to avoid duplicates
    Set<String> processedMessages = {};

    for (final sms in smsMessages) {
      try {
        final sender = sms['sender'] as String? ?? '';
        final message = sms['message'] as String? ?? '';
        final date = sms['date'] as DateTime? ?? DateTime.now();

        // Create a unique key for this message to detect duplicates
        final messageKey = '${message.hashCode}_${date.millisecondsSinceEpoch}';

        // Skip if already processed
        if (processedMessages.contains(messageKey)) {
          print('⏭️ Skipping duplicate SMS');
          continue;
        }
        processedMessages.add(messageKey);

        // Parse SMS
        final parsed = await parseSms(
          sender: sender,
          message: message,
          dateReceived: date,
        );

        if (parsed != null && parsed.isValid) {
          validCount++;
          transactions.add(parsed);
          // Note: Transaction is already added by /api/sms/receive endpoint
          // No need to call addParsedTransaction() again
          addedCount++;
        }
      } catch (e) {
        failedCount++;
        errors.add('Lỗi xử lý SMS: $e');
      }
    }

    return SmsScanResult(
      totalSmsScanned: totalScanned,
      validTransactions: validCount,
      addedTransactions: addedCount,
      failedTransactions: failedCount,
      transactions: transactions,
      errors: errors,
    );
  }
}
