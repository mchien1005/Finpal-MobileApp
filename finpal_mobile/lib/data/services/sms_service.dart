import 'dart:convert';
import 'package:http/http.dart' as http;
import 'auth_service.dart';

/// Model chứa dữ liệu giao dịch được parse từ SMS
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
    // Parse số tiền từ các định dạng khác nhau
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

    // Xem như hợp lệ nếu có số tiền và loại giao dịch
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

/// Kết quả của thao tác quét SMS
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

/// Service xử lý quét và parse SMS ngân hàng
class SmsService {
  final String _baseUrl = 'http://175.41.150.228:8080/api';
  final AuthService _authService = AuthService();

  /// Danh sách các địa chỉ gửi SMS ngân hàng đã biết
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

  /// Kiểm tra xem SMS có phải từ ngân hàng không
  /// Cũng kiểm tra nội dung tin nhắn để tìm từ khóa ngân hàng
  bool isBankSms(String sender, {String? messageBody}) {
    final senderLower = sender.toLowerCase();
    final bodyLower = (messageBody ?? '').toLowerCase();

    // Kiểm tra tên/địa chỉ người gửi
    final isSenderBank = bankSenders.any(
      (bank) => senderLower.contains(bank.toLowerCase()),
    );

    // Kiểm tra nội dung tin nhắn có chứa tên ngân hàng không
    // Tìm các pattern như "BIDV:", "VCB:", hoặc tin nhắn bắt đầu bằng tên ngân hàng
    final isBodyFromBank = bankSenders.any((bank) {
      final bankLower = bank.toLowerCase();
      return bodyLower.startsWith(bankLower) ||
          bodyLower.contains('$bankLower:') ||
          bodyLower.contains('$bankLower ') ||
          bodyLower.contains('$bankLower\n');
    });

    print(
      '🔍 Kiểm tra SMS ngân hàng - Người gửi: $sender, isSenderBank: $isSenderBank, isBodyFromBank: $isBodyFromBank',
    );
    return isSenderBank || isBodyFromBank;
  }

  /// Trích xuất tên ngân hàng từ nội dung tin nhắn hoặc người gửi
  /// Trả về tên ngân hàng nếu tìm thấy, ngược lại trả về người gửi gốc
  String getBankNameFromMessage(String sender, String messageBody) {
    final bodyLower = messageBody.toLowerCase();

    // Kiểm tra người gửi có phải tên ngân hàng không
    for (final bank in bankSenders) {
      if (sender.toLowerCase().contains(bank.toLowerCase())) {
        return bank;
      }
    }

    // Kiểm tra nội dung tin nhắn để tìm tên ngân hàng
    for (final bank in bankSenders) {
      final bankLower = bank.toLowerCase();
      if (bodyLower.startsWith(bankLower) ||
          bodyLower.contains('$bankLower:') ||
          bodyLower.contains('$bankLower ') ||
          bodyLower.contains('$bankLower\n')) {
        return bank;
      }
    }

    // Trả về người gửi gốc nếu không tìm thấy tên ngân hàng
    return sender;
  }

  /// Gửi SMS đến API để parse
  Future<ParsedSmsTransaction?> parseSms({
    required String sender,
    required String message,
    required DateTime dateReceived,
  }) async {
    try {
      final url = Uri.parse('$_baseUrl/sms/receive');
      final token = await _authService.getToken();

      // Sử dụng đúng tên field theo API yêu cầu
      final requestBody = {
        'senderPhone': sender,
        'smsContent': message,
        'receivedAt': dateReceived.toIso8601String(),
      };

      final headers = {'Content-Type': 'application/json'};

      if (token != null && token.isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }

      print('📱 Đang parse SMS từ: $sender');
      print(
        '📝 Nội dung: ${message.length > 100 ? '${message.substring(0, 100)}...' : message}',
      );

      final response = await http.post(
        url,
        headers: headers,
        body: jsonEncode(requestBody),
      );

      print('📊 Trạng thái API Parse SMS: ${response.statusCode}');
      print('📄 Phản hồi: ${response.body}');

      if (response.statusCode == 200 || response.statusCode == 201) {
        final jsonResponse = jsonDecode(response.body);
        print('📋 JSON đã parse: $jsonResponse');
        final transaction = ParsedSmsTransaction.fromJson(jsonResponse);
        print(
          '✅ Đã parse giao dịch - Loại: ${transaction.type}, Số tiền: ${transaction.amount}, isValid: ${transaction.isValid}',
        );
        return transaction;
      } else {
        print('❌ Parse SMS thất bại: ${response.body}');
        return null;
      }
    } catch (e) {
      print('❌ Lỗi parse SMS: $e');
      return null;
    }
  }

  /// Thêm giao dịch đã parse vào hệ thống
  Future<bool> addParsedTransaction(ParsedSmsTransaction transaction) async {
    try {
      if (!transaction.isValid || transaction.amount == null) {
        print('⚠️ Giao dịch không hợp lệ, bỏ qua');
        return false;
      }

      final url = Uri.parse('$_baseUrl/transactions');
      final token = await _authService.getToken();

      // Xác định nguồn giao dịch dựa trên tên ngân hàng
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
        '💾 Đang thêm giao dịch: ${transaction.type} - ${transaction.amount}',
      );

      final response = await http.post(
        url,
        headers: headers,
        body: jsonEncode(requestBody),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        print('✅ Thêm giao dịch thành công');
        return true;
      } else {
        print('❌ Thêm giao dịch thất bại: ${response.body}');
        return false;
      }
    } catch (e) {
      print('❌ Lỗi thêm giao dịch: $e');
      return false;
    }
  }

  /// Quét và xử lý nhiều tin nhắn SMS
  Future<SmsScanResult> scanAndProcessSms(
    List<Map<String, dynamic>> smsMessages,
  ) async {
    int totalScanned = smsMessages.length;
    int validCount = 0;
    int addedCount = 0;
    int failedCount = 0;
    List<ParsedSmsTransaction> transactions = [];
    List<String> errors = [];

    // Theo dõi các tin nhắn đã xử lý để tránh trùng lặp
    Set<String> processedMessages = {};

    for (final sms in smsMessages) {
      try {
        final sender = sms['sender'] as String? ?? '';
        final message = sms['message'] as String? ?? '';
        final date = sms['date'] as DateTime? ?? DateTime.now();

        // Tạo key duy nhất cho tin nhắn để phát hiện trùng lặp
        final messageKey = '${message.hashCode}_${date.millisecondsSinceEpoch}';

        // Bỏ qua nếu đã xử lý
        if (processedMessages.contains(messageKey)) {
          print('⏭️ Bỏ qua SMS trùng lặp');
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
          // Giao dịch đã được thêm bởi endpoint /api/sms/receive
          // Không cần gọi addParsedTransaction() nữa
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
